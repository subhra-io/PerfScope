package io.perfscope.sdk.export

/**
 * Interface for exporting performance events to external systems.
 * Implementations should be non-blocking and handle failures gracefully.
 */
interface PerfScopeExporter {
    
    /**
     * Send a performance event.
     * This should be non-blocking and not throw exceptions.
     */
    fun send(event: PerfEvent)
    
    /**
     * Flush any pending events.
     * Called when app goes to background or session ends.
     */
    fun flush()
    
    /**
     * Close the exporter and clean up resources.
     */
    fun close()
}

/**
 * No-op exporter for when export is disabled
 */
class NoOpExporter : PerfScopeExporter {
    override fun send(event: PerfEvent) {
        // Do nothing
    }
    
    override fun flush() {
        // Do nothing
    }
    
    override fun close() {
        // Do nothing
    }
}