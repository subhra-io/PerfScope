import { Request, Response } from 'express';
import { EventBatchSchema } from '../schemas/events';
import { AppRepository } from '../models/App';
import { EventIngestionService } from '../services/EventIngestionService';
import { logger } from '../utils/logger';

export class EventController {
  private appRepo = new AppRepository();
  private ingestionService = new EventIngestionService();
  
  async ingestEvents(req: Request, res: Response): Promise<void> {
    try {
      // Validate request body
      const parseResult = EventBatchSchema.safeParse(req.body);
      if (!parseResult.success) {
        res.status(400).json({
          error: 'Invalid request format',
          details: parseResult.error.issues
        });
        return;
      }
      
      const { api_key, events } = parseResult.data;
      
      // Authenticate app
      const app = await this.appRepo.findByApiKey(api_key);
      if (!app) {
        res.status(401).json({
          error: 'Invalid API key'
        });
        return;
      }
      
      // Rate limiting check (basic)
      if (events.length > 100) {
        res.status(429).json({
          error: 'Too many events in batch',
          max_allowed: 100,
          received: events.length
        });
        return;
      }
      
      // Ingest events
      await this.ingestionService.ingestEvents(app.id, events);
      
      // Success response
      res.status(200).json({
        success: true,
        processed_events: events.length,
        app_id: app.id
      });
      
      logger.info('Events ingested successfully', {
        appId: app.id,
        packageId: app.package_id,
        eventCount: events.length,
        clientIp: req.ip,
        userAgent: req.get('User-Agent')
      });
      
    } catch (error) {
      logger.error('Error ingesting events', {
        error: error instanceof Error ? error.message : error,
        stack: error instanceof Error ? error.stack : undefined,
        body: req.body
      });
      
      res.status(500).json({
        error: 'Internal server error',
        message: 'Failed to process events'
      });
    }
  }
  
  async getEventStats(req: Request, res: Response): Promise<void> {
    try {
      const { appId } = req.params;
      const hours = parseInt(req.query.hours as string) || 24;
      
      // Authenticate app access (simplified for demo)
      const app = await this.appRepo.findByApiKey(req.headers.authorization?.replace('Bearer ', '') || '');
      if (!app || app.id !== appId) {
        res.status(403).json({ error: 'Access denied' });
        return;
      }
      
      // Get event statistics
      const stats = await this.getAppEventStats(appId, hours);
      
      res.json({
        app_id: appId,
        time_range_hours: hours,
        stats
      });
      
    } catch (error) {
      logger.error('Error getting event stats', { error, appId: req.params.appId });
      res.status(500).json({ error: 'Internal server error' });
    }
  }
  
  private async getAppEventStats(appId: string, hours: number): Promise<any> {
    // This would be implemented with proper aggregation queries
    // For now, return mock data structure
    return {
      total_events: 0,
      violations: {
        memory: 0,
        jank: 0
      },
      sessions: {
        total: 0,
        active: 0
      },
      top_screens: [],
      device_breakdown: {}
    };
  }
}