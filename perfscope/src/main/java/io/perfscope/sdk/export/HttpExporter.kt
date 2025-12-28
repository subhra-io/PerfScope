package io.perfscope.sdk.export

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

/**
 * HTTP-based exporter that batches events and sends them to a web endpoint.
 * Designed to be non-blocking and resilient to network failures.
 */
class HttpExporter(
    private val context: Context,
    private val endpoint: String,
    private val apiKey: String,
    private val batchSize: Int = 10,
    private val flushIntervalMs: Long = 5000 // 5 seconds
) : PerfScopeExporter {
    
    private val eventQueue = ConcurrentLinkedQueue<PerfEvent>()
    private val isRunning = AtomicBoolean(true)
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private var flushJob: Job? = null
    
    init {
        startPeriodicFlush()
    }
    
    override fun send(event: PerfEvent) {
        if (!isRunning.get()) return
        
        Log.d("PerfScope", "Queuing event: ${event.javaClass.simpleName}")
        eventQueue.offer(event)
        
        // Flush immediately if batch is full
        if (eventQueue.size >= batchSize) {
            Log.d("PerfScope", "Batch full, flushing ${eventQueue.size} events")
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
                flushAsync()
            }
        }
    }
    
    private fun flushAsync() {
        if (eventQueue.isEmpty()) return
        
        coroutineScope.launch {
            try {
                sendBatch()
            } catch (e: Exception) {
                Log.w("PerfScope", "Failed to send events batch", e)
            }
        }
    }
    
    private fun flushSync() {
        if (eventQueue.isEmpty()) return
        
        try {
            runBlocking {
                sendBatch()
            }
        } catch (e: Exception) {
            Log.w("PerfScope", "Failed to flush events", e)
        }
    }
    
    private suspend fun sendBatch() = withContext(Dispatchers.IO) {
        val events = mutableListOf<PerfEvent>()
        
        // Drain up to batchSize events
        repeat(batchSize) {
            val event = eventQueue.poll() ?: return@repeat
            events.add(event)
        }
        
        if (events.isEmpty()) return@withContext
        
        Log.d("PerfScope", "Sending batch of ${events.size} events to $endpoint")
        
        try {
            val json = createBatchJson(events)
            sendHttpRequest(json)
            Log.d("PerfScope", "Successfully sent ${events.size} events")
        } catch (e: Exception) {
            Log.e("PerfScope", "Failed to send events batch", e)
            // Put events back in queue for retry (simple strategy)
            events.forEach { eventQueue.offer(it) }
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
        Log.d("PerfScope", "Sending HTTP request to: $endpoint")
        Log.d("PerfScope", "Request payload: ${json.take(200)}...")
        
        val url = URL(endpoint)
        val connection = url.openConnection() as HttpURLConnection
        
        try {
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("User-Agent", "PerfScope-SDK/1.0")
            connection.doOutput = true
            connection.connectTimeout = 10000 // 10 seconds
            connection.readTimeout = 10000
            
            // Send request
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(json)
                writer.flush()
            }
            
            // Check response
            val responseCode = connection.responseCode
            Log.d("PerfScope", "HTTP response code: $responseCode")
            
            if (responseCode !in 200..299) {
                val errorMessage = connection.responseMessage
                Log.e("PerfScope", "HTTP error: $responseCode - $errorMessage")
                throw Exception("HTTP $responseCode: $errorMessage")
            }
            
            Log.d("PerfScope", "HTTP request successful")
            
        } catch (e: Exception) {
            Log.e("PerfScope", "HTTP request failed", e)
            throw e
        } finally {
            connection.disconnect()
        }
    }
}