package io.perfscope.sdk.export

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import io.perfscope.sdk.config.Environment
import io.perfscope.sdk.config.PerfScopeConfig
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * HTTP-based exporter that batches events and sends them to a web endpoint.
 * Designed to be non-blocking and resilient to network failures.
 * Supports retry logic, exponential backoff, and production-ready error handling.
 */
class HttpExporter(
    private val context: Context,
    private val config: PerfScopeConfig
) : PerfScopeExporter {
    
    private val endpoint = config.exportEndpoint ?: throw IllegalArgumentException("Export endpoint is required")
    private val apiKey = config.exportApiKey ?: throw IllegalArgumentException("API key is required")
    private val batchSize = config.exportBatchSize
    private val timeoutMs = config.exportTimeoutMs
    private val maxRetries = config.exportRetryAttempts
    private val retryDelayMs = config.exportRetryDelayMs
    
    private val eventQueue = ConcurrentLinkedQueue<PerfEvent>()
    private val isRunning = AtomicBoolean(true)
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val failedAttempts = AtomicInteger(0)
    
    private var flushJob: Job? = null
    private val flushIntervalMs = if (config.environment == Environment.PRODUCTION) 10000L else 5000L
    
    init {
        startPeriodicFlush()
        logConfiguration()
    }
    
    private fun logConfiguration() {
        if (config.enableNetworkLogs) {
            Log.i("PerfScope", "HttpExporter initialized:")
            Log.i("PerfScope", "  Endpoint: $endpoint")
            Log.i("PerfScope", "  Environment: ${config.environment}")
            Log.i("PerfScope", "  Batch size: $batchSize")
            Log.i("PerfScope", "  Timeout: ${timeoutMs}ms")
            Log.i("PerfScope", "  Max retries: $maxRetries")
        }
    }
    
    override fun send(event: PerfEvent) {
        if (!isRunning.get()) return
        
        if (config.enableDebugLogs) {
            Log.d("PerfScope", "Queuing event: ${event.javaClass.simpleName}")
        }
        eventQueue.offer(event)
        
        // Flush immediately if batch is full
        if (eventQueue.size >= batchSize) {
            if (config.enableDebugLogs) {
                Log.d("PerfScope", "Batch full, flushing ${eventQueue.size} events")
            }
            flushAsync()
        }
    }
    
    override fun flush() {
        flushSync()
    }
    
    override fun close() {
        isRunning.set(false)
        flushJob?.cancel()
        flushSync()
        coroutineScope.cancel()
    }
    
    private fun startPeriodicFlush() {
        flushJob = coroutineScope.launch {
            while (isRunning.get()) {
                delay(flushIntervalMs)
                if (eventQueue.isNotEmpty()) {
                    flushAsync()
                }
            }
        }
    }
    
    private fun flushAsync() {
        if (eventQueue.isEmpty()) return
        
        coroutineScope.launch {
            try {
                sendBatchWithRetry()
                failedAttempts.set(0) // Reset on success
            } catch (e: Exception) {
                val attempts = failedAttempts.incrementAndGet()
                if (config.enableNetworkLogs) {
                    Log.w("PerfScope", "Failed to send events batch (attempt $attempts)", e)
                }
                
                // If too many failures, start dropping events to prevent memory issues
                if (attempts > maxRetries * 3 && eventQueue.size > batchSize * 5) {
                    val dropped = minOf(batchSize, eventQueue.size)
                    repeat(dropped) { eventQueue.poll() }
                    Log.w("PerfScope", "Dropped $dropped events due to persistent failures")
                }
            }
        }
    }
    
    private fun flushSync() {
        if (eventQueue.isEmpty()) return
        
        try {
            runBlocking {
                sendBatchWithRetry()
            }
        } catch (e: Exception) {
            if (config.enableNetworkLogs) {
                Log.w("PerfScope", "Failed to flush events", e)
            }
        }
    }
    
    private suspend fun sendBatchWithRetry() {
        var lastException: Exception? = null
        
        repeat(maxRetries + 1) { attempt ->
            try {
                sendBatch()
                return // Success
            } catch (e: Exception) {
                lastException = e
                
                if (attempt < maxRetries) {
                    val delay = retryDelayMs * (1L shl attempt) // Exponential backoff
                    if (config.enableNetworkLogs) {
                        Log.d("PerfScope", "Retry attempt ${attempt + 1} in ${delay}ms")
                    }
                    delay(delay)
                } else {
                    if (config.enableNetworkLogs) {
                        Log.e("PerfScope", "All retry attempts failed", e)
                    }
                }
            }
        }
        
        throw lastException ?: Exception("Unknown error during batch send")
    }
    
    private suspend fun sendBatch() = withContext(Dispatchers.IO) {
        val events = mutableListOf<PerfEvent>()
        
        // Drain up to batchSize events
        repeat(batchSize) {
            val event = eventQueue.poll() ?: return@repeat
            events.add(event)
        }
        
        if (events.isEmpty()) return@withContext
        
        if (config.enableDebugLogs) {
            Log.d("PerfScope", "Sending batch of ${events.size} events to $endpoint")
        }
        
        try {
            val json = createBatchJson(events)
            sendHttpRequest(json)
            
            if (config.enableDebugLogs) {
                Log.d("PerfScope", "Successfully sent ${events.size} events")
            }
        } catch (e: Exception) {
            if (config.enableNetworkLogs) {
                Log.e("PerfScope", "Failed to send events batch", e)
            }
            // Put events back in queue for retry
            events.reversed().forEach { eventQueue.offer(it) }
            throw e
        }
    }
    
    private fun createBatchJson(events: List<PerfEvent>): String {
        val batch = JSONObject()
        batch.put("api_key", apiKey)
        
        // Create events array properly
        val eventsArray = org.json.JSONArray()
        events.forEach { event ->
            eventsArray.put(eventToJson(event))
        }
        batch.put("events", eventsArray)
        
        return batch.toString()
    }
    
    private fun eventToJson(event: PerfEvent): JSONObject {
        val json = JSONObject()
        
        // Common fields
        json.put("timestamp", event.timestamp)
        json.put("app_id", event.appId)
        json.put("session_id", event.sessionId)
        json.put("screen", event.screen)
        json.put("device", deviceToJson(event.device))
        json.put("build", buildToJson(event.build))
        
        // Event-specific fields
        when (event) {
            is SessionStartEvent -> {
                json.put("event_type", "SESSION_START")
                json.put("sdk_version", event.sdkVersion)
            }
            
            is SessionEndEvent -> {
                json.put("event_type", "SESSION_END")
                json.put("duration_ms", event.durationMs)
                json.put("total_violations", event.totalViolations)
            }
            
            is ScreenChangeEvent -> {
                json.put("event_type", "SCREEN_CHANGE")
                json.put("previous_screen", event.previousScreen)
                json.put("time_on_previous_screen_ms", event.timeOnPreviousScreenMs)
            }
            
            is MemoryViolationEvent -> {
                json.put("event_type", "MEMORY_VIOLATION")
                json.put("violation_type", event.violationType)
                json.put("actual_mb", event.actualMb)
                json.put("budget_mb", event.budgetMb)
                json.put("severity", event.severity)
                json.put("attribution", memoryAttributionToJson(event.attribution))
            }
            
            is JankViolationEvent -> {
                json.put("event_type", "JANK_VIOLATION")
                json.put("violation_type", event.violationType)
                json.put("actual_value", event.actualValue)
                json.put("budget_value", event.budgetValue)
                json.put("severity", event.severity)
                json.put("jank_type", event.jankType)
                json.put("attribution", jankAttributionToJson(event.attribution))
            }
            
            is HealthSnapshotEvent -> {
                json.put("event_type", "HEALTH_SNAPSHOT")
                json.put("memory_mb", event.memoryMb)
                json.put("jank_percent", event.jankPercent)
                json.put("avg_frame_ms", event.avgFrameMs)
                json.put("cpu_percent", event.cpuPercent)
            }
        }
        
        return json
    }
    
    private fun deviceToJson(device: DeviceInfo): JSONObject {
        return JSONObject().apply {
            put("model", device.model)
            put("manufacturer", device.manufacturer)
            put("android_version", device.androidVersion)
            put("ram_mb", device.ramMb)
            put("screen_density", device.screenDensity)
            put("screen_resolution", device.screenResolution)
        }
    }
    
    private fun buildToJson(build: BuildInfo): JSONObject {
        return JSONObject().apply {
            put("version_name", build.versionName)
            put("version_code", build.versionCode)
            put("build_type", build.buildType)
            build.flavor?.let { put("flavor", it) }
        }
    }
    
    private fun memoryAttributionToJson(attribution: MemoryAttributionData): JSONObject {
        return JSONObject().apply {
            put("likely_cause", attribution.likelyCause)
            put("delta_mb", attribution.deltaMb)
            put("details", attribution.details)
        }
    }
    
    private fun jankAttributionToJson(attribution: JankAttributionData): JSONObject {
        return JSONObject().apply {
            put("jank_percent", attribution.jankPercent)
            put("avg_frame_ms", attribution.avgFrameMs)
            put("frame_count", attribution.frameCount)
            put("jank_frame_count", attribution.jankFrameCount)
            put("details", attribution.details)
        }
    }
    
    private fun sendHttpRequest(json: String) {
        if (config.enableNetworkLogs) {
            Log.d("PerfScope", "Sending HTTP request to: $endpoint")
            Log.d("PerfScope", "Request payload: ${json.take(200)}...")
        }
        
        val url = URL(endpoint)
        val connection = url.openConnection() as HttpURLConnection
        
        try {
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("User-Agent", "PerfScope-SDK/1.0 (Android ${Build.VERSION.RELEASE})")
            connection.setRequestProperty("X-API-Key", apiKey)
            connection.setRequestProperty("X-Environment", config.environment.name)
            connection.doOutput = true
            connection.connectTimeout = timeoutMs.toInt()
            connection.readTimeout = timeoutMs.toInt()
            
            // Send request
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(json)
                writer.flush()
            }
            
            // Check response
            val responseCode = connection.responseCode
            if (config.enableNetworkLogs) {
                Log.d("PerfScope", "HTTP response code: $responseCode")
            }
            
            if (responseCode !in 200..299) {
                val errorMessage = try {
                    connection.errorStream?.bufferedReader()?.readText() ?: connection.responseMessage
                } catch (e: Exception) {
                    connection.responseMessage
                }
                
                Log.e("PerfScope", "HTTP error: $responseCode - $errorMessage")
                throw Exception("HTTP $responseCode: $errorMessage")
            }
            
            if (config.enableDebugLogs) {
                Log.d("PerfScope", "HTTP request successful")
            }
            
        } catch (e: Exception) {
            if (config.enableNetworkLogs) {
                Log.e("PerfScope", "HTTP request failed: ${e.message}")
            }
            throw e
        } finally {
            connection.disconnect()
        }
    }
}