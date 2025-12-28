package io.perfscope.sdk.data

/**
 * Represents memory attribution data for a specific screen/context
 */
data class MemoryAttribution(
    val screenName: String,
    val memoryDeltaMb: Int,
    val likelyCause: MemorySpike,
    val timestamp: Long = System.currentTimeMillis(),
    val details: String = ""
)

/**
 * Classification of memory spikes by type
 */
enum class MemorySpike(val displayName: String, val description: String) {
    BITMAP_HEAVY("Bitmap allocations", "Large image or graphics allocations"),
    COLLECTION_HEAVY("Collection growth", "Lists, maps, or arrays growing in size"),
    NATIVE_HEAVY("Native memory", "JNI or native library allocations"),
    OBJECT_HEAVY("Object creation", "Many small objects created"),
    UNKNOWN("Unknown cause", "Memory increase detected but cause unclear"),
    NORMAL("Normal usage", "Expected memory usage pattern")
}

/**
 * Memory sample at a specific point in time
 */
data class MemorySample(
    val memoryMb: Int,
    val timestamp: Long,
    val screenName: String,
    val gcCount: Long = 0,
    val nativeMemoryMb: Int = 0
)