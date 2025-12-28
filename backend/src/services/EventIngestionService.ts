import { pool } from '../config/database';
import { logger } from '../utils/logger';
import { 
  PerfEvent, 
  SessionStartEvent, 
  SessionEndEvent, 
  ScreenChangeEvent,
  MemoryViolationEvent,
  JankViolationEvent,
  HealthSnapshotEvent 
} from '../schemas/events';
import { SessionRepository } from '../models/Session';

export class EventIngestionService {
  private sessionRepo = new SessionRepository();
  
  async ingestEvents(appId: string, events: PerfEvent[]): Promise<void> {
    const client = await pool.connect();
    
    try {
      await client.query('BEGIN');
      
      // Process events in order to maintain session state
      for (const event of events) {
        await this.processEvent(client, appId, event);
      }
      
      await client.query('COMMIT');
      
      logger.info('Successfully ingested events', {
        appId,
        eventCount: events.length,
        eventTypes: this.getEventTypeCounts(events)
      });
      
    } catch (error) {
      await client.query('ROLLBACK');
      logger.error('Failed to ingest events', { error, appId, eventCount: events.length });
      throw error;
    } finally {
      client.release();
    }
  }
  
  private async processEvent(client: any, appId: string, event: PerfEvent): Promise<void> {
    switch (event.event_type) {
      case 'SESSION_START':
        await this.handleSessionStart(client, appId, event);
        break;
        
      case 'SESSION_END':
        await this.handleSessionEnd(client, appId, event);
        break;
        
      case 'SCREEN_CHANGE':
        await this.handleScreenChange(client, appId, event);
        break;
        
      case 'MEMORY_VIOLATION':
        await this.handleMemoryViolation(client, appId, event);
        break;
        
      case 'JANK_VIOLATION':
        await this.handleJankViolation(client, appId, event);
        break;
        
      case 'HEALTH_SNAPSHOT':
        await this.handleHealthSnapshot(client, appId, event);
        break;
        
      default:
        logger.warn('Unknown event type', { eventType: (event as any).event_type });
    }
  }
  
  private async handleSessionStart(client: any, appId: string, event: SessionStartEvent): Promise<void> {
    // Create or update session
    const sessionData = {
      app_id: appId,
      session_id: event.session_id,
      started_at: new Date(event.timestamp),
      device: event.device,
      build: event.build,
      sdk_version: event.sdk_version
    };
    
    await this.sessionRepo.create(sessionData);
    
    // Insert event record
    await this.insertEvent(client, appId, event);
  }
  
  private async handleSessionEnd(client: any, appId: string, event: SessionEndEvent): Promise<void> {
    // Find session and update it
    const session = await this.sessionRepo.findBySessionId(appId, event.session_id);
    if (session) {
      await this.sessionRepo.update(session.id, {
        ended_at: new Date(event.timestamp),
        duration_ms: event.duration_ms,
        total_violations: event.total_violations
      });
    }
    
    // Insert event record
    await this.insertEvent(client, appId, event);
  }
  
  private async handleScreenChange(client: any, appId: string, event: ScreenChangeEvent): Promise<void> {
    // Insert into screen_changes table
    const query = `
      INSERT INTO screen_changes (session_id, timestamp, from_screen, to_screen, duration_ms)
      SELECT s.id, $1, $2, $3, $4
      FROM sessions s
      WHERE s.app_id = $5 AND s.session_id = $6
    `;
    
    await client.query(query, [
      new Date(event.timestamp),
      event.previous_screen,
      event.screen,
      event.time_on_previous_screen_ms,
      appId,
      event.session_id
    ]);
    
    // Update session screen count
    const updateQuery = `
      UPDATE sessions 
      SET screen_count = screen_count + 1
      WHERE app_id = $1 AND session_id = $2
    `;
    
    await client.query(updateQuery, [appId, event.session_id]);
    
    // Insert event record
    await this.insertEvent(client, appId, event);
  }
  
  private async handleMemoryViolation(client: any, appId: string, event: MemoryViolationEvent): Promise<void> {
    // Insert event with extracted fields for fast querying
    await this.insertViolationEvent(client, appId, event, {
      violation_type: event.violation_type,
      severity: event.severity,
      actual_value: event.actual_mb,
      budget_value: event.budget_mb
    });
    
    // Update session max memory if needed
    const updateQuery = `
      UPDATE sessions 
      SET max_memory_mb = GREATEST(max_memory_mb, $1)
      WHERE app_id = $2 AND session_id = $3
    `;
    
    await client.query(updateQuery, [event.actual_mb, appId, event.session_id]);
    
    // Update violation summaries (for dashboard aggregation)
    await this.updateViolationSummary(client, appId, event);
  }
  
