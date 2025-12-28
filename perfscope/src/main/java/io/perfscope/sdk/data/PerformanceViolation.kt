package io.perfscope.sdk.data

/**
 * Represents a performance budget violation
 */
data class PerformanceViolation(
    val type: ViolationType,
    val screenName: String,
    val actualValue: Int,
    val budgetValue: Int,
    val severity: ViolationSeverity,
    val recommendation: String,
    val timestamp: Long = System.currentTimeMillis(),
    val memoryAttribution: MemoryAttribution? = null
)

/**
 * Types of performance violations
 */
enum class ViolationType(val displayName: String) {
    HEAP_MEMORY("Heap Memory"),
    SCREEN_MEMORY_DELTA("Screen Memory Growth"),
    BITMAP_SPIKE("Bitmap Memory Spike"),
    COLLECTION_SPIKE("Collection Memory Spike"),
    OBJECT_SPIKE("Object Memory Spike"),
    NATIVE_SPIKE("Native Memory Spike"),
    FRAME_RATE("Frame Rate Drop"),
    JANK_PERCENT("Jank Percentage"),
    FRAME_TIME("Frame Time"),
    SEVERE_JANK("Severe Jank"),
    CPU_USAGE("CPU Usage"),
    APP_SIZE("App Size")
}

/**
 * Severity levels for violations
 */
enum class ViolationSeverity(val displayName: String, val priority: Int) {
    LOW("Low", 1),
    MEDIUM("Medium", 2),
    HIGH("High", 3),
    CRITICAL("Critical", 4);
    
    companion object {
        fun fromExcess(actualValue: Int, budgetValue: Int): ViolationSeverity {
            val excessPercent = ((actualValue - budgetValue).toFloat() / budgetValue) * 100
            return when {
                excessPercent >= 100 -> CRITICAL  // 2x over budget
                excessPercent >= 50 -> HIGH       // 1.5x over budget
                excessPercent >= 25 -> MEDIUM     // 1.25x over budget
                else -> LOW                       // Just over budget
            }
        }
    }
}

/**
 * Violation history entry for tracking patterns
 */
data class ViolationHistoryEntry(
    val violation: PerformanceViolation,
    val resolvedAt: Long? = null,
    val isRecurring: Boolean = false
)