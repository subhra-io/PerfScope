import rateLimit from 'express-rate-limit';
import { logger } from '../utils/logger';

// Rate limiter for event ingestion endpoint
export const eventIngestionLimiter = rateLimit({
  windowMs: 60 * 1000, // 1 minute
  max: 100, // Limit each API key to 100 requests per minute
  message: {
    error: 'Too many requests',
    message: 'Rate limit exceeded. Please try again later.',
    retry_after: 60
  },
  standardHeaders: true,
  legacyHeaders: false,
  keyGenerator: (req) => {
    // Use API key from request body for rate limiting
    const apiKey = req.body?.api_key || req.headers.authorization?.replace('Bearer ', '') || req.ip;
    return `events:${apiKey}`;
  },
  onLimitReached: (req) => {
    logger.warn('Rate limit exceeded for event ingestion', {
      ip: req.ip,
      userAgent: req.get('User-Agent'),
      apiKey: req.body?.api_key ? 'present' : 'missing'
    });
  }
});

// Rate limiter for API endpoints
export const apiLimiter = rateLimit({
  windowMs: 60 * 1000, // 1 minute
  max: 300, // Limit each IP to 300 requests per minute
  message: {
    error: 'Too many API requests',
    message: 'API rate limit exceeded. Please try again later.',
    retry_after: 60
  },
  standardHeaders: true,
  legacyHeaders: false,
  keyGenerator: (req) => {
    return req.ip;
  },
  onLimitReached: (req) => {
    logger.warn('API rate limit exceeded', {
      ip: req.ip,
      userAgent: req.get('User-Agent'),
      path: req.path
    });
  }
});

// Strict rate limiter for authentication endpoints
export const authLimiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 10, // Limit each IP to 10 auth requests per 15 minutes
  message: {
    error: 'Too many authentication attempts',
    message: 'Please try again later.',
    retry_after: 900
  },
  standardHeaders: true,
  legacyHeaders: false,
  onLimitReached: (req) => {
    logger.warn('Auth rate limit exceeded', {
      ip: req.ip,
      userAgent: req.get('User-Agent'),
      path: req.path
    });
  }
});