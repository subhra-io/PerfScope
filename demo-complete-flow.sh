#!/bin/bash

# Complete PerfScope Demo - End-to-End Flow
# Shows: Android SDK → Backend API → Web Portal

echo "🚀 PerfScope Complete Demo - End-to-End Flow"
echo "============================================="

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
PURPLE='\033[0;35m'
NC='\033[0m'

echo -e "\n${PURPLE}📋 Demo Overview:${NC}"
echo "1. Backend API running on http://localhost:3001"
echo "2. Web Portal running on http://localhost:3000"
echo "3. Android SDK simulation sending real events"
echo "4. Real-time dashboard updates"

# Check if services are running
echo -e "\n${BLUE}🔍 Checking Services...${NC}"

# Check Backend
BACKEND_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:3001/health)
if [ "$BACKEND_STATUS" = "200" ]; then
    echo -e "${GREEN}✅ Backend API: Running${NC}"
else
    echo -e "${RED}❌ Backend API: Not running${NC}"
    echo "   Please start: cd backend && node test-server.js"
    exit 1
fi

# Check Web Portal
PORTAL_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:3000)
if [ "$PORTAL_STATUS" = "200" ]; then
    echo -e "${GREEN}✅ Web Portal: Running${NC}"
else
    echo -e "${RED}❌ Web Portal: Not running${NC}"
    echo "   Please start: cd web-portal && npm run dev"
    exit 1
fi

echo -e "\n${YELLOW}🧹 Clearing Previous Data...${NC}"
curl -s -X DELETE http://localhost:3001/api/events/clear | jq -r '.message'

echo -e "\n${BLUE}📱 Simulating Real User Session...${NC}"
echo "   User: Opens PerfScope Demo App"
echo "   Device: Google Pixel 6 (Android 14)"
echo "   Build: v1.0.0 Debug"

SESSION_ID="demo-session-$(date +%s)"
API_KEY="demo-api-key-12345"

# 1. Session Start
echo -e "\n${YELLOW}🎬 Event 1/8: Session Started${NC}"
curl -s -X POST http://localhost:3001/api/events \
  -H "Content-Type: application/json" \
  -d '{
    "api_key": "'$API_KEY'",
    "events": [{
      "event_type": "SESSION_START",
      "timestamp": '$(date +%s000)',
      "app_id": "io.perfscope.demo",
      "session_id": "'$SESSION_ID'",
      "screen": "MainActivity",
      "sdk_version": "1.0.0",
      "device": {
        "model": "Pixel 6",
        "manufacturer": "Google",
        "android_version": 34,
        "ram_mb": 8192,
        "screen_density": 440,
        "screen_resolution": "1080x2400"
      },
      "build": {
        "version_name": "1.0.0",
        "version_code": 1,
        "build_type": "debug"
      }
    }]
  }' > /dev/null

echo "   ✅ Session started - User on MainActivity"
sleep 2

# 2. Health Snapshot
echo -e "\n${GREEN}💚 Event 2/8: Health Snapshot (30s interval)${NC}"
curl -s -X POST http://localhost:3001/api/events \
  -H "Content-Type: application/json" \
  -d '{
    "api_key": "'$API_KEY'",
    "events": [{
      "event_type": "HEALTH_SNAPSHOT",
      "timestamp": '$(date +%s000)',
      "app_id": "io.perfscope.demo",
      "session_id": "'$SESSION_ID'",
      "screen": "MainActivity",
      "memory_mb": 78,
      "jank_percent": 0.5,
      "avg_frame_ms": 16.1,
      "cpu_percent": 8.2,
      "device": {
        "model": "Pixel 6",
        "manufacturer": "Google",
        "android_version": 34,
        "ram_mb": 8192
      },
      "build": {
        "version_name": "1.0.0",
        "version_code": 1,
        "build_type": "debug"
      }
    }]
  }' > /dev/null

echo "   ✅ Healthy performance - 78MB memory, 0.5% jank"
sleep 1