  private async handleJankViolation(client: any, appId: string, event: JankViolationEvent): Promise<void> {
    // Insert event with extracted fields
    await this.insertViolationEvent(client, appId, event, {
      violation_type: event.violation_type,
      severity: event.severity,
      actual_value: event.actual_value,
      budget_value: event.budget_value
    });
    
    // Update session jank average
    const updateQuery = `
      UPDATE sessions 
      SET avg_jank_percent = (
        SELECT AVG((payload->>'actual_value')::decimal)
        FROM events 
        WHERE session_id = (
          SELECT id FROM sessions WHERE app_id = $1 AND session_id = $2
        ) AND event_type = 'JANK_VIOLATION'
      )
      WHERE app_id = $1 AND session_id = $2
    `;
    
    await client.query(updateQuery, [appId, event.session_id]);
    
    // Update violation summaries
    await this.updateViolationSummary(client, appId, event);
  }
  
  private async handleHealthSnapshot(client: any, appId: string, event: HealthSnapshotEvent): Promise<void> {
    // Insert into health_metrics table for time-series analysis
    const query = `
      INSERT INTO health_metrics (session_id, timestamp, screen, memory_mb, jank_percent, avg_frame_ms, cpu_percent)
      SELECT s.id, $1, $2, $3, $4, $5, $6
      FROM sessions s
      WHERE s.app_id = $7 AND s.session_id = $8
    `;
    
    await client.query(query, [
      new Date(event.timestamp),
      event.screen,
      event.memory_mb,
      event.jank_percent,
      event.avg_frame_ms,
      event.cpu_percent,
      appId,
      event.session_id
    ]);
    
    // Insert event record
    await this.insertEvent(client, appId, event);
  }
  
  private async insertEvent(client: any, appId: string, event: PerfEvent): Promise<void> {
    const query = `
      INSERT INTO events (session_id, event_type, timestamp, screen, payload)
      SELECT s.id, $1, $2, $3, $4
      FROM sessions s
      WHERE s.app_id = $5 AND s.session_id = $6
    `;
    
    await client.query(query, [
      event.event_type,
      new Date(event.timestamp),
      event.screen,
      JSON.stringify(event),
      appId,
      event.session_id
    ]);
  }
  
  private async insertViolationEvent(
    client: any, 
    appId: string, 
    event: MemoryViolationEvent | JankViolationEvent,
    extracted: {
      violation_type: string;
      severity: string;
      actual_value: number;
      budget_value: number;
    }
  ): Promise<void> {
    const query = `
      INSERT INTO events (
        session_id, event_type, timestamp, screen, payload,
        violation_type, severity, actual_value, budget_value
      )
      SELECT s.id, $1, $2, $3, $4, $5, $6, $7, $8
      FROM sessions s
      WHERE s.app_id = $9 AND s.session_id = $10
    `;
    
    await client.query(query, [
      event.event_type,
      new Date(event.timestamp),
      event.screen,
      JSON.stringify(event),
      extracted.violation_type,
      extracted.severity,
      extracted.actual_value,
      extracted.budget_value,
      appId,
      event.session_id
    ]);
  }
  
  private async updateViolationSummary(
    client: any, 
    appId: string, 
    event: MemoryViolationEvent | JankViolationEvent
  ): Promise<void> {
    // Aggregate violations by hour for dashboard performance
    const timeBucket = new Date(event.timestamp);
    timeBucket.setMinutes(0, 0, 0); // Round to hour
    
    const query = `
      INSERT INTO violation_summaries (
        app_id, time_bucket, violation_type, screen, 
        device_model, build_version, violation_count, unique_sessions,
        avg_actual_value, avg_budget_value, severity_distribution
      ) VALUES (
        $1, $2, $3, $4, $5, $6, 1, 1, $7, $8, $9
      )
      ON CONFLICT (app_id, time_bucket, violation_type, screen, device_model, build_version)
      DO UPDATE SET
        violation_count = violation_summaries.violation_count + 1,
        unique_sessions = violation_summaries.unique_sessions + 1,
        avg_actual_value = (violation_summaries.avg_actual_value * violation_summaries.violation_count + EXCLUDED.avg_actual_value) / (violation_summaries.violation_count + 1),
        avg_budget_value = (violation_summaries.avg_budget_value * violation_summaries.violation_count + EXCLUDED.avg_budget_value) / (violation_summaries.violation_count + 1),
        severity_distribution = jsonb_set(
          COALESCE(violation_summaries.severity_distribution, '{}'::jsonb),
          ARRAY[$10],
          (COALESCE(violation_summaries.severity_distribution->>$10, '0')::int + 1)::text::jsonb
        ),
        updated_at = NOW()
    `;
    
    const actualValue = 'actual_mb' in event ? event.actual_mb : event.actual_value;
    const budgetValue = 'budget_mb' in event ? event.budget_mb : event.budget_value;
    
    await client.query(query, [
      appId,
      timeBucket,
      event.violation_type,
      event.screen,
      event.device.model,
      event.build.version_name,
      actualValue,
      budgetValue,
      JSON.stringify({ [event.severity]: 1 }),
      event.severity
    ]);
  }
  
  private getEventTypeCounts(events: PerfEvent[]): Record<string, number> {
    return events.reduce((counts, event) => {
      counts[event.event_type] = (counts[event.event_type] || 0) + 1;
      return counts;
    }, {} as Record<string, number>);
  }
}