# 🚀 PerfScope Quick Start Templates

## 📱 **Template 1: New Compose Project**

### **Step 1: Add PerfScope SDK**
```kotlin
// settings.gradle.kts
include(":perfscope")

// app/build.gradle.kts
dependencies {
    implementation(project(":perfscope"))
}
```

### **Step 2: AndroidManifest.xml**
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<application android:usesCleartextTraffic="true">
```

### **Step 3: MainActivity.kt**
```kotlin
import io.perfscope.sdk.PerfScope
import io.perfscope.sdk.config.PerfScopeConfig

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize PerfScope
        val config = PerfScopeConfig.withExport(
            endpoint = "https://your-backend.com/api/events",
            apiKey = "your-api-key"
        )
        PerfScope.init(this, config)
        
        setContent {
            MyAppTheme {
                Navigation()
                
                // Optional: Show PerfScope overlay in debug
                if (BuildConfig.DEBUG) {
                    PerfScope.OverlayContent()
                }
            }
        }
    }
}
```

### **Step 4: Screen Tracking**
```kotlin
@Composable
fun HomeScreen() {
    LaunchedEffect(Unit) {
        PerfScope.setCurrentScreen("HomeScreen")
    }
    
    // Your UI code...
}
```

---

## 📱 **Template 2: Existing Project Integration**

### **Minimal Integration (5 minutes)**
```kotlin
// 1. Add to your existing MainActivity
class ExistingMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Add this line - that's it!
        PerfScope.init(this, PerfScopeConfig.development())
        
        // Your existing code unchanged
        setContent {
            ExistingTheme {
                ExistingNavigation()
            }
        }
    }
}

// 2. Add to your existing screens (optional)
@Composable
fun ExistingScreen() {
    LaunchedEffect(Unit) {
        PerfScope.setCurrentScreen("ExistingScreen") // Add this line
    }
    
    // Your existing UI code unchanged
}
```

---

## 🏪 **Template 3: E-commerce App**

### **Optimized for Product Catalogs**
```kotlin
class ShoppingMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // E-commerce optimized config
        val ecommerceConfig = PerfScopeConfig(
            maxHeapMb = 200,           // Higher for product images
            maxBitmapSpikeMb = 30,     // Allow product image loading
            maxJankPercent = 2f,       // Smooth scrolling important
            maxFrameMs = 20f,          // Smooth product browsing
            enableExport = true,
            exportEndpoint = "https://analytics.yourstore.com/perfscope/events",
            exportApiKey = BuildConfig.PERFSCOPE_API_KEY
        )
        PerfScope.init(this, ecommerceConfig)
        
        setContent {
            ShoppingTheme {
                ShoppingNavigation()
            }
        }
    }
}

@Composable
fun ProductListScreen() {
    LaunchedEffect(Unit) {
        PerfScope.setCurrentScreen("ProductList")
    }
    
    LazyVerticalGrid {
        // PerfScope automatically monitors image loading performance
        items(products) { product ->
            ProductCard(product)
        }
    }
}
```

---

## 🎮 **Template 4: Gaming App**

### **Optimized for 60 FPS Performance**
```kotlin
class GameMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Gaming optimized config
        val gamingConfig = PerfScopeConfig(
            maxHeapMb = 300,           // Higher for game assets
            maxFrameMs = 16f,          // 60 FPS requirement
            maxJankPercent = 1f,       // Ultra-smooth gameplay
            maxSevereJankMs = 20f,     // Very strict jank detection
            enableFrameMonitoring = true,
            enableViolationAlerts = false, // No alerts during gameplay
            enableExport = true,
            exportEndpoint = "https://game-analytics.com/perfscope/events",
            exportApiKey = BuildConfig.PERFSCOPE_API_KEY
        )
        PerfScope.init(this, gamingConfig)
        
        setContent {
            GameTheme {
                GameNavigation()
            }
        }
    }
}

@Composable
fun GameplayScreen() {
    LaunchedEffect(Unit) {
        PerfScope.setCurrentScreen("Gameplay")
    }
    
    // Your game UI - PerfScope monitors frame timing
}
```

---

## 📱 **Template 5: Social Media App**

### **Optimized for Feed Scrolling**
```kotlin
class SocialMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Social media optimized config
        val socialConfig = PerfScopeConfig(
            maxHeapMb = 250,           // Higher for media content
            maxBitmapSpikeMb = 40,     // Image/video heavy
            maxCollectionSpikeMb = 35, // Large feed data
            maxJankPercent = 2f,       // Smooth scrolling feeds
            maxFrameMs = 22f,          // Smooth feed scrolling
            enableExport = true,
            exportEndpoint = "https://social-analytics.com/perfscope/events",
            exportApiKey = BuildConfig.PERFSCOPE_API_KEY
        )
        PerfScope.init(this, socialConfig)
        
        setContent {
            SocialTheme {
                SocialNavigation()
            }
        }
    }
}