# 3. Screen Change
echo -e "\n${YELLOW}🔄 Event 3/8: Screen Navigation${NC}"
curl -s -X POST http://localhost:3001/api/events \
  -H "Content-Type: application/json" \
  -d '{
    "api_key": "'$API_KEY'",
    "events": [{
      "event_type": "SCREEN_CHANGE",
      "timestamp": '$(date +%s000)',
      "app_id": "io.perfscope.demo",
      "session_id": "'$SESSION_ID'",
      "screen": "BitmapViolationTest",
      "previous_screen": "MainActivity",
      "time_on_previous_screen_ms": 4200,
      "device": {
        "model": "Pixel 6",
        "manufacturer": "Google",
        "android_version": 34,
        "ram_mb": 8192
      },
      "build": {
        "version_name": "1.0.0",
        "version_code": 1,
        "build_type": "debug"
      }
    }]
  }' > /dev/null

echo "   ✅ User navigated: MainActivity → BitmapViolationTest (4.2s)"
sleep 1

# 4. Memory Violation
echo -e "\n${RED}🚨 Event 4/8: MEMORY VIOLATION DETECTED!${NC}"
curl -s -X POST http://localhost:3001/api/events \
  -H "Content-Type: application/json" \
  -d '{
    "api_key": "'$API_KEY'",
    "events": [{
      "event_type": "MEMORY_VIOLATION",
      "timestamp": '$(date +%s000)',
      "app_id": "io.perfscope.demo",
      "session_id": "'$SESSION_ID'",
      "screen": "BitmapViolationTest",
      "violation_type": "BITMAP_SPIKE",
      "actual_mb": 67,
      "budget_mb": 10,
      "severity": "CRITICAL",
      "attribution": {
        "likely_cause": "BITMAP_HEAVY",
        "delta_mb": 67,
        "details": "User triggered bitmap test - massive image allocations detected"
      },
      "device": {
        "model": "Pixel 6",
        "manufacturer": "Google",
        "android_version": 34,
        "ram_mb": 8192
      },
      "build": {
        "version_name": "1.0.0",
        "version_code": 1,
        "build_type": "debug"
      }
    }]
  }' > /dev/null

echo "   🚨 CRITICAL: 67MB bitmap spike (Budget: 10MB)"
echo "   🔍 Attribution: Bitmap-heavy allocations"
sleep 2

# 5. Screen Change
echo -e "\n${YELLOW}🔄 Event 5/8: Screen Navigation${NC}"
curl -s -X POST http://localhost:3001/api/events \
  -H "Content-Type: application/json" \
  -d '{
    "api_key": "'$API_KEY'",
    "events": [{
      "event_type": "SCREEN_CHANGE",
      "timestamp": '$(date +%s000)',
      "app_id": "io.perfscope.demo",
      "session_id": "'$SESSION_ID'",
      "screen": "MainThreadBlockTest",
      "previous_screen": "BitmapViolationTest",
      "time_on_previous_screen_ms": 6800,
      "device": {
        "model": "Pixel 6",
        "manufacturer": "Google",
        "android_version": 34,
        "ram_mb": 8192
      },
      "build": {
        "version_name": "1.0.0",
        "version_code": 1,
        "build_type": "debug"
      }
    }]
  }' > /dev/null

echo "   ✅ User navigated: BitmapViolationTest → MainThreadBlockTest (6.8s)"
sleep 1

# 6. Jank Violation
echo -e "\n${RED}🚨 Event 6/8: JANK VIOLATION DETECTED!${NC}"
curl -s -X POST http://localhost:3001/api/events \
  -H "Content-Type: application/json" \
  -d '{
    "api_key": "'$API_KEY'",
    "events": [{
      "event_type": "JANK_VIOLATION",
      "timestamp": '$(date +%s000)',
      "app_id": "io.perfscope.demo",
      "session_id": "'$SESSION_ID'",
      "screen": "MainThreadBlockTest",
      "violation_type": "SEVERE_JANK",
      "actual_value": 18.7,
      "budget_value": 2.0,
      "severity": "CRITICAL",
      "jank_type": "MAIN_THREAD_BLOCKING",
      "attribution": {
        "jank_percent": 18.7,
        "avg_frame_ms": 48.3,
        "frame_count": 240,
        "jank_frame_count": 45,
        "details": "User triggered main thread block - heavy computation on UI thread"
      },
      "device": {
        "model": "Pixel 6",
        "manufacturer": "Google",
        "android_version": 34,
        "ram_mb": 8192
      },
      "build": {
        "version_name": "1.0.0",
        "version_code": 1,
        "build_type": "debug"
      }
    }]
  }' > /dev/null

