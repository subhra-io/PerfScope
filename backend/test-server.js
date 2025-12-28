#!/usr/bin/env node

/**
 * Simple test server to demonstrate PerfScope SDK → Backend integration
 * This runs without database dependencies for quick testing
 */

const express = require('express');
const cors = require('cors');

const app = express();
const PORT = 3001;

// Middleware
app.use(cors());
app.use(express.json({ limit: '10mb' }));

// In-memory storage for demo
const receivedEvents = [];
const apps = new Map([
  ['demo-api-key-12345', { 
    id: 'demo-app', 
    name: 'PerfScope Demo', 
    package_id: 'io.perfscope.demo',
    device_id: 'def93dee',
    device_info: {
      model: 'CPH2691',
      manufacturer: 'OnePlus',
      brand: 'OnePlus',
      android_version: 35,
      android_release: '15',
      ram_mb: 11214,
      screen_density: 560,
      screen_resolution: '1264x2780'
    }
  }],
  ['ps_abc123', { id: 'test-app', name: 'Test App', package_id: 'com.example.testapp' }]
]);

// Health check
app.get('/health', (req, res) => {
  res.json({
    status: 'healthy',
    timestamp: new Date().toISOString(),
    version: '1.0.0-test',
    environment: 'test'
  });
});

// Event ingestion endpoint
app.post('/api/events', (req, res) => {
  console.log('\n🚀 Received event batch from SDK:');
  console.log('Headers:', req.headers);
  console.log('Body:', JSON.stringify(req.body, null, 2));
  
  try {
    const { api_key, events } = req.body;
    
    // Validate API key
    const app = apps.get(api_key);
    if (!app) {
      console.log('❌ Invalid API key:', api_key);
      return res.status(401).json({ error: 'Invalid API key' });
    }
    
    console.log('✅ Authenticated app:', app.name);
    
    // Validate events array
    if (!Array.isArray(events)) {
      console.log('❌ Invalid events format');
      return res.status(400).json({ error: 'Events must be an array' });
    }
    
    // Process each event
    events.forEach((event, index) => {
      console.log(`\n📊 Event ${index + 1}/${events.length}:`);
      console.log(`   Type: ${event.event_type}`);
      console.log(`   Screen: ${event.screen}`);
      console.log(`   Timestamp: ${new Date(event.timestamp).toISOString()}`);
      
      if (event.event_type === 'MEMORY_VIOLATION') {
        console.log(`   🚨 Memory Violation: ${event.violation_type}`);
        console.log(`   📈 Actual: ${event.actual_mb}MB, Budget: ${event.budget_mb}MB`);
        console.log(`   ⚠️  Severity: ${event.severity}`);
        console.log(`   🔍 Attribution: ${event.attribution?.likely_cause}`);
      }
      
      if (event.event_type === 'JANK_VIOLATION') {
        console.log(`   🚨 Jank Violation: ${event.violation_type}`);
        console.log(`   📈 Actual: ${event.actual_value}%, Budget: ${event.budget_value}%`);
        console.log(`   ⚠️  Severity: ${event.severity}`);
        console.log(`   🎯 Jank Type: ${event.jank_type}`);
      }
      
      if (event.event_type === 'SESSION_START') {
        console.log(`   🎬 Session Started: ${event.session_id}`);
        console.log(`   📱 Device: ${event.device?.manufacturer} ${event.device?.model} (${event.device?.android_version})`);
        console.log(`   💾 RAM: ${event.device?.ram_mb}MB`);
        console.log(`   🏗️  Build: ${event.build?.version_name} (${event.build?.version_code})`);
        console.log(`   🔍 Device ID: def93dee (Your OnePlus)`);
      }
      
      if (event.event_type === 'SCREEN_CHANGE') {
        console.log(`   🔄 Screen Change: ${event.previous_screen} → ${event.screen}`);
        console.log(`   ⏱️  Time on previous: ${event.time_on_previous_screen_ms}ms`);
      }
      
      if (event.event_type === 'HEALTH_SNAPSHOT') {
        console.log(`   💚 Health Snapshot:`);
        console.log(`   📊 Memory: ${event.memory_mb}MB, Jank: ${event.jank_percent}%`);
        console.log(`   🎯 Avg Frame: ${event.avg_frame_ms}ms`);
      }
      
      // Store event
      receivedEvents.push({
        ...event,
        app_id: app.id,
        received_at: new Date().toISOString()
      });
    });
    
    console.log(`\n✅ Successfully processed ${events.length} events`);
    console.log(`📈 Total events received: ${receivedEvents.length}`);
    
    res.json({
      success: true,
      processed_events: events.length,
      app_id: app.id,
      total_events_received: receivedEvents.length
    });
    
  } catch (error) {
    console.error('❌ Error processing events:', error);
    res.status(500).json({
      error: 'Internal server error',
      message: error.message
    });
  }
});

// Get received events (for testing)
app.get('/api/events/received', (req, res) => {
  const limit = parseInt(req.query.limit) || 50;
  const events = receivedEvents.slice(-limit).reverse();
  
  res.json({
    total_events: receivedEvents.length,
    events: events,
    summary: {
      violations: events.filter(e => e.event_type.includes('VIOLATION')).length,
      sessions: events.filter(e => e.event_type === 'SESSION_START').length,
      screen_changes: events.filter(e => e.event_type === 'SCREEN_CHANGE').length,
      health_snapshots: events.filter(e => e.event_type === 'HEALTH_SNAPSHOT').length
    }
  });
});

// Clear events (for testing)
app.delete('/api/events/clear', (req, res) => {
  const count = receivedEvents.length;
  receivedEvents.length = 0;
  res.json({ message: `Cleared ${count} events` });
});

// Start server
app.listen(PORT, () => {
  console.log(`\n🚀 PerfScope Test Backend running on http://localhost:${PORT}`);
  console.log(`📊 Health check: http://localhost:${PORT}/health`);
  console.log(`📈 View events: http://localhost:${PORT}/api/events/received`);
  console.log(`\n🔑 Valid API Keys:`);
  apps.forEach((app, key) => {
    console.log(`   ${key} → ${app.name} (${app.package_id})`);
  });
  console.log(`\n⏳ Waiting for events from PerfScope SDK...`);
});