import { AppRepository } from '../models/App';
import { logger } from '../utils/logger';
import { pool } from '../config/database';

async function seedDatabase() {
  try {
    logger.info('Starting database seeding...');
    
    const appRepo = new AppRepository();
    
    // Create demo app
    const demoApp = await appRepo.create({
      name: 'PerfScope Demo App',
      package_id: 'io.perfscope.demo',
      description: 'Demo application for testing PerfScope SDK',
      team_id: 'demo-team'
    });
    
    logger.info('Created demo app', {
      appId: demoApp.id,
      packageId: demoApp.package_id,
      apiKey: demoApp.api_key
    });
    
    // Create additional test apps if needed
    const testApp = await appRepo.create({
      name: 'Test Mobile App',
      package_id: 'com.example.testapp',
      description: 'Test application for development',
      team_id: 'test-team'
    });
    
    logger.info('Created test app', {
      appId: testApp.id,
      packageId: testApp.package_id,
      apiKey: testApp.api_key
    });
    
    logger.info('Database seeding completed successfully');
    
    // Print API keys for easy access
    console.log('\n=== API KEYS FOR TESTING ===');
    console.log(`Demo App (${demoApp.package_id}): ${demoApp.api_key}`);
    console.log(`Test App (${testApp.package_id}): ${testApp.api_key}`);
    console.log('================================\n');
    
  } catch (error) {
    logger.error('Seeding failed', error);
    throw error;
  } finally {
    await pool.end();
  }
}

// Run seeding if this file is executed directly
if (require.main === module) {
  seedDatabase()
    .then(() => process.exit(0))
    .catch(() => process.exit(1));
}

export { seedDatabase };