import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import compression from 'compression';
import { logger } from './utils/logger';
import { eventIngestionLimiter, apiLimiter } from './middleware/rateLimiter';
import { EventController } from './controllers/EventController';

const app = express();

// Security middleware
app.use(helmet({
  contentSecurityPolicy: {
    directives: {
      defaultSrc: ["'self'"],
      styleSrc: ["'self'", "'unsafe-inline'"],
      scriptSrc: ["'self'"],
      imgSrc: ["'self'", "data:", "https:"],
    },
  },
  hsts: {
    maxAge: 31536000,
    includeSubDomains: true,
    preload: true
  }
}));

// CORS configuration
app.use(cors({
  origin: process.env.ALLOWED_ORIGINS?.split(',') || ['http://localhost:3000'],
  credentials: true,
  methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
  allowedHeaders: ['Content-Type', 'Authorization', 'X-Requested-With']
}));

// Compression middleware
app.use(compression());

// Body parsing middleware
app.use(express.json({ 
  limit: '10mb', // Allow larger payloads for event batches
  verify: (req, res, buf) => {
    // Store raw body for signature verification if needed
    (req as any).rawBody = buf;
  }
}));

app.use(express.urlencoded({ extended: true, limit: '10mb' }));

// Request logging middleware
app.use((req, res, next) => {
  const start = Date.now();
  
  res.on('finish', () => {
    const duration = Date.now() - start;
    
    logger.info('HTTP Request', {
      method: req.method,
      url: req.url,
      status: res.statusCode,
      duration,
      ip: req.ip,
      userAgent: req.get('User-Agent'),
      contentLength: res.get('Content-Length')
    });
  });
  
  next();
});

// Health check endpoint
app.get('/health', (req, res) => {
  res.json({
    status: 'healthy',
    timestamp: new Date().toISOString(),
    version: process.env.npm_package_version || '1.0.0',
    environment: process.env.NODE_ENV || 'development'
  });
});

// API routes
const eventController = new EventController();

// Event ingestion endpoint (high-throughput)
app.post('/api/events', eventIngestionLimiter, (req, res) => {
  eventController.ingestEvents(req, res);
});

// API endpoints with rate limiting
app.use('/api', apiLimiter);

// Event statistics endpoint
app.get('/api/apps/:appId/events/stats', (req, res) => {
  eventController.getEventStats(req, res);
});

// Apps management endpoints (would be expanded)
app.get('/api/apps', (req, res) => {
  res.json({ message: 'Apps endpoint - to be implemented' });
});

// Sessions endpoints (would be expanded)
app.get('/api/apps/:appId/sessions', (req, res) => {
  res.json({ message: 'Sessions endpoint - to be implemented' });
});

// Violations endpoints (would be expanded)
app.get('/api/apps/:appId/violations', (req, res) => {
  res.json({ message: 'Violations endpoint - to be implemented' });
});

// Metrics endpoints (would be expanded)
app.get('/api/apps/:appId/metrics', (req, res) => {
  res.json({ message: 'Metrics endpoint - to be implemented' });
});

// 404 handler
app.use('*', (req, res) => {
  res.status(404).json({
    error: 'Not Found',
    message: `Route ${req.method} ${req.originalUrl} not found`,
    timestamp: new Date().toISOString()
  });
});

// Global error handler
app.use((err: any, req: express.Request, res: express.Response, next: express.NextFunction) => {
  logger.error('Unhandled error', {
    error: err.message,
    stack: err.stack,
    url: req.url,
    method: req.method,
    ip: req.ip
  });
  
  res.status(err.status || 500).json({
    error: 'Internal Server Error',
    message: process.env.NODE_ENV === 'development' ? err.message : 'Something went wrong',
    timestamp: new Date().toISOString()
  });
});

export default app;