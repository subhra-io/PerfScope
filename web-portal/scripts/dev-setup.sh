#!/bin/bash

# PerfScope Web Portal - Development Setup Script

echo "🚀 Setting up PerfScope Web Portal for development..."

# Check if we're in the right directory
if [ ! -f "package.json" ]; then
    echo "❌ Error: Please run this script from the web-portal directory"
    exit 1
fi

# Install dependencies
echo "📦 Installing dependencies..."
npm install

# Copy environment file if it doesn't exist
if [ ! -f ".env.local" ]; then
    echo "📝 Creating .env.local file..."
    cp .env.example .env.local
    echo "✅ Created .env.local - update with your backend URLs"
else
    echo "✅ .env.local already exists"
fi

# Check if backend is running
echo "🔍 Checking backend connection..."
if curl -s http://localhost:3001/health > /dev/null 2>&1; then
    echo "✅ Backend is running on http://localhost:3001"
else
    echo "⚠️  Backend not detected on http://localhost:3001"
    echo "   Start your backend first: cd ../backend && npm run dev"
fi

echo ""
echo "🎉 Setup complete! Run the following commands:"
echo ""
echo "  npm run dev     # Start development server"
echo "  npm run build   # Build for production"
echo "  npm run start   # Start production server"
echo ""
echo "📊 Dashboard will be available at: http://localhost:3000"
echo ""
echo "🔧 Next steps:"
echo "  1. Start the backend API (see ../backend/README.md)"
echo "  2. Run 'npm run dev' to start the dashboard"
echo "  3. Connect your Android app with PerfScope SDK"
echo ""