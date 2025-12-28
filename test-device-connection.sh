#!/bin/bash

echo "🧪 Testing Device Connection"
echo "============================"

# Test if we can reach the backend from the device's perspective
LOCAL_IP=$(ifconfig | grep "inet " | grep -v 127.0.0.1 | head -1 | awk '{print $2}')
echo "📡 Testing connection to backend at $LOCAL_IP:3001"

# Test health endpoint
curl -s "http://$LOCAL_IP:3001/health" | jq .

echo ""
echo "✅ If you see the health response above, your device can reach the backend!"
echo "📱 Now interact with the PerfScope Demo app on your OnePlus device"
echo "🔍 Watch the monitoring terminal for live events"