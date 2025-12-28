#!/bin/bash

# Real Device Monitoring Script
# Monitors live events from your OnePlus CPH2691 device

echo "📱 PerfScope Real Device Monitoring"
echo "===================================="
echo "Device: OnePlus CPH2691 (def93dee)"
echo "Android: 15 (API 35)"
echo "RAM: 11GB"
echo "Backend: http://localhost:3001"
echo "Dashboard: http://localhost:3000"
echo ""

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
PURPLE='\033[0;35m'
NC='\033[0m'

# Clear previous events
echo -e "${YELLOW}🧹 Clearing previous test data...${NC}"
curl -s -X DELETE http://localhost:3001/api/events/clear | jq -r '.message'

echo -e "\n${GREEN}✅ Ready for live monitoring!${NC}"
echo -e "\n${PURPLE}📋 Instructions:${NC}"
echo "1. Open the PerfScope Demo app on your OnePlus device"
echo "2. Tap 'Show PerfScope' to see the SDK overlay"
echo "3. Try the violation test buttons:"
echo "   • 🚨 Trigger Bitmap Violation"
echo "   • 🚨 Trigger Collection Violation" 
echo "   • 🚨 Trigger Main Thread Block"
echo "   • 🚨 Trigger Layout Thrash"
echo "4. Watch this terminal for live events"
echo "5. Check the web dashboard: http://localhost:3000"

echo -e "\n${BLUE}⏳ Waiting for events from your OnePlus device...${NC}"
echo "   (Events will appear here in real-time)"

# Monitor events in real-time
LAST_COUNT=0
while true; do
    sleep 2
    
    # Get current event count
    RESPONSE=$(curl -s http://localhost:3001/api/events/received)
    if [ $? -eq 0 ]; then
        CURRENT_COUNT=$(echo "$RESPONSE" | jq -r '.total_events // 0')
        
        if [ "$CURRENT_COUNT" -gt "$LAST_COUNT" ]; then
            NEW_EVENTS=$((CURRENT_COUNT - LAST_COUNT))
            echo -e "\n${GREEN}🔔 $NEW_EVENTS new event(s) received! Total: $CURRENT_COUNT${NC}"
            
            # Show latest events
            echo "$RESPONSE" | jq -r '.events[0:3][] | "   📊 \(.event_type) on \(.screen) - \(.timestamp | strftime("%H:%M:%S"))"' 2>/dev/null || echo "   📊 New events received"
            
            # Show summary
            SUMMARY=$(echo "$RESPONSE" | jq -r '.summary')
            VIOLATIONS=$(echo "$SUMMARY" | jq -r '.violations // 0')
            SESSIONS=$(echo "$SUMMARY" | jq -r '.sessions // 0')
            
            if [ "$VIOLATIONS" -gt 0 ]; then
                echo -e "   ${RED}🚨 $VIOLATIONS violation(s) detected${NC}"
            fi
            
            if [ "$SESSIONS" -gt 0 ]; then
                echo -e "   ${BLUE}👤 $SESSIONS session(s) active${NC}"
            fi
            
            LAST_COUNT=$CURRENT_COUNT
        fi
    fi
done