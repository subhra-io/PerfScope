#!/bin/bash

# Get Real Device Information
echo "📱 Connected Android Device Information"
echo "======================================"

DEVICE_ID=$(adb devices | grep -v "List of devices" | grep "device" | awk '{print $1}')

if [ -z "$DEVICE_ID" ]; then
    echo "❌ No Android device connected"
    echo "Please connect your device and enable USB debugging"
    exit 1
fi

echo "🔍 Device ID: $DEVICE_ID"

# Get device properties
echo ""
echo "📊 Device Details:"
echo "  Model: $(adb shell getprop ro.product.model)"
echo "  Manufacturer: $(adb shell getprop ro.product.manufacturer)"
echo "  Brand: $(adb shell getprop ro.product.brand)"
echo "  Android Version: $(adb shell getprop ro.build.version.release)"
echo "  API Level: $(adb shell getprop ro.build.version.sdk)"
echo "  Build ID: $(adb shell getprop ro.build.id)"
echo "  Hardware: $(adb shell getprop ro.hardware)"

# Get memory info
echo ""
echo "💾 Memory Information:"
MEMINFO=$(adb shell cat /proc/meminfo | grep MemTotal | awk '{print $2}')
MEMORY_MB=$((MEMINFO / 1024))
echo "  Total RAM: ${MEMORY_MB}MB"

# Get screen info
echo ""
echo "📺 Display Information:"
DENSITY=$(adb shell wm density | awk '{print $3}')
SIZE=$(adb shell wm size | awk '{print $3}')
echo "  Screen Density: $DENSITY"
echo "  Screen Size: $SIZE"

# Get CPU info
echo ""
echo "🔧 CPU Information:"
echo "  CPU ABI: $(adb shell getprop ro.product.cpu.abi)"
echo "  CPU Cores: $(adb shell cat /proc/cpuinfo | grep processor | wc -l)"

# Create device info JSON for backend
echo ""
echo "📝 Creating device configuration..."

cat > device-info.json << EOF
{
  "device_id": "$DEVICE_ID",
  "model": "$(adb shell getprop ro.product.model)",
  "manufacturer": "$(adb shell getprop ro.product.manufacturer)",
  "brand": "$(adb shell getprop ro.product.brand)",
  "android_version": $(adb shell getprop ro.build.version.sdk),
  "android_release": "$(adb shell getprop ro.build.version.release)",
  "build_id": "$(adb shell getprop ro.build.id)",
  "hardware": "$(adb shell getprop ro.hardware)",
  "ram_mb": $MEMORY_MB,
  "screen_density": $(echo $DENSITY | tr -d 'dpi'),
  "screen_resolution": "$SIZE",
  "cpu_abi": "$(adb shell getprop ro.product.cpu.abi)"
}
EOF

echo "✅ Device info saved to device-info.json"
echo ""
echo "🚀 Ready to deploy to device!"
echo "   Run: ./gradlew installDebug"
echo "   Then open the PerfScope Demo app on your device"