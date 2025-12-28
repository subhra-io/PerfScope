package io.perfscope.sdk.export

/**
 * High-level performance events sent to web portal.
 * These are conclusions/insights, not raw metrics.
 */
sealed class PerfEvent {
    abstract val timestamp: Long
    abstract val appId: String
    abstract val sessionId: String
    abstract val screen: String
    abstract val device: DeviceInfo
    abstract val build: BuildInfo
}

/**
 * Session lifecycle events
 */
data class SessionStartEvent(
    override val timestamp: Long,
    override val appId: String,
    override val sessionId: String,
    override val screen: String,
    override val device: DeviceInfo,
    override val build: BuildInfo,
    val sdkVersion: String
) : PerfEvent()

data class SessionEndEvent(
    override val timestamp: Long,
    override val appId: String,
    override val sessionId: String,
    override val screen: String,
    override val device: DeviceInfo,
    override val build: BuildInfo,
    val durationMs: Long,
    val totalViolations: Int
) : PerfEvent()

/**
 * Screen navigation events
 */
data class ScreenChangeEvent(
    override val timestamp: Long,
    override val appId: String,
    override val sessionId: String,
    override val screen: String,
    override val device: DeviceInfo,
    override val build: BuildInfo,
    val previousScreen: String,
    val timeOnPreviousScreenMs: Long
) : PerfEvent()

/**
 * Performance violation events (the key insights)
 */
data class MemoryViolationEvent(
    override val timestamp: Long,
    override val appId: String,
    override val sessionId: String,
    override val screen: String,
    override val device: DeviceInfo,
    override val build: BuildInfo,
    val violationType: String, // "BITMAP_SPIKE", "COLLECTION_SPIKE", etc.
    val actualMb: Int,
    val budgetMb: Int,
    val severity: String, // "LOW", "MEDIUM", "HIGH", "CRITICAL"
    val attribution: MemoryAttributionData
) : PerfEvent()

data class JankViolationEvent(
    override val timestamp: Long,
    override val appId: String,
    override val sessionId: String,
    override val screen: String,
    override val device: DeviceInfo,
    override val build: BuildInfo,
    val violationType: String, // "JANK_PERCENT", "FRAME_TIME", "SEVERE_JANK"
    val actualValue: Float, // jank % or frame time ms
    val budgetValue: Float,
    val severity: String,
    val jankType: String, // "MAIN_THREAD_BLOCKING", "LAYOUT_THRASH", etc.
    val attribution: JankAttributionData
) : PerfEvent()

/**
 * Periodic health snapshots (every 30 seconds)
 */
data class HealthSnapshotEvent(
    override val timestamp: Long,
    override val appId: String,
    override val sessionId: String,
    override val screen: String,
    override val device: DeviceInfo,
    override val build: BuildInfo,
    val memoryMb: Int,
    val jankPercent: Float,
    val avgFrameMs: Float,
    val cpuPercent: Int
) : PerfEvent()

/**
 * Supporting data structures
 */
data class DeviceInfo(
    val model: String,
    val manufacturer: String,
    val androidVersion: Int,
    val ramMb: Int,
    val screenDensity: String, // "hdpi", "xhdpi", etc.
    val screenResolution: String // "1080x1920"
)

data class BuildInfo(
    val versionName: String,
    val versionCode: Int,
    val buildType: String, // "debug", "release"
    val flavor: String? = null
)

data class MemoryAttributionData(
    val likelyCause: String, // "BITMAP_HEAVY", "COLLECTION_HEAVY", etc.
    val deltaMb: Int,
    val details: String
)

data class JankAttributionData(
    val jankPercent: Float,
    val avgFrameMs: Float,
    val frameCount: Int,
    val jankFrameCount: Int,
    val details: String
)