@Composable
fun FeedScreen() {
    LaunchedEffect(Unit) {
        PerfScope.setCurrentScreen("Feed")
    }
    
    LazyColumn {
        // PerfScope monitors scrolling performance automatically
        items(posts) { post ->
            PostCard(post)
        }
    }
}
```

---

## 🏢 **Template 6: Enterprise App**

### **Conservative Performance Monitoring**
```kotlin
class EnterpriseMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enterprise optimized config
        val enterpriseConfig = PerfScopeConfig(
            maxHeapMb = 150,           // Conservative memory
            maxBitmapSpikeMb = 20,     // Limited media usage
            maxJankPercent = 4f,       // Functionality over smoothness
            maxFrameMs = 25f,          // Relaxed frame timing
            enableViolationAlerts = false, // No user-facing alerts
            enableFrameMonitoring = true,
            enableExport = true,
            exportEndpoint = "https://internal-analytics.company.com/perfscope/events",
            exportApiKey = BuildConfig.PERFSCOPE_API_KEY
        )
        PerfScope.init(this, enterpriseConfig)
        
        setContent {
            EnterpriseTheme {
                EnterpriseNavigation()
            }
        }
    }
}
```

---

## ⚙️ **Configuration Presets**

### **Quick Config Options**
```kotlin
// Development - Relaxed budgets, UI alerts enabled
val devConfig = PerfScopeConfig.development()

// Production - Strict budgets, no UI alerts
val prodConfig = PerfScopeConfig.production()

// Performance Critical - Ultra-strict budgets
val criticalConfig = PerfScopeConfig.performanceCritical()

// Custom configuration
val customConfig = PerfScopeConfig(
    maxHeapMb = 180,
    maxJankPercent = 3f,
    enableExport = true,
    exportEndpoint = "your-endpoint",
    exportApiKey = "your-key"
)
```

---

## 🔧 **Build Configuration**

### **Gradle Setup for Different Environments**
```kotlin
// app/build.gradle.kts
android {
    buildTypes {
        debug {
            buildConfigField("String", "PERFSCOPE_ENDPOINT", "\"http://localhost:3001/api/events\"")
            buildConfigField("String", "PERFSCOPE_API_KEY", "\"dev-key-123\"")
        }
        release {
            buildConfigField("String", "PERFSCOPE_ENDPOINT", "\"https://api.yourcompany.com/perfscope/events\"")
            buildConfigField("String", "PERFSCOPE_API_KEY", "\"prod-key-xyz\"")
        }
    }
}

// Usage in code
val config = PerfScopeConfig.withExport(
    endpoint = BuildConfig.PERFSCOPE_ENDPOINT,
    apiKey = BuildConfig.PERFSCOPE_API_KEY
)
```

---

## 📊 **Backend Quick Setup**

### **Option 1: Use Our Test Server**
```bash
# Copy backend to your server
cp -r backend/ /your/server/path/
cd backend
npm install
node test-server.js
```

### **Option 2: Docker Deployment**
```bash
cd backend
docker-compose up -d
# Backend runs on port 3001
# Dashboard runs on port 3000
```

### **Option 3: Cloud Deployment**
```bash
# Deploy to Heroku/Railway/etc.
git push heroku main
# Set environment variables in your platform
```

---

## ✅ **Integration Checklist**

### **Android App**
- [ ] Copy `perfscope/` module to your project
- [ ] Add to `settings.gradle.kts`
- [ ] Add dependency in `build.gradle.kts`
- [ ] Add network permissions to `AndroidManifest.xml`
- [ ] Initialize PerfScope in `MainActivity`
- [ ] Add screen tracking to your screens
- [ ] Configure build variants with different endpoints

### **Backend**
- [ ] Deploy backend server
- [ ] Set up database (PostgreSQL recommended)
- [ ] Configure environment variables
- [ ] Set up API keys
- [ ] Test connectivity from app

### **Dashboard**
- [ ] Deploy web dashboard
- [ ] Configure API endpoint
- [ ] Test real-time updates
- [ ] Set up monitoring alerts

---

## 🎯 **Success Validation**

After integration, verify:
1. **App starts** without crashes
2. **Events appear** in backend logs
3. **Dashboard shows** real-time data
4. **Violations trigger** when expected
5. **Performance attribution** works correctly

**You're now monitoring your app's performance in real-time!** 🚀