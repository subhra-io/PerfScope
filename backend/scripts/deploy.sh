#!/bin/bash

# PerfScope Backend Deployment Script

set -e

echo "🚀 PerfScope Backend Deployment"
echo "================================"

# Check if we're in the right directory
if [ ! -f "package.json" ]; then
    echo "❌ Error: Please run this script from the backend directory"
    exit 1
fi

# Check for required environment variables
if [ -z "$DATABASE_URL" ]; then
    echo "❌ Error: DATABASE_URL environment variable is required"
    exit 1
fi

if [ -z "$CORS_ORIGIN" ]; then
    echo "⚠️  Warning: CORS_ORIGIN not set, using default"
    export CORS_ORIGIN="http://localhost:3000"
fi

echo "📦 Installing dependencies..."
npm ci --only=production

echo "🔨 Building TypeScript..."
npm run build

echo "🗄️  Running database migrations..."
npm run db:migrate

echo "🌱 Seeding database (if needed)..."
npm run db:seed || echo "Seeding skipped (database may already be seeded)"

echo "🧪 Running health check..."
if npm run test:health; then
    echo "✅ Health check passed"
else
    echo "❌ Health check failed"
    exit 1
fi

echo "🎉 Deployment complete!"
echo ""
echo "🔗 API will be available at: $API_URL"
echo "📊 Health check: $API_URL/health"
echo "📚 API docs: $API_URL/docs"
echo ""
echo "🔧 Next steps:"
echo "  1. Update frontend NEXT_PUBLIC_API_URL"
echo "  2. Test Android SDK integration"
echo "  3. Monitor logs and metrics"
echo ""