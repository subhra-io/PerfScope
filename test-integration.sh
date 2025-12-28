#!/bin/bash

# PerfScope End-to-End Integration Test
# This script demonstrates the complete flow: SDK → Backend → Analytics

echo "🚀 PerfScope End-to-End Integration Test"
echo "========================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Check if backend is running
echo -e "\n${BLUE}1. Checking Backend Health...${NC}"
HEALTH_RESPONSE=$(curl -s http://localhost:3001/health)
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Backend is healthy${NC}"
    echo "$HEALTH_RESPONSE" | jq .
else
    echo -e "${RED}❌ Backend is not running. Please start it first:${NC}"
    echo "   cd backend && node test-server.js"
    exit 1
fi

# Clear previous events
echo -e "\n${BLUE}2. Clearing Previous Events...${NC}"
curl -s -X DELETE http://localhost:3001/api/events/clear | jq .

# Test Session Start Event
echo -e "\n${BLUE}3. Testing Session Start Event...${NC}"
SESSION_START='{
  "api_key": "demo-api-key-12345",
  "events": [
    {
      "event_type": "SESSION_START",
      "timestamp": '$(date +%s000)',
      "app_id": "io.perfscope.demo",
      "session_id": "integration-test-'$(date +%s)'",
      "screen": "MainActivity",
      "sdk_version": "1.0.0",
      "device": {
        "model": "Integration Test Device",
        "manufacturer": "Test",
        "android_version": 34,
        "ram_mb": 8192,
        "screen_density": 440,
        "screen_resolution": "1080x2400"
      },
      "build": {
        "version_name": "1.0.0-test",
        "version_code": 1,
        "build_type": "debug"
      }
    }
  ]
}'

RESPONSE=$(curl -s -X POST http://localhost:3001/api/events \
  -H "Content-Type: application/json" \
  -d "$SESSION_START")

if echo "$RESPONSE" | jq -e '.success' > /dev/null; then
    echo -e "${GREEN}✅ Session start event processed${NC}"
else
    echo -e "${RED}❌ Failed to process session start event${NC}"
    echo "$RESPONSE"
fi

# Test Memory Violation Event
echo -e "\n${BLUE}4. Testing Memory Violation Event...${NC}"
MEMORY_VIOLATION='{
  "api_key": "demo-api-key-12345",
  "events": [
    {
      "event_type": "MEMORY_VIOLATION",
      "timestamp": '$(date +%s000)',
      "app_id": "io.perfscope.demo",
      "session_id": "integration-test-'$(date +%s)'",
      "screen": "BitmapViolationTest",
      "violation_type": "BITMAP_SPIKE",
      "actual_mb": 45,
      "budget_mb": 10,
      "severity": "CRITICAL",
      "attribution": {
        "likely_cause": "BITMAP_HEAVY",
        "delta_mb": 45,
        "details": "Integration test: Large bitmap allocation detected"
      },
      "device": {
        "model": "Integration Test Device",
        "manufacturer": "Test",
        "android_version": 34,
        "ram_mb": 8192
      },
      "build": {
        "version_name": "1.0.0-test",
        "version_code": 1,
        "build_type": "debug"
      }
    }
  ]
}'

RESPONSE=$(curl -s -X POST http://localhost:3001/api/events \
  -H "Content-Type: application/json" \
  -d "$MEMORY_VIOLATION")

if echo "$RESPONSE" | jq -e '.success' > /dev/null; then
    echo -e "${GREEN}✅ Memory violation event processed${NC}"
else
    echo -e "${RED}❌ Failed to process memory violation event${NC}"
    echo "$RESPONSE"
fi

# Test Jank Violation Event
echo -e "\n${BLUE}5. Testing Jank Violation Event...${NC}"
JANK_VIOLATION='{
  "api_key": "demo-api-key-12345",
  "events": [
    {
      "event_type": "JANK_VIOLATION",
      "timestamp": '$(date +%s000)',
      "app_id": "io.perfscope.demo",
      "session_id": "integration-test-'$(date +%s)'",
      "screen": "MainThreadBlockTest",
      "violation_type": "SEVERE_JANK",
      "actual_value": 12.5,
      "budget_value": 2.0,
      "severity": "CRITICAL",
      "jank_type": "MAIN_THREAD_BLOCKING",
      "attribution": {
        "jank_percent": 12.5,
        "avg_frame_ms": 35.2,
        "frame_count": 120,
        "jank_frame_count": 15,
        "details": "Integration test: Main thread blocking detected"
      },
      "device": {
        "model": "Integration Test Device",
        "manufacturer": "Test",
        "android_version": 34,
        "ram_mb": 8192
      },
      "build": {
        "version_name": "1.0.0-test",
        "version_code": 1,
        "build_type": "debug"
      }
    }
  ]
}'

RESPONSE=$(curl -s -X POST http://localhost:3001/api/events \
  -H "Content-Type: application/json" \
  -d "$JANK_VIOLATION")

if echo "$RESPONSE" | jq -e '.success' > /dev/null; then
    echo -e "${GREEN}✅ Jank violation event processed${NC}"
else
    echo -e "${RED}❌ Failed to process jank violation event${NC}"
    echo "$RESPONSE"
fi

