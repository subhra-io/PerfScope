import { pool } from '../config/database';
import { logger } from '../utils/logger';

export interface App {
  id: string;
  name: string;
  package_id: string;
  api_key: string;
  description?: string;
  team_id?: string;
  is_active: boolean;
  created_at: Date;
  updated_at: Date;
}

export interface CreateAppData {
  name: string;
  package_id: string;
  description?: string;
  team_id?: string;
}

export class AppRepository {
  
  async findByApiKey(apiKey: string): Promise<App | null> {
    try {
      const query = `
        SELECT * FROM apps 
        WHERE api_key = $1 AND is_active = true
      `;
      
      const result = await pool.query(query, [apiKey]);
      return result.rows[0] || null;
    } catch (error) {
      logger.error('Error finding app by API key', { error, apiKey });
      throw error;
    }
  }
  
  async findByPackageId(packageId: string): Promise<App | null> {
    try {
      const query = `
        SELECT * FROM apps 
        WHERE package_id = $1
      `;
      
      const result = await pool.query(query, [packageId]);
      return result.rows[0] || null;
    } catch (error) {
      logger.error('Error finding app by package ID', { error, packageId });
      throw error;
    }
  }
  
  async create(data: CreateAppData): Promise<App> {
    try {
      const apiKey = this.generateApiKey();
      
      const query = `
        INSERT INTO apps (name, package_id, api_key, description, team_id)
        VALUES ($1, $2, $3, $4, $5)
        RETURNING *
      `;
      
      const values = [
        data.name,
        data.package_id,
        apiKey,
        data.description,
        data.team_id
      ];
      
      const result = await pool.query(query, values);
      const app = result.rows[0];
      
      logger.info('Created new app', { 
        appId: app.id, 
        packageId: app.package_id,
        name: app.name 
      });
      
      return app;
    } catch (error) {
      logger.error('Error creating app', { error, data });
      throw error;
    }
  }
  
  async findAll(): Promise<App[]> {
    try {
      const query = `
        SELECT * FROM apps 
        WHERE is_active = true
        ORDER BY created_at DESC
      `;
      
      const result = await pool.query(query);
      return result.rows;
    } catch (error) {
      logger.error('Error finding all apps', { error });
      throw error;
    }
  }
  
  async updateActivity(id: string, isActive: boolean): Promise<void> {
    try {
      const query = `
        UPDATE apps 
        SET is_active = $1, updated_at = NOW()
        WHERE id = $2
      `;
      
      await pool.query(query, [isActive, id]);
      
      logger.info('Updated app activity', { appId: id, isActive });
    } catch (error) {
      logger.error('Error updating app activity', { error, id, isActive });
      throw error;
    }
  }
  
  private generateApiKey(): string {
    // Generate a secure API key
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    let result = 'ps_'; // PerfScope prefix
    
    for (let i = 0; i < 32; i++) {
      result += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    
    return result;
  }
}