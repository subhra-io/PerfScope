package io.perfscope.sdk.export

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import io.perfscope.sdk.data.FrameAttribution
import io.perfscope.sdk.data.MemoryAttribution
import io.perfscope.sdk.data.PerformanceViolation
import io.perfscope.sdk.data.ViolationType
import java.util.*

/**
 * Factory for creating PerfEvents from SDK data structures.
 * Handles the conversion from internal SDK data to exportable events.
 */
class EventFactory(private val context: Context) {
    
    private val appId: String by lazy { context.packageName }
    private val sessionId: String = UUID.randomUUID().toString()
    private val deviceInfo: DeviceInfo by lazy { createDeviceInfo() }
    private val buildInfo: BuildInfo by lazy { createBuildInfo() }
    
    fun createSessionStartEvent(screen: String): SessionStartEvent {
        return SessionStartEvent(
            timestamp = System.currentTimeMillis(),
            appId = appId,
            sessionId = sessionId,
            screen = screen,
            device = deviceInfo,
            build = buildInfo,
            sdkVersion = "1.0.0"
        )
    }
    
    fun createSessionEndEvent(
        screen: String,
        durationMs: Long,
        totalViolations: Int
    ): SessionEndEvent {
        return SessionEndEvent(
            timestamp = System.currentTimeMillis(),
            appId = appId,
            sessionId = sessionId,
            screen = screen,
            device = deviceInfo,
            build = buildInfo,
            durationMs = durationMs,
            totalViolations = totalViolations
        )
    }
    
    fun createScreenChangeEvent(
        currentScreen: String,
        previousScreen: String,
        timeOnPreviousScreenMs: Long
    ): ScreenChangeEvent {
        return ScreenChangeEvent(
            timestamp = System.currentTimeMillis(),
            appId = appId,
            sessionId = sessionId,
            screen = currentScreen,
            device = deviceInfo,
            build = buildInfo,
            previousScreen = previousScreen,
            timeOnPreviousScreenMs = timeOnPreviousScreenMs
        )
    }
    
    fun createMemoryViolationEvent(
        screen: String,
        violation: PerformanceViolation,
        attribution: MemoryAttribution
    ): MemoryViolationEvent {
        return MemoryViolationEvent(
            timestamp = System.currentTimeMillis(),
            appId = appId,
            sessionId = sessionId,
            screen = screen,
            device = deviceInfo,
            build = buildInfo,
            violationType = violation.type.name,
            actualMb = violation.actualValue,
            budgetMb = violation.budgetValue,
            severity = violation.severity.name,
            attribution = MemoryAttributionData(
                likelyCause = attribution.likelyCause.name,
                deltaMb = attribution.memoryDeltaMb,
                details = attribution.details
            )
        )
    }
    
    fun createJankViolationEvent(
        screen: String,
        violation: PerformanceViolation,
        attribution: FrameAttribution
    ): JankViolationEvent {
        return JankViolationEvent(
            timestamp = System.currentTimeMillis(),
            appId = appId,
            sessionId = sessionId,
            screen = screen,
            device = deviceInfo,
            build = buildInfo,
            violationType = violation.type.name,
            actualValue = violation.actualValue.toFloat(),
            budgetValue = violation.budgetValue.toFloat(),
            severity = violation.severity.name,
            jankType = attribution.jankType.name,
            attribution = JankAttributionData(
                jankPercent = attribution.jankPercent,
                avgFrameMs = attribution.averageFrameMs,
                frameCount = attribution.frameCount,
                jankFrameCount = attribution.jankFrameCount,
                details = attribution.details
            )
        )
    }
    
    fun createHealthSnapshotEvent(
        screen: String,
        memoryMb: Int,
        jankPercent: Float,
        avgFrameMs: Float,
        cpuPercent: Int
    ): HealthSnapshotEvent {
        return HealthSnapshotEvent(
            timestamp = System.currentTimeMillis(),
            appId = appId,
            sessionId = sessionId,
            screen = screen,
            device = deviceInfo,
            build = buildInfo,
            memoryMb = memoryMb,
            jankPercent = jankPercent,
            avgFrameMs = avgFrameMs,
            cpuPercent = cpuPercent
        )
    }
    
    private fun createDeviceInfo(): DeviceInfo {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        
        val densityName = when (displayMetrics.densityDpi) {
            DisplayMetrics.DENSITY_LOW -> "ldpi"
            DisplayMetrics.DENSITY_MEDIUM -> "mdpi"
            DisplayMetrics.DENSITY_HIGH -> "hdpi"
            DisplayMetrics.DENSITY_XHIGH -> "xhdpi"
            DisplayMetrics.DENSITY_XXHIGH -> "xxhdpi"
            DisplayMetrics.DENSITY_XXXHIGH -> "xxxhdpi"
            else -> "unknown"
        }
        
        return DeviceInfo(
            model = Build.MODEL,
            manufacturer = Build.MANUFACTURER,
            androidVersion = Build.VERSION.SDK_INT,
            ramMb = getRamMb(),
            screenDensity = densityName,
            screenResolution = "${displayMetrics.widthPixels}x${displayMetrics.heightPixels}"
        )
    }
    
    private fun createBuildInfo(): BuildInfo {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            BuildInfo(
                versionName = packageInfo.versionName ?: "unknown",
                versionCode = packageInfo.versionCode,
                buildType = if (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0) "debug" else "release",
                flavor = null // Could be extracted from BuildConfig if needed
            )
        } catch (e: PackageManager.NameNotFoundException) {
            BuildInfo(
                versionName = "unknown",
                versionCode = 0,
                buildType = "unknown"
            )
        }
    }
    
    private fun getRamMb(): Int {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
            val memoryInfo = android.app.ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)
            (memoryInfo.totalMem / (1024 * 1024)).toInt()
        } catch (e: Exception) {
            0
        }
    }
    
    fun getSessionId(): String = sessionId
}