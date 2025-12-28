# PerfScope Backend - Deployment Guide

This guide covers deploying the PerfScope backend API to various cloud platforms.

## 🚀 Quick Deploy Options

### Railway (Recommended)
[![Deploy on Railway](https://railway.app/button.svg)](https://railway.app/template/perfscope-backend)

### Render
[![Deploy to Render](https://render.com/images/deploy-to-render-button.svg)](https://render.com/deploy?repo=https://github.com/subhra-io/PerfScope)

## 📋 Prerequisites

- **Database**: PostgreSQL 15+ (managed database recommended)
- **Node.js**: 18+ 
- **Environment Variables**: See `.env.example`

## 🛠️ Platform-Specific Deployments

### 1. Railway Deployment (Recommended)

Railway provides excellent PostgreSQL integration and automatic deployments.

**Steps:**
1. Fork the repository
2. Connect to [Railway](https://railway.app)
3. Create new project from GitHub repo
4. Add PostgreSQL service
5. Set environment variables:
   ```bash
   NODE_ENV=production
   DATABASE_URL=${{Postgres.DATABASE_URL}}
   CORS_ORIGIN=https://your-frontend-domain.vercel.app
   ```
6. Deploy automatically triggers

**Features:**
- ✅ Automatic deployments from GitHub
- ✅ Managed PostgreSQL database
- ✅ Built-in monitoring and logs
- ✅ Custom domains
- ✅ Environment variable management

### 2. Render Deployment

Render offers great free tier and managed databases.

**Steps:**
1. Fork the repository
2. Connect to [Render](https://render.com)
3. Create PostgreSQL database
4. Create web service from repo
5. Configure build/start commands:
   - **Build**: `npm ci && npm run build`
   - **Start**: `npm start`
6. Set environment variables (see render.yaml)

**Features:**
- ✅ Free tier available
- ✅ Managed PostgreSQL
- ✅ Auto-deploy from GitHub
- ✅ SSL certificates
- ✅ Health checks

### 3. Vercel (Serverless)

Deploy as serverless functions for automatic scaling.

**Steps:**
1. Install Vercel CLI: `npm i -g vercel`
2. From backend directory: `vercel`
3. Set environment variables in Vercel dashboard
4. Connect external PostgreSQL (Supabase, Neon, etc.)

**Features:**
- ✅ Automatic scaling
- ✅ Global edge network
- ✅ Zero cold starts
- ⚠️ Requires external database

### 4. Docker Deployment

Deploy using Docker on any cloud provider.

**Steps:**
1. Build image: `docker build -t perfscope-backend .`
2. Push to registry (Docker Hub, ECR, etc.)
3. Deploy to cloud provider
4. Set environment variables
5. Connect to managed PostgreSQL

**Supported Platforms:**
- AWS ECS/Fargate
- Google Cloud Run
- Azure Container Instances
- DigitalOcean App Platform

## 🔧 Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `NODE_ENV` | Environment | `production` |
| `PORT` | Server port | `3001` |
| `DATABASE_URL` | PostgreSQL connection | `postgresql://user:pass@host:5432/db` |
| `CORS_ORIGIN` | Frontend URL | `https://app.perfscope.com` |
| `JWT_SECRET` | JWT signing key | `your-secret-key` |
| `API_KEY` | API authentication | `your-api-key` |
| `LOG_LEVEL` | Logging level | `info` |

## 🗄️ Database Setup

### Managed Database (Recommended)

**Railway PostgreSQL:**
- Automatically provisioned
- Connection string provided
- Backups included

**Render PostgreSQL:**
- Free tier: 1GB storage
- Automatic backups
- Connection pooling

**Supabase:**
- Generous free tier
- Real-time features
- Built-in auth

**Neon:**
- Serverless PostgreSQL
- Automatic scaling
- Branching support

### Manual Database Setup

If using your own PostgreSQL:

```sql
-- Create database
CREATE DATABASE perfscope;

-- Create user
CREATE USER perfscope WITH PASSWORD 'secure_password';

-- Grant permissions
GRANT ALL PRIVILEGES ON DATABASE perfscope TO perfscope;

-- Run schema
\i src/database/schema.sql
```

## 🔍 Health Checks

The API includes health check endpoints:

- `GET /health` - Basic health check
- `GET /health/db` - Database connectivity
- `GET /health/detailed` - Full system status

Configure your platform's health checks to use `/health`.

## 📊 Monitoring

### Built-in Logging
- Winston logger with structured logs
- Request/response logging
- Error tracking
- Performance metrics

### Platform Monitoring
- **Railway**: Built-in metrics and logs
- **Render**: Application metrics
- **Vercel**: Function analytics

### External Monitoring
- Sentry for error tracking
- DataDog for APM
- New Relic for performance

## 🔒 Security

### Production Checklist
- ✅ HTTPS enabled
- ✅ CORS configured
- ✅ Rate limiting active
- ✅ Helmet security headers
- ✅ Environment variables secured
- ✅ Database credentials rotated
- ✅ API keys generated

### Security Headers
```javascript
// Automatically applied via Helmet
X-Frame-Options: DENY
X-Content-Type-Options: nosniff
X-XSS-Protection: 1; mode=block
Strict-Transport-Security: max-age=31536000
```

## 🚀 Performance Optimization

### Database
- Connection pooling enabled
- Query optimization
- Indexes on frequently queried columns
- Regular VACUUM and ANALYZE

### API
- Response compression (gzip)
- Request rate limiting
- Caching headers
- Efficient JSON serialization

### Scaling
- Horizontal scaling supported
- Stateless design
- Database connection pooling
- Load balancer ready

## 🔧 Troubleshooting

### Common Issues

**Database Connection Failed**
```bash
# Check DATABASE_URL format
postgresql://username:password@host:port/database

# Verify database is accessible
pg_isready -h host -p port -U username
```

**CORS Errors**
```bash
# Set correct frontend URL
CORS_ORIGIN=https://your-frontend-domain.com

# Multiple origins (comma-separated)
CORS_ORIGIN=https://app.com,https://staging.app.com
```

**Memory Issues**
```bash
# Increase Node.js memory limit
NODE_OPTIONS=--max-old-space-size=2048
```

### Debugging

**Enable Debug Logs**
```bash
LOG_LEVEL=debug
```

**Database Query Logging**
```bash
DB_LOGGING=true
```

**Performance Profiling**
```bash
NODE_ENV=development
ENABLE_PROFILING=true
```

## 📈 Scaling

### Horizontal Scaling
- Deploy multiple instances
- Use load balancer
- Session-less design
- Database connection pooling

### Database Scaling
- Read replicas for queries
- Connection pooling
- Query optimization
- Caching layer (Redis)

### CDN Integration
- Static asset delivery
- API response caching
- Geographic distribution

## 🔄 CI/CD

### GitHub Actions
```yaml
# .github/workflows/deploy.yml
name: Deploy Backend
on:
  push:
    branches: [main]
    paths: ['backend/**']

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Deploy to Railway
        uses: railway/deploy@v1
        with:
          token: ${{ secrets.RAILWAY_TOKEN }}
```

### Automated Testing
- Unit tests with Jest
- Integration tests
- Database migration tests
- API endpoint tests

---

## 🎯 Next Steps

1. **Deploy Backend**: Choose your platform and deploy
2. **Configure Database**: Set up PostgreSQL and run migrations
3. **Update Frontend**: Set `NEXT_PUBLIC_API_URL` in web dashboard
4. **Test Integration**: Verify Android SDK → Backend → Dashboard flow
5. **Monitor**: Set up logging and monitoring
6. **Scale**: Add caching and optimize as needed

**Support**: Check deployment logs and health endpoints for troubleshooting.