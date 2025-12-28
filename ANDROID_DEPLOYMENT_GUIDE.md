# PerfScope Android SDK - Production Deployment Guide

This guide shows how to integrate the PerfScope Android SDK with your deployed backend infrastructure.

## 🚀 Quick Integration

### 1. Add PerfScope SDK to Your Project

```kotlin
// In your app/build.gradle.kts
dependencies {
    implementation(project(":perfscope"))
}

// In your settings.gradle.kts
include(":perfscope")
```

### 2. Add Network Permissions

```xml
<!-- In your AndroidManifest.xml -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

### 3. Initialize in Your Application

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Choose your deployment configuration
        val config = when {
            BuildConfig.BUILD_TYPE == "release" -> {
                // Production with Railway
                PerfScopeConfig.railway("your-production-api-key")
            }
            BuildConfig.BUILD_TYPE == "staging" -> {
                // Staging environment
                PerfScopeConfig.staging(
                    endpoint = "https://your-staging-backend.com/api/events",
                    apiKey = "staging-api-key"
                )
            }
            else -> {
                // Development with local backend
                PerfScopeConfig.development()
            }
        }
        
        PerfScope.init(this, config)
    }
}
```

## 🌐 Backend Integration Options

### Railway Deployment (Recommended)

```kotlin
// Automatic Railway configuration
val config = PerfScopeConfig.railway("your-api-key")
PerfScope.init(this, config)
```

### Render Deployment

```kotlin
// Automatic Render configuration
val config = PerfScopeConfig.render("your-api-key")
PerfScope.init(this, config)
```

### Vercel Deployment

```kotlin
// Automatic Vercel configuration
val config = PerfScopeConfig.vercel("your-api-key")
PerfScope.init(this, config)
```

### Custom Backend

```kotlin
// Custom backend URL
val config = PerfScopeConfig.production(
    endpoint = "https://your-backend.com/api/events",
    apiKey = "your-api-key"
)
PerfScope.init(this, config)
```

## 📱 Screen Tracking

### Compose Integration

```kotlin
@Composable
fun YourScreen() {
    // Set screen name for attribution
    LaunchedEffect(Unit) {
        PerfScope.setCurrentScreen("YourScreen")
    }
    
    // Your existing UI code
    YourScreenContent()
}
```

### Activity Integration

```kotlin
class YourActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set screen name
        PerfScope.setCurrentScreen("YourActivity")
        
        setContent {
            YourApp {
                // Add PerfScope overlay
                PerfScope.OverlayContent()
            }
        }
    }
}
```

## ⚙️ Configuration Options

### Development Configuration

```kotlin
val devConfig = PerfScopeConfig.development()
// Features:
// - Strict budgets to catch issues early
// - Debug logging enabled
// - Violation alerts shown
// - Export disabled by default
```

### Staging Configuration

```kotlin
val stagingConfig = PerfScopeConfig.staging(
    endpoint = "https://staging-backend.com/api/events",
    apiKey = "staging-key"
)
// Features:
// - Moderate budgets
// - Debug logging enabled
// - Export enabled with smaller batches
// - Violation alerts shown
```

### Production Configuration

```kotlin
val prodConfig = PerfScopeConfig.production(
    endpoint = "https://prod-backend.com/api/events",
    apiKey = "prod-key"
)
// Features:
// - Lenient budgets for real users
// - Debug logging disabled
// - Export enabled with larger batches
// - Violation alerts hidden
```

### Custom Configuration

```kotlin
val customConfig = PerfScopeConfig(
    // Memory Budgets
    maxHeapMb = 150,
    maxScreenDeltaMb = 20,
    maxBitmapSpikeMb = 15,
    
    // Frame/UX Budgets
    maxJankPercent = 5f,
    maxFrameMs = 25f,
    maxSevereJankMs = 40f,
    
    // Export Settings
    enableExport = true,
    exportEndpoint = "https://your-backend.com/api/events",
    exportApiKey = "your-api-key",
    exportBatchSize = 15,
    exportTimeoutMs = 20000,
    exportRetryAttempts = 3,
    
    // Environment Settings
    environment = Environment.PRODUCTION,
    enableDebugLogs = false,
    enableNetworkLogs = false
)
```

## 🔧 Advanced Features

### Performance Budget Enforcement

```kotlin
// Ultra-strict for performance-critical apps
val criticalConfig = PerfScopeConfig.performanceCritical()

// Testing configuration (relaxed budgets)
val testConfig = PerfScopeConfig.testing()

// Update configuration at runtime
PerfScope.updateBudgetConfig(newConfig)
```

### Manual Overlay Control

```kotlin
// Show/hide overlay programmatically
PerfScope.showOverlay()
PerfScope.hideOverlay()

// Check overlay state
if (PerfScope.isOverlayVisible()) {
    // Overlay is visible
}
```

### Screen Attribution

```kotlin
// Set screen names for better attribution
PerfScope.setCurrentScreen("ProductDetails")
PerfScope.setCurrentScreen("Checkout")
PerfScope.setCurrentScreen("OrderConfirmation")
```

## 📊 What Gets Sent to Backend

The SDK automatically sends these events to your backend:

### Session Events
- **Session Start**: App launch, initial screen
- **Session End**: App close, total violations
- **Screen Changes**: Navigation between screens