# Test Health Snapshot Event
echo -e "\n${BLUE}6. Testing Health Snapshot Event...${NC}"
HEALTH_SNAPSHOT='{
  "api_key": "demo-api-key-12345",
  "events": [
    {
      "event_type": "HEALTH_SNAPSHOT",
      "timestamp": '$(date +%s000)',
      "app_id": "io.perfscope.demo",
      "session_id": "integration-test-'$(date +%s)'",
      "screen": "MainActivity",
      "memory_mb": 85,
      "jank_percent": 1.2,
      "avg_frame_ms": 16.7,
      "cpu_percent": 15.3,
      "device": {
        "model": "Integration Test Device",
        "manufacturer": "Test",
        "android_version": 34,
        "ram_mb": 8192
      },
      "build": {
        "version_name": "1.0.0-test",
        "version_code": 1,
        "build_type": "debug"
      }
    }
  ]
}'

RESPONSE=$(curl -s -X POST http://localhost:3001/api/events \
  -H "Content-Type: application/json" \
  -d "$HEALTH_SNAPSHOT")

if echo "$RESPONSE" | jq -e '.success' > /dev/null; then
    echo -e "${GREEN}✅ Health snapshot event processed${NC}"
else
    echo -e "${RED}❌ Failed to process health snapshot event${NC}"
    echo "$RESPONSE"
fi

# Test Screen Change Event
echo -e "\n${BLUE}7. Testing Screen Change Event...${NC}"
SCREEN_CHANGE='{
  "api_key": "demo-api-key-12345",
  "events": [
    {
      "event_type": "SCREEN_CHANGE",
      "timestamp": '$(date +%s000)',
      "app_id": "io.perfscope.demo",
      "session_id": "integration-test-'$(date +%s)'",
      "screen": "ViolationTestScreen",
      "previous_screen": "MainActivity",
      "time_on_previous_screen_ms": 5420,
      "device": {
        "model": "Integration Test Device",
        "manufacturer": "Test",
        "android_version": 34,
        "ram_mb": 8192
      },
      "build": {
        "version_name": "1.0.0-test",
        "version_code": 1,
        "build_type": "debug"
      }
    }
  ]
}'

RESPONSE=$(curl -s -X POST http://localhost:3001/api/events \
  -H "Content-Type: application/json" \
  -d "$SCREEN_CHANGE")

if echo "$RESPONSE" | jq -e '.success' > /dev/null; then
    echo -e "${GREEN}✅ Screen change event processed${NC}"
else
    echo -e "${RED}❌ Failed to process screen change event${NC}"
    echo "$RESPONSE"
fi

# Get Analytics Summary
echo -e "\n${BLUE}8. Analytics Summary...${NC}"
ANALYTICS=$(curl -s http://localhost:3001/api/events/received)
echo "$ANALYTICS" | jq .

TOTAL_EVENTS=$(echo "$ANALYTICS" | jq -r '.total_events')
VIOLATIONS=$(echo "$ANALYTICS" | jq -r '.summary.violations')
SESSIONS=$(echo "$ANALYTICS" | jq -r '.summary.sessions')
SCREEN_CHANGES=$(echo "$ANALYTICS" | jq -r '.summary.screen_changes')
HEALTH_SNAPSHOTS=$(echo "$ANALYTICS" | jq -r '.summary.health_snapshots')

echo -e "\n${YELLOW}📊 Integration Test Results:${NC}"
echo -e "   Total Events: ${GREEN}$TOTAL_EVENTS${NC}"
echo -e "   Violations: ${RED}$VIOLATIONS${NC}"
echo -e "   Sessions: ${BLUE}$SESSIONS${NC}"
echo -e "   Screen Changes: ${BLUE}$SCREEN_CHANGES${NC}"
echo -e "   Health Snapshots: ${GREEN}$HEALTH_SNAPSHOTS${NC}"

# Test Invalid API Key
echo -e "\n${BLUE}9. Testing Security (Invalid API Key)...${NC}"
INVALID_REQUEST='{
  "api_key": "invalid-key",
  "events": [
    {
      "event_type": "SESSION_START",
      "timestamp": '$(date +%s000)',
      "app_id": "io.perfscope.demo",
      "session_id": "hack-attempt",
      "screen": "HackScreen"
    }
  ]
}'

RESPONSE=$(curl -s -X POST http://localhost:3001/api/events \
  -H "Content-Type: application/json" \
  -d "$INVALID_REQUEST")

if echo "$RESPONSE" | jq -e '.error' > /dev/null; then
    echo -e "${GREEN}✅ Security test passed - invalid API key rejected${NC}"
else
    echo -e "${RED}❌ Security test failed - invalid API key accepted${NC}"
fi

echo -e "\n${GREEN}🎉 End-to-End Integration Test Complete!${NC}"
echo -e "\n${YELLOW}Next Steps:${NC}"
echo -e "1. Run Android app with PerfScope SDK"
echo -e "2. Trigger violations in the demo app"
echo -e "3. Watch events appear in backend logs"
echo -e "4. Build web portal to visualize this data"
echo -e "\n${BLUE}Backend URLs:${NC}"
echo -e "   Health: http://localhost:3001/health"
echo -e "   Events: http://localhost:3001/api/events/received"
echo -e "   Clear:  curl -X DELETE http://localhost:3001/api/events/clear"