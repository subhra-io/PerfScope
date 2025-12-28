# PerfScope SDK

A LeakCanary-style Android SDK that provides real-time visibility into app performance, memory usage, and app size directly inside the running app, without attaching a profiler.

## 🎯 Key Features: System Health + User Experience Enforcement

**From Observability to Engineering Enforcement** - PerfScope enforces both system health and user experience budgets:

### System Health (Memory Budgets)
```kotlin
PerfScopeConfig(
    maxHeapMb = 180,
    maxScreenDeltaMb = 25,
    maxBitmapSpikeMb = 20
)
```

### User Experience (Frame Budgets)
```kotlin
PerfScopeConfig(
    maxJankPercent = 3f,      // Max 3% jank frames
    maxFrameMs = 24f,         // Max 24ms per frame (~42fps)
    maxSevereJankMs = 50f     // Severe jank threshold
)
```

**Instead of just showing metrics, PerfScope shows actionable violations:**

```
⚠️ Performance Violation
Main Thread Blocking Jank
Actual: 8.5% | Budget: 3%
💡 Move long operations to background threads
```

This balances PerfScope across **system health** (memory) and **user experience** (frame timing) - exactly what real teams need.

## Features

- 📊 **Real-time Performance Monitoring**: Track memory usage, frame rate, CPU usage, and app size
- 🎯 **Memory Attribution**: Identifies which screen and what type of operations caused memory changes
- 🎬 **Frame/Jank Attribution**: Choreographer-based frame timing with jank classification
- 🚨 **Performance Budget Enforcement**: Define acceptable limits for both system and UX metrics
- 🔍 **Smart Classification**: 
  - Memory: bitmap-heavy, collection-heavy, native-heavy, object-heavy
  - Jank: main-thread blocking, layout thrash, heavy recomposition, overdraw
- ⚖️ **Configurable Budgets**: Different configs for development, production, and performance-critical apps
- 🎨 **Compose-based UI**: Modern, clean overlay interface with violation alerts
- 🔧 **Easy Integration**: Simple API with minimal setup required
- 📱 **Non-intrusive**: Optional overlay that can be shown/hidden on demand
- 📈 **Screen Tracking**: Automatic activity tracking with manual screen naming support

## Frame/Jank Attribution System

### Choreographer-Based Monitoring

PerfScope uses Android's Choreographer for precise frame timing measurement:

- **Real Frame Times**: Actual frame durations, not estimates
- **Jank Detection**: Frames >16.67ms (60fps) classified as jank
- **Pattern Analysis**: Identifies jank causes through timing patterns

### Jank Classification Types

- **Main Thread Blocking**: Long-running operations blocking UI (>100ms frames)
- **Layout Thrash**: Excessive layout calculations (consistent 30-50ms frames)
- **Heavy Recomposition**: Compose recomposition issues (frequent 20-35ms frames)
- **Overdraw**: Too many overlapping draw operations (consistent moderate jank)
- **Memory Pressure**: GC pauses causing frame drops (sporadic severe jank)
- **GPU Bottleneck**: Graphics processing delays (high average frame time)

### Frame Budget Enforcement

```kotlin
// Development - Strict UX budgets
val devConfig = PerfScopeConfig(
    maxJankPercent = 2f,       // Very strict jank budget
    maxFrameMs = 20f,          // Strict frame timing
    maxSevereJankMs = 30f,     // Low severe jank threshold
    enableFrameMonitoring = true
)

// Performance-Critical - Ultra-strict
val criticalConfig = PerfScopeConfig.performanceCritical()
// maxJankPercent = 1f, maxFrameMs = 16.67f (60fps strict)
```

## Performance Budget System

### Budget Configuration

```kotlin
// Development - Strict budgets for catching issues early
val devConfig = PerfScopeConfig(
    // Memory Budgets
    maxHeapMb = 150,
    maxScreenDeltaMb = 20,
    maxBitmapSpikeMb = 15,
    
    // Frame/UX Budgets  
    maxJankPercent = 3f,
    maxFrameMs = 24f,
    maxSevereJankMs = 50f,
    
    enableViolationAlerts = true,
    enableFrameMonitoring = true
)

// Production - Monitoring without UI alerts
val prodConfig = PerfScopeConfig.production()

// Performance-Critical - Ultra-strict budgets
val criticalConfig = PerfScopeConfig.performanceCritical()
```

