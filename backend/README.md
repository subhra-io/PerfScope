# PerfScope Backend API

Production-ready backend for PerfScope performance monitoring platform. Built with Node.js, TypeScript, PostgreSQL, and designed for high-throughput event ingestion.

## 🚀 **Quick Start**

### Prerequisites
- Node.js 18+
- PostgreSQL 15+
- Docker & Docker Compose (optional)

### Development Setup

1. **Clone and Install**
```bash
cd backend
npm install
```

2. **Environment Configuration**
```bash
cp .env.example .env
# Edit .env with your database credentials
```

3. **Database Setup**
```bash
# Option 1: Using Docker Compose (Recommended)
docker-compose up -d postgres redis

# Option 2: Local PostgreSQL
createdb perfscope
```

4. **Run Migrations & Seed Data**
```bash
npm run db:migrate
npm run db:seed
```

5. **Start Development Server**
```bash
npm run dev
```

The API will be available at `http://localhost:3001`

### Docker Setup (Complete Stack)

```bash
# Start all services (PostgreSQL, Redis, Backend, pgAdmin)
docker-compose up -d

# View logs
docker-compose logs -f backend

# Stop services
docker-compose down
```

## 📊 **API Endpoints**

### Event Ingestion
```http
POST /api/events
Content-Type: application/json

{
  "api_key": "ps_your_api_key_here",
  "events": [
    {
      "event_type": "MEMORY_VIOLATION",
      "timestamp": 1703123456789,
      "app_id": "io.perfscope.demo",
      "session_id": "uuid-here",
      "screen": "HomeFeed",
      "violation_type": "BITMAP_SPIKE",
      "actual_mb": 32,
      "budget_mb": 20,
      "severity": "CRITICAL",
      "device": { ... },
      "build": { ... },
      "attribution": { ... }
    }
  ]
}
```

### Analytics Endpoints
```http
# Get app statistics
GET /api/apps/{appId}/events/stats?hours=24

# Get sessions (planned)
GET /api/apps/{appId}/sessions

# Get violations (planned)
GET /api/apps/{appId}/violations

# Get metrics (planned)
GET /api/apps/{appId}/metrics
```

### Health Check
```http
GET /health
```

## 🏗️ **Architecture**

### Database Schema
- **apps**: Mobile applications using PerfScope SDK
- **sessions**: User sessions within apps
- **events**: All performance events (violations, health snapshots)
- **screen_changes**: Navigation flow analysis
- **violation_summaries**: Pre-aggregated data for dashboards
- **health_metrics**: Time-series performance data

### Key Features
- **High-throughput event ingestion** with batching
- **Real-time violation detection** and aggregation
- **Time-series data storage** for analytics
- **Rate limiting** and security middleware
- **Structured logging** with Winston
- **Type-safe validation** with Zod schemas
- **Production-ready** error handling and monitoring

### Performance Optimizations
- **JSONB columns** for flexible event storage
- **Composite indexes** for fast analytics queries
- **Pre-aggregated summaries** for dashboard performance
- **Connection pooling** for database efficiency
- **Compression middleware** for reduced bandwidth

## 🔧 **Configuration**

### Environment Variables
```bash
# Server
NODE_ENV=development
PORT=3001
LOG_LEVEL=info

# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=perfscope
DB_USER=perfscope
DB_PASSWORD=perfscope_dev

# Security
ALLOWED_ORIGINS=http://localhost:3000
```

### Rate Limiting
- **Event ingestion**: 100 requests/minute per API key
- **API endpoints**: 300 requests/minute per IP
- **Auth endpoints**: 10 requests/15 minutes per IP

## 📈 **Monitoring & Observability**

### Logging
- **Structured JSON logging** with Winston
- **Request/response logging** with timing
- **Error tracking** with stack traces
- **Performance metrics** logging

### Health Checks
- **Database connectivity** monitoring
- **Application health** endpoint
- **Docker health checks** configured

### Metrics (Planned)
- Event ingestion rate
- Database query performance
- API response times
- Error rates by endpoint

## 🧪 **Testing**

```bash
# Run tests
npm test

# Run with coverage
npm run test:coverage

# Lint code
npm run lint

# Format code
npm run format
```

## 🚀 **Deployment**

### Docker Production
```bash
# Build production image
docker build -t perfscope-backend .

# Run with production config
docker run -d \
  --name perfscope-backend \
  -p 3001:3001 \
  -e NODE_ENV=production \
  -e DB_HOST=your-db-host \
  perfscope-backend
```

### Environment-Specific Configs
- **Development**: Full logging, relaxed rate limits
- **Staging**: Production-like with debug enabled
- **Production**: Optimized performance, security hardened

## 📝 **API Key Management**

After seeding the database, you'll get API keys for testing:

```
Demo App (io.perfscope.demo): ps_abc123...
Test App (com.example.testapp): ps_def456...
```

Use these in your Android SDK configuration:
```kotlin
val config = PerfScopeConfig.withExport(
    endpoint = "http://localhost:3001/api/events",
    apiKey = "ps_abc123..."
)
```

## 🔒 **Security Features**

- **Helmet.js** security headers
- **CORS** configuration
- **Rate limiting** per API key and IP
- **Input validation** with Zod schemas
- **SQL injection** prevention with parameterized queries
- **Request size limits** to prevent DoS

## 📊 **Database Management**

### pgAdmin Access
- URL: `http://localhost:5050`
- Email: `admin@perfscope.io`
- Password: `admin123`

### Common Queries
```sql
-- View recent violations
SELECT * FROM recent_violations LIMIT 10;

-- App performance summary
SELECT * FROM app_performance_summary;

-- Active sessions
SELECT * FROM active_sessions;
```

## 🤝 **Contributing**

1. Follow TypeScript best practices
2. Add tests for new features
3. Update documentation
4. Use conventional commits
5. Ensure Docker builds pass

## 📄 **License**

MIT License - see LICENSE file for details.