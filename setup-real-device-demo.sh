#!/bin/bash

# Complete Real Device Demo Setup
echo "🚀 PerfScope Real Device Demo Setup"
echo "===================================="

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
PURPLE='\033[0;35m'
NC='\033[0m'

# Check device connection
DEVICE_ID=$(adb devices | grep -v "List of devices" | grep "device" | awk '{print $1}')
if [ -z "$DEVICE_ID" ]; then
    echo -e "${RED}❌ No Android device connected${NC}"
    echo "Please connect your device and enable USB debugging"
    exit 1
fi

echo -e "${GREEN}✅ Device Connected: $DEVICE_ID${NC}"

# Get device info
MODEL=$(adb shell getprop ro.product.model)
MANUFACTURER=$(adb shell getprop ro.product.manufacturer)
ANDROID_VERSION=$(adb shell getprop ro.build.version.release)
MEMINFO=$(adb shell cat /proc/meminfo | grep MemTotal | awk '{print $2}')
MEMORY_MB=$((MEMINFO / 1024))

echo -e "${BLUE}📱 Device: $MANUFACTURER $MODEL${NC}"
echo -e "${BLUE}🤖 Android: $ANDROID_VERSION${NC}"
echo -e "${BLUE}💾 RAM: ${MEMORY_MB}MB${NC}"

# Check if backend is running
echo -e "\n${YELLOW}🔍 Checking Backend Status...${NC}"
BACKEND_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:3001/health)
if [ "$BACKEND_STATUS" != "200" ]; then
    echo -e "${RED}❌ Backend not running${NC}"
    echo "Please start the backend first:"
    echo "   cd backend && node test-server.js"
    exit 1
fi
echo -e "${GREEN}✅ Backend running on http://localhost:3001${NC}"

# Check if web portal is running
echo -e "\n${YELLOW}🔍 Checking Web Portal Status...${NC}"
PORTAL_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:3000)
if [ "$PORTAL_STATUS" != "200" ]; then
    echo -e "${RED}❌ Web Portal not running${NC}"
    echo "Please start the web portal first:"
    echo "   cd web-portal && npm run dev"
    exit 1
fi
echo -e "${GREEN}✅ Web Portal running on http://localhost:3000${NC}"

# Check if app is installed
echo -e "\n${YELLOW}🔍 Checking App Installation...${NC}"
APP_INSTALLED=$(adb shell pm list packages | grep "io.perfscope.demo")
if [ -z "$APP_INSTALLED" ]; then
    echo -e "${YELLOW}⚠️  App not installed, installing now...${NC}"
    ./gradlew installDebug
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ App installed successfully${NC}"
    else
        echo -e "${RED}❌ Failed to install app${NC}"
        exit 1
    fi
else
    echo -e "${GREEN}✅ App already installed${NC}"
fi

# Clear previous data
echo -e "\n${YELLOW}🧹 Clearing previous data...${NC}"
curl -s -X DELETE http://localhost:3001/api/events/clear | jq -r '.message'

echo -e "\n${PURPLE}🎉 Setup Complete!${NC}"
echo -e "\n${BLUE}📋 Next Steps:${NC}"
echo "1. Open the PerfScope Demo app on your $MANUFACTURER $MODEL"
echo "2. The app will automatically start sending events to the backend"
echo "3. Monitor live events in terminal: ./monitor-real-device.sh"
echo "4. View the web dashboard: http://localhost:3000"
echo "5. Try triggering violations in the app to see real-time alerts"

echo -e "\n${GREEN}🔗 Quick Links:${NC}"
echo "   📊 Dashboard: http://localhost:3000"
echo "   🔧 API Events: http://localhost:3001/api/events/received"
echo "   💚 Health: http://localhost:3001/health"

echo -e "\n${YELLOW}💡 Pro Tip:${NC}"
echo "Open the dashboard in your browser and the monitoring script in terminal"
echo "to see real-time data flowing from your device!"

# Launch app on device
echo -e "\n${BLUE}🚀 Launching app on device...${NC}"
adb shell am start -n io.perfscope.demo/.MainActivity
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ App launched on your device${NC}"
    echo -e "\n${PURPLE}🎯 The PerfScope Demo app is now running on your $MANUFACTURER $MODEL!${NC}"
    echo "Watch for events in the backend logs and web dashboard."
else
    echo -e "${YELLOW}⚠️  Please manually open the PerfScope Demo app on your device${NC}"
fi