### Violation Types

**Memory Violations:**
- Heap Memory, Screen Memory Delta, Bitmap/Collection/Object/Native Spikes

**Frame/UX Violations:**
- Jank Percentage, Frame Time, Severe Jank

**System Violations:**
- Frame Rate Drop, CPU Usage, App Size

### Why This Matters

- **Memory bugs hurt devices** (crashes, slowdowns)
- **Jank hurts users** (poor experience, app abandonment)
- **PerfScope enforces both** (comprehensive performance engineering)

## Project Structure

```
PerfScopeSdk/
├── app/          # Demo application with frame/jank violation tests
├── perfscope/    # The SDK library module
│   ├── budget/          # Performance budget engine
│   ├── config/          # Budget configuration
│   ├── attribution/     # Memory & frame attribution engines
│   ├── tracking/        # Screen tracking system
│   ├── monitoring/      # Performance & frame monitoring
│   ├── ui/             # Compose UI components
│   └── data/           # Data models
└── gradle/       # Build configuration
```

## Quick Start

### 1. Add SDK Dependency

In your app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation(project(":perfscope"))
}
```

### 2. Initialize SDK with Budget Configuration

In your `MainActivity.onCreate()`:

```kotlin
import io.perfscope.sdk.PerfScope
import io.perfscope.sdk.config.PerfScopeConfig

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize with frame/jank monitoring
        val config = PerfScopeConfig(
            // Memory budgets
            maxHeapMb = 180,
            maxScreenDeltaMb = 25,
            maxBitmapSpikeMb = 20,
            
            // Frame/UX budgets
            maxJankPercent = 3f,
            maxFrameMs = 24f,
            
            enableViolationAlerts = true,
            enableFrameMonitoring = true
        )
        PerfScope.init(this, config)
        
        setContent {
            YourAppTheme {
                YourMainScreen()
                PerfScope.OverlayContent()
            }
        }
    }
}
```

### 3. Use Performance Budget Enforcement

```kotlin
// Show performance overlay with violations
PerfScope.showOverlay()

// Switch to performance-critical budgets
PerfScope.updateBudgetConfig(PerfScopeConfig.performanceCritical())

// Manual screen tracking for attribution
PerfScope.setCurrentScreen("UserProfile")
```

## Demo App Features

The demo app includes comprehensive violation tests:

- **Memory Violation Tests**: Bitmap, collection, and object allocation tests
- **Frame/Jank Violation Tests**: Main thread blocking, layout thrash, Compose jank
- **Budget Configuration**: Switch between relaxed, strict, and performance-critical budgets
- **Real-time Attribution**: See both memory and frame attribution as violations happen

## Performance Engineering Impact

### Development Workflow
1. **Strict Budgets**: Catch both memory and UX regressions early
2. **Immediate Feedback**: See violations as you code
3. **Attribution Insights**: Know exactly what caused the issue
4. **Team Standards**: Enforce consistent performance across team

### User Experience Focus
1. **Jank Prevention**: Catch frame drops before users notice
2. **Smooth Performance**: Maintain 60fps standards
3. **Attribution Context**: Know which screens cause jank
4. **Proactive Optimization**: Fix UX issues during development

## Building the Project

```bash
./gradlew assembleDebug
```

## Requirements

- Android API 23+
- Kotlin
- Jetpack Compose

## Next Steps

Future enhancements:

1. **Network Attribution**: API call performance per screen
2. **Battery Attribution**: Power consumption by screen/feature  
3. **CI/CD Integration**: Automated budget enforcement in build pipelines
4. **Performance Profiles**: Different budgets per feature/screen
5. **Historical Analytics**: Long-term performance trend analysis
6. **Custom Jank Detection**: App-specific jank pattern recognition

## License

This project is a demonstration SDK for educational purposes.