# 🚀 PerfScope - Real-time Performance Monitoring for Android

PerfScope is a production-ready performance monitoring platform for Android applications that provides real-time visibility into app performance, memory usage, and user experience quality.

## ✨ Features

- **🧠 Intelligent Performance Attribution** - Automatic detection of memory leaks, jank, and performance violations
- **📊 Real-time Monitoring** - Live performance overlay and instant violation alerts
- **🎯 Performance Budget Enforcement** - Configurable thresholds with automatic enforcement
- **🌐 Production-ready Backend** - High-throughput event ingestion with web dashboard
- **📱 Screen-level Tracking** - Performance issues tied to specific app screens

## 🚀 Quick Start

### 1. Add PerfScope SDK to your Android project

```kotlin
// In your MainActivity
PerfScope.init(this, PerfScopeConfig.development())
```

### 2. Add screen tracking (optional)

```kotlin
@Composable
fun YourScreen() {
    LaunchedEffect(Unit) {
        PerfScope.setCurrentScreen("YourScreen")
    }
    // Your existing UI code
}
```

### 3. Deploy backend (optional)

```bash
cd backend
docker-compose up -d
```

### 4. Deploy web dashboard (optional)

[![Deploy with Vercel](https://vercel.com/button)](https://vercel.com/new/clone?repository-url=https://github.com/subhra-io/PerfScope&project-name=perfscope-dashboard&repository-name=perfscope-dashboard&root-directory=web-portal)

Or see [web-portal/DEPLOYMENT.md](web-portal/DEPLOYMENT.md) for detailed instructions.

## 📱 Integration

### Add to your project

1. Copy the `perfscope/` module to your project
2. Add to `settings.gradle.kts`: `include(":perfscope")`
3. Add dependency: `implementation(project(":perfscope"))`
4. Add network permissions to `AndroidManifest.xml`

### Configuration Options

```kotlin
// Development
val devConfig = PerfScopeConfig.development()

// Production with export
val prodConfig = PerfScopeConfig.withExport(
    endpoint = "https://your-backend.com/api/events",
    apiKey = "your-api-key"
)

// Custom configuration
val customConfig = PerfScopeConfig(
    maxHeapMb = 150,
    maxJankPercent = 2f,
    enableExport = true
)
```

## 🏗️ Architecture

- **📱 Android SDK (Kotlin)** - Performance monitoring library
- **🖥️ Backend API (Node.js + TypeScript)** - Event processing and storage
- **📊 Web Dashboard (React + Next.js)** - Real-time monitoring interface
- **☁️ Cloud Ready** - Deploy on Vercel, Railway, or any cloud platform

## 🎯 Use Cases

- **E-commerce Apps** - Monitor product loading and checkout performance
- **Gaming Apps** - Ensure consistent 60 FPS gameplay
- **Social Media Apps** - Optimize feed scrolling and media loading
- **Enterprise Apps** - Monitor business-critical workflows

## 📊 What You Get

- **Automatic detection** of memory leaks, jank, and performance violations
- **Root cause analysis** with specific recommendations
- **Real-time monitoring** across all users and devices
- **Professional dashboard** with charts and alerts
- **Screen-level attribution** for precise debugging

## 🔧 Requirements

- **Android**: API 21+ (Android 5.0)
- **Kotlin**: 1.8+
- **Compose**: Optional (for UI overlay)
- **Backend**: Node.js 18+ (optional)

## 📄 License

MIT License - see LICENSE file for details.

## 🤝 Contributing

Contributions are welcome! Please read our contributing guidelines and submit pull requests.

---

**PerfScope: Professional Performance Monitoring for Android Apps**

*Stop guessing why your app is slow. Start knowing exactly what to fix.*