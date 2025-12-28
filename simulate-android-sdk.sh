#!/bin/bash

# Simulate Android SDK sending events to backend
# This demonstrates what happens when a user runs the demo app

echo "📱 Simulating Android SDK Event Flow"
echo "===================================="

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

SESSION_ID="android-demo-$(date +%s)"
API_KEY="demo-api-key-12345"

echo -e "${BLUE}📱 User opens PerfScope Demo App...${NC}"

# 1. Session Start
echo -e "\n${YELLOW}🎬 SDK: Session Started${NC}"
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

sleep 2

# 2. User navigates to violation test
echo -e "${YELLOW}🔄 SDK: Screen Change (MainActivity → BitmapViolationTest)${NC}"
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
      "time_on_previous_screen_ms": 3200,
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

sleep 1

# 3. User triggers bitmap violation
echo -e "${RED}🚨 SDK: Memory Violation Detected!${NC}"
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
      "actual_mb": 52,
      "budget_mb": 10,
      "severity": "CRITICAL",
      "attribution": {
        "likely_cause": "BITMAP_HEAVY",
        "delta_mb": 52,
        "details": "User triggered bitmap violation test - large bitmap allocations detected"
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

sleep 2

# 4. Health snapshot
echo -e "${GREEN}💚 SDK: Health Snapshot (30s interval)${NC}"
curl -s -X POST http://localhost:3001/api/events \
  -H "Content-Type: application/json" \
  -d '{
    "api_key": "'$API_KEY'",
    "events": [{
      "event_type": "HEALTH_SNAPSHOT",
      "timestamp": '$(date +%s000)',
      "app_id": "io.perfscope.demo",
      "session_id": "'$SESSION_ID'",
      "screen": "BitmapViolationTest",
      "memory_mb": 145,
      "jank_percent": 0.8,
      "avg_frame_ms": 16.2,
      "cpu_percent": 12.5,
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

sleep 1

# 5. User navigates to jank test
echo -e "${YELLOW}🔄 SDK: Screen Change (BitmapViolationTest → MainThreadBlockTest)${NC}"
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
      "time_on_previous_screen_ms": 8500,
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

sleep 1

# 6. User triggers jank violation
echo -e "${RED}🚨 SDK: Jank Violation Detected!${NC}"
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
      "actual_value": 15.2,
      "budget_value": 2.0,
      "severity": "CRITICAL",
      "jank_type": "MAIN_THREAD_BLOCKING",
      "attribution": {
        "jank_percent": 15.2,
        "avg_frame_ms": 42.8,
        "frame_count": 180,
        "jank_frame_count": 27,
        "details": "User triggered main thread block test - heavy main thread work detected"
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

sleep 2

# 7. User goes back to home
echo -e "${YELLOW}🔄 SDK: Screen Change (MainThreadBlockTest → MainActivity)${NC}"
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

sleep 1

# 8. Session end (user closes app)
echo -e "${YELLOW}🏁 SDK: Session Ended${NC}"
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
      "duration_ms": 45000,
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

echo -e "\n${GREEN}✅ Android SDK Simulation Complete!${NC}"
echo -e "\n${BLUE}📊 Session Summary:${NC}"
echo -e "   Session ID: $SESSION_ID"
echo -e "   Duration: ~45 seconds"
echo -e "   Screens: MainActivity → BitmapViolationTest → MainThreadBlockTest → MainActivity"
echo -e "   Violations: 2 (Memory + Jank)"
echo -e "   Events Sent: 8 total"

echo -e "\n${YELLOW}🔍 View Results:${NC}"
echo -e "   curl http://localhost:3001/api/events/received | jq ."