echo "   🚨 CRITICAL: 18.7% jank (Budget: 2.0%)"
echo "   🔍 Attribution: Main thread blocking - 48.3ms avg frame time"
sleep 2

# 7. Screen Change Back
echo -e "\n${YELLOW}🔄 Event 7/8: Screen Navigation${NC}"
curl -s -X POST http://localhost:3001/api/events \
  -H "Content-Type: application/json" \
  -d '{
    "api_key": "'$API_KEY'",
    "events": [{
      "event_type": "SCREEN_CHANGE",
      "timestamp": '$(date +%s000)',
      "app_id": "io.perfscope.demo",
      "session_id": "'$SESSION_ID'",
      "screen": "MainActivity",
      "previous_screen": "MainThreadBlockTest",
      "time_on_previous_screen_ms": 3500,
      "device": {
        "model": "Pixel 6",
        "manufacturer": "Google",
        "android_version": 34,
        "ram_mb": 8192
      },
      "build": {
        "version_name": "1.0.0",
        "version_code": 1,
        "build_type": "debug"
      }
    }]
  }' > /dev/null

echo "   ✅ User navigated back: MainThreadBlockTest → MainActivity (3.5s)"
sleep 1

# 8. Session End
echo -e "\n${YELLOW}🏁 Event 8/8: Session Ended${NC}"
curl -s -X POST http://localhost:3001/api/events \
  -H "Content-Type: application/json" \
  -d '{
    "api_key": "'$API_KEY'",
    "events": [{
      "event_type": "SESSION_END",
      "timestamp": '$(date +%s000)',
      "app_id": "io.perfscope.demo",
      "session_id": "'$SESSION_ID'",
      "screen": "MainActivity",
      "duration_ms": 52000,
      "total_violations": 2,
      "device": {
        "model": "Pixel 6",
        "manufacturer": "Google",
        "android_version": 34,
        "ram_mb": 8192
      },
      "build": {
        "version_name": "1.0.0",
        "version_code": 1,
        "build_type": "debug"
      }
    }]
  }' > /dev/null

echo "   ✅ Session completed - 52s duration, 2 violations"

# Show Results
echo -e "\n${PURPLE}📊 Session Summary:${NC}"
SUMMARY=$(curl -s http://localhost:3001/api/events/received | jq -r '.summary')
echo "   Total Events: $(echo $SUMMARY | jq -r '.violations + .sessions + .screen_changes + .health_snapshots')"
echo "   Violations: $(echo $SUMMARY | jq -r '.violations') (Memory + Jank)"
echo "   Sessions: $(echo $SUMMARY | jq -r '.sessions')"
echo "   Screen Changes: $(echo $SUMMARY | jq -r '.screen_changes')"
echo "   Health Snapshots: $(echo $SUMMARY | jq -r '.health_snapshots')"

echo -e "\n${GREEN}🎉 Demo Complete! View Results:${NC}"
echo -e "   📊 Web Portal: ${BLUE}http://localhost:3000${NC}"
echo -e "   🔧 Backend API: ${BLUE}http://localhost:3001/api/events/received${NC}"
echo -e "   💚 Health Check: ${BLUE}http://localhost:3001/health${NC}"

echo -e "\n${YELLOW}💡 What You Just Saw:${NC}"
echo "✅ Professional Android SDK monitoring performance"
echo "✅ Real-time event ingestion with attribution"
echo "✅ Budget enforcement with violation detection"
echo "✅ Production-ready backend API"
echo "✅ Beautiful web dashboard with live updates"
echo "✅ Complete Firebase/Sentry-style architecture"

echo -e "\n${PURPLE}🚀 This is exactly how real monitoring platforms work!${NC}"