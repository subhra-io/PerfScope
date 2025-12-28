import { pool } from '../config/database';
import { logger } from '../utils/logger';
import { DeviceInfo, BuildInfo } from '../schemas/events';

export interface Session {
  id: string;
  app_id: string;
  session_id: string;
  started_at: Date;
  ended_at?: Date;
  duration_ms?: number;
  device: DeviceInfo;
  build: BuildInfo;
  total_violations: number;
  max_memory_mb: number;
  avg_jank_percent: number;
  screen_count: number;
  sdk_version?: string;
  created_at: Date;
}

export interface CreateSessionData {
  app_id: string;
  session_id: string;
  started_at: Date;
  device: DeviceInfo;
  build: BuildInfo;
  sdk_version?: string;
}

export interface UpdateSessionData {
  ended_at?: Date;
  duration_ms?: number;
  total_violations?: number;
  max_memory_mb?: number;
  avg_jank_percent?: number;
  screen_count?: number;
}

export class SessionRepository {
  
  async create(data: CreateSessionData): Promise<Session> {
    try {
      const query = `
        INSERT INTO sessions (
          app_id, session_id, started_at, device, build, sdk_version
        ) VALUES ($1, $2, $3, $4, $5, $6)
        ON CONFLICT (app_id, session_id) 
        DO UPDATE SET 
          started_at = EXCLUDED.started_at,
          device = EXCLUDED.device,
          build = EXCLUDED.build,
          sdk_version = EXCLUDED.sdk_version
        RETURNING *
      `;
      
      const values = [
        data.app_id,
        data.session_id,
        data.started_at,
        JSON.stringify(data.device),
        JSON.stringify(data.build),
        data.sdk_version
      ];
      
      const result = await pool.query(query, values);
      const session = result.rows[0];
      
      logger.info('Created/updated session', {
        sessionId: session.id,
        appId: session.app_id,
        sdkSessionId: session.session_id
      });
      
      return session;
    } catch (error) {
      logger.error('Error creating session', { error, data });
      throw error;
    }
  }
  
  async findBySessionId(appId: string, sessionId: string): Promise<Session | null> {
    try {
      const query = `
        SELECT * FROM sessions 
        WHERE app_id = $1 AND session_id = $2
      `;
      
      const result = await pool.query(query, [appId, sessionId]);
      return result.rows[0] || null;
    } catch (error) {
      logger.error('Error finding session', { error, appId, sessionId });
      throw error;
    }
  }
  
  async update(id: string, data: UpdateSessionData): Promise<void> {
    try {
      const updates: string[] = [];
      const values: any[] = [];
      let paramIndex = 1;
      
      if (data.ended_at !== undefined) {
        updates.push(`ended_at = $${paramIndex++}`);
        values.push(data.ended_at);
      }
      
      if (data.duration_ms !== undefined) {
        updates.push(`duration_ms = $${paramIndex++}`);
        values.push(data.duration_ms);
      }
      
      if (data.total_violations !== undefined) {
        updates.push(`total_violations = $${paramIndex++}`);
        values.push(data.total_violations);
      }
      
      if (data.max_memory_mb !== undefined) {
        updates.push(`max_memory_mb = $${paramIndex++}`);
        values.push(data.max_memory_mb);
      }
      
      if (data.avg_jank_percent !== undefined) {
        updates.push(`avg_jank_percent = $${paramIndex++}`);
        values.push(data.avg_jank_percent);
      }
      
      if (data.screen_count !== undefined) {
        updates.push(`screen_count = $${paramIndex++}`);
        values.push(data.screen_count);
      }
      
      if (updates.length === 0) return;
      
      const query = `
        UPDATE sessions 
        SET ${updates.join(', ')}
        WHERE id = $${paramIndex}
      `;
      
      values.push(id);
      
      await pool.query(query, values);
      
      logger.debug('Updated session', { sessionId: id, updates: Object.keys(data) });
    } catch (error) {
      logger.error('Error updating session', { error, id, data });
      throw error;
    }
  }
  
  async findByApp(appId: string, limit = 50, offset = 0): Promise<Session[]> {
    try {
      const query = `
        SELECT * FROM sessions 
        WHERE app_id = $1
        ORDER BY started_at DESC
        LIMIT $2 OFFSET $3
      `;
      
      const result = await pool.query(query, [appId, limit, offset]);
      return result.rows;
    } catch (error) {
      logger.error('Error finding sessions by app', { error, appId });
      throw error;
    }
  }
  
  async getActiveSessions(appId: string): Promise<Session[]> {
    try {
      const query = `
        SELECT * FROM sessions 
        WHERE app_id = $1 AND ended_at IS NULL
        ORDER BY started_at DESC
      `;
      
      const result = await pool.query(query, [appId]);
      return result.rows;
    } catch (error) {
      logger.error('Error finding active sessions', { error, appId });
      throw error;
    }
  }
  
  async getSessionStats(appId: string, hours = 24): Promise<{
    total_sessions: number;
    active_sessions: number;
    avg_duration_ms: number;
    total_violations: number;
  }> {
    try {
      const query = `
        SELECT 
          COUNT(*) as total_sessions,
          COUNT(CASE WHEN ended_at IS NULL THEN 1 END) as active_sessions,
          AVG(duration_ms) as avg_duration_ms,
          SUM(total_violations) as total_violations
        FROM sessions 
        WHERE app_id = $1 
          AND started_at > NOW() - INTERVAL '${hours} hours'
      `;
      
      const result = await pool.query(query, [appId]);
      return result.rows[0];
    } catch (error) {
      logger.error('Error getting session stats', { error, appId, hours });
      throw error;
    }
  }
}