### Performance Events
- **Memory Violations**: Heap, bitmap, collection spikes
- **Jank Violations**: Frame drops, severe jank
- **Health Snapshots**: Periodic performance metrics

### Device Context
- Device model, manufacturer, Android version
- RAM, screen density, resolution
- App version, build type, flavor

## 🔍 Monitoring & Debugging

### Enable Debug Logging

```kotlin
val config = PerfScopeConfig.development().copy(
    enableDebugLogs = true,
    enableNetworkLogs = true
)
```

### Check Logs

```bash
# Filter PerfScope logs
adb logcat | grep PerfScope

# Common log messages:
# I/PerfScope: SDK initialized with PRODUCTION configuration
# I/PerfScope: Export enabled to: https://your-backend.com/api/events
# D/PerfScope: Sending batch of 10 events to backend
# D/PerfScope: Successfully sent 10 events
```

### Test Integration

```kotlin
// Trigger test violations to verify backend connection
Button(onClick = {
    // This will trigger memory violation
    val largeArray = ByteArray(50 * 1024 * 1024) // 50MB
}) {
    Text("Test Memory Violation")
}
```

## 🚨 Troubleshooting

### Common Issues

**No Data in Dashboard**
- Check network permissions in AndroidManifest.xml
- Verify backend URL is correct and accessible
- Enable debug logs to see HTTP requests
- Check API key is valid

**HTTP Connection Failed**
```kotlin
// Check logs for:
E/PerfScope: HTTP request failed: java.net.ConnectException
E/PerfScope: HTTP error: 404 - Not Found
E/PerfScope: HTTP error: 401 - Unauthorized
```

**SSL/HTTPS Issues**
```kotlin
// For development with self-signed certificates
val config = PerfScopeConfig.development().copy(
    exportEndpoint = "http://your-local-ip:3001/api/events" // Use HTTP for local
)
```

**Memory Issues**
```kotlin
// Reduce batch size for memory-constrained devices
val config = PerfScopeConfig.production(endpoint, apiKey).copy(
    exportBatchSize = 5,  // Smaller batches
    exportTimeoutMs = 10000  // Shorter timeout
)
```

### Debug Network Requests

```kotlin
// Enable detailed network logging
val config = PerfScopeConfig.development().copy(
    enableNetworkLogs = true
)

// Check logs:
// D/PerfScope: Sending HTTP request to: https://backend.com/api/events
// D/PerfScope: Request payload: {"api_key":"...","events":[...]}
// D/PerfScope: HTTP response code: 200
```

## 🎯 Best Practices

### Production Deployment

1. **Use Environment-Specific Configs**
   ```kotlin
   val config = when (BuildConfig.BUILD_TYPE) {
       "release" -> PerfScopeConfig.railway("prod-key")
       "staging" -> PerfScopeConfig.staging("staging-url", "staging-key")
       else -> PerfScopeConfig.development()
   }
   ```

2. **Disable Debug Logs in Production**
   ```kotlin
   val prodConfig = PerfScopeConfig.production(endpoint, apiKey).copy(
       enableDebugLogs = false,
       enableNetworkLogs = false
   )
   ```

3. **Set Appropriate Budgets**
   ```kotlin
   // Strict for development (catch issues early)
   val devConfig = PerfScopeConfig.development()
   
   // Lenient for production (real user conditions)
   val prodConfig = PerfScopeConfig.production(endpoint, apiKey)
   ```

### Performance Optimization

1. **Batch Size Tuning**
   ```kotlin
   // High-traffic apps: larger batches
   exportBatchSize = 25
   
   // Low-memory devices: smaller batches
   exportBatchSize = 5
   ```

2. **Network Optimization**
   ```kotlin
   // Faster timeout for mobile networks
   exportTimeoutMs = 15000
   
   // More retries for unreliable networks
   exportRetryAttempts = 5
   ```

3. **Screen Attribution**
   ```kotlin
   // Set meaningful screen names
   PerfScope.setCurrentScreen("ProductList")
   PerfScope.setCurrentScreen("ProductDetails_${productId}")
   PerfScope.setCurrentScreen("Checkout_Step2")
   ```

## 📈 Scaling Considerations

### High-Volume Apps

```kotlin
val highVolumeConfig = PerfScopeConfig.production(endpoint, apiKey).copy(
    exportBatchSize = 50,        // Larger batches
    exportTimeoutMs = 30000,     // Longer timeout
    exportRetryAttempts = 2      // Fewer retries
)
```

### Low-End Devices

```kotlin
val lowEndConfig = PerfScopeConfig.production(endpoint, apiKey).copy(
    exportBatchSize = 3,         // Smaller batches
    exportTimeoutMs = 10000,     // Shorter timeout
    enableFrameMonitoring = false // Reduce overhead
)
```

---

## 🎉 You're Ready!

Your Android app is now connected to your deployed PerfScope backend. The SDK will automatically:

- ✅ Monitor performance and detect violations
- ✅ Send real-time data to your backend
- ✅ Provide attribution for performance issues
- ✅ Scale with your user base
- ✅ Handle network failures gracefully

**Next Steps:**
1. Deploy your app with PerfScope integration
2. Monitor the web dashboard for real-time data
3. Analyze performance patterns across users
4. Optimize based on real user insights