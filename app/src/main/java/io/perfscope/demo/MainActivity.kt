package io.perfscope.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.perfscope.demo.ui.theme.PerfScopeSdkTheme
import io.perfscope.sdk.PerfScope
import io.perfscope.sdk.config.PerfScopeConfig

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize PerfScope SDK with deployment-ready configuration
        val config = when {
            // Production deployment examples
            BuildConfig.BUILD_TYPE == "release" -> {
                // Example: Railway deployment
                PerfScopeConfig.railway("your-production-api-key")
                
                // Alternative examples:
                // PerfScopeConfig.render("your-production-api-key")
                // PerfScopeConfig.vercel("your-production-api-key")
                // PerfScopeConfig.production("https://your-backend.com/api/events", "your-api-key")
            }
            
            // Staging deployment
            BuildConfig.BUILD_TYPE == "staging" -> {
                PerfScopeConfig.staging(
                    endpoint = "https://perfscope-backend-staging.railway.app/api/events",
                    apiKey = "staging-api-key"
                )
            }
            
            // Development with local backend
            else -> {
                PerfScopeConfig.withExport(
                    endpoint = "http://192.168.1.10:3001/api/events", // Replace with your local IP
                    apiKey = "demo-api-key-12345"
                ).copy(
                    // Strict budgets for development to catch issues early
                    maxHeapMb = 100,           // Low threshold to trigger violations
                    maxScreenDeltaMb = 15,     // Strict screen memory growth
                    maxBitmapSpikeMb = 10,     // Low bitmap threshold
                    maxCollectionSpikeMb = 8,  // Low collection threshold
                    maxObjectSpikeMb = 5,      // Low object threshold
                    maxJankPercent = 2f,       // Very strict jank budget
                    maxFrameMs = 20f,          // Strict frame time budget
                    maxSevereJankMs = 30f,     // Low severe jank threshold
                    enableViolationAlerts = true,
                    enableFrameMonitoring = true
                )
            }
        }
        
        PerfScope.init(this, config)
        
        enableEdgeToEdge()
        setContent {
            PerfScopeSdkTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DemoScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                
                // Add PerfScope overlay content
                PerfScope.OverlayContent()
            }
        }
    }
}

@Composable
fun DemoScreen(modifier: Modifier = Modifier) {
    var isOverlayVisible by remember { mutableStateOf(false) }
    
    // Set screen name for attribution
    LaunchedEffect(Unit) {
        PerfScope.setCurrentScreen("DemoHome")
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Text(
            text = "PerfScope SDK Demo",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Text(
            text = "Frame/Jank Budget Enforcement Demo",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "This demo enforces both system health (memory) and user experience (frame timing) budgets. Watch violations for jank, not just resource usage.",
            style = MaterialTheme.typography.bodyMedium
        )
        
        Button(
            onClick = {
                if (isOverlayVisible) {
                    PerfScope.hideOverlay()
                    isOverlayVisible = false
                } else {
                    PerfScope.showOverlay()
                    isOverlayVisible = true
                }
            }
        ) {
            Text(if (isOverlayVisible) "Hide PerfScope" else "Show PerfScope")
        }
        
        // Budget Configuration Demo
        Text(
            text = "Budget Configuration Tests:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Button(
            onClick = {
                // Switch to relaxed budgets including frame budgets
                val relaxedConfig = PerfScopeConfig(
                    maxHeapMb = 300,
                    maxScreenDeltaMb = 50,
                    maxBitmapSpikeMb = 40,
                    maxJankPercent = 10f,      // Relaxed jank budget
                    maxFrameMs = 35f,          // Relaxed frame time
                    enableViolationAlerts = true,
                    enableFrameMonitoring = true
                )
                PerfScope.updateBudgetConfig(relaxedConfig)
            }
        ) {
            Text("Switch to Relaxed Budgets")
        }
        
        Button(
            onClick = {
                // Switch to performance-critical budgets
                val criticalConfig = PerfScopeConfig.performanceCritical()
                PerfScope.updateBudgetConfig(criticalConfig)
            }
        ) {
            Text("Switch to Performance-Critical")
        }
        
        // Violation Test Buttons
        Text(
            text = "Memory Violation Tests:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Button(
            onClick = {
                PerfScope.setCurrentScreen("BitmapViolationTest")
                // Create large bitmap allocations to exceed budget
                Thread {
                    val bitmaps = mutableListOf<ByteArray>()
                    repeat(30) {
                        // 2MB each - will exceed bitmap budget
                        bitmaps.add(ByteArray(2 * 1024 * 1024))
                        Thread.sleep(100)
                    }
                    println("Created ${bitmaps.size} large bitmaps - should trigger violation")
                }.start()
            }
        ) {
            Text("🚨 Trigger Bitmap Violation")
        }
        
        Button(
            onClick = {
                PerfScope.setCurrentScreen("CollectionViolationTest")
                // Create large collections to exceed budget
                Thread {
                    val largeList = mutableListOf<String>()
                    repeat(200000) {
                        largeList.add("Large item $it with lots of data to increase memory usage significantly")
                        if (it % 20000 == 0) Thread.sleep(50)
                    }
                    println("Created list with ${largeList.size} items - should trigger violation")
                }.start()
            }
        ) {
            Text("🚨 Trigger Collection Violation")
        }
        
        // Frame/Jank Violation Tests
        Text(
            text = "Frame/Jank Violation Tests:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Button(
            onClick = {
                PerfScope.setCurrentScreen("MainThreadBlockTest")
                // Simulate main thread blocking
                Thread.sleep(100) // Block main thread for 100ms - will cause severe jank
            }
        ) {
            Text("🚨 Trigger Main Thread Block")
        }
        
        Button(
            onClick = {
                PerfScope.setCurrentScreen("LayoutThrashTest")
                // Simulate layout thrashing by triggering many recompositions
                repeat(50) {
                    // This would cause layout calculations in a real scenario
                    Thread.sleep(5) // Small delays that add up
                }
            }
        ) {
            Text("🚨 Trigger Layout Thrash")
        }
        
        Button(
            onClick = {
                PerfScope.setCurrentScreen("ComposeJankTest")
                // Simulate heavy Compose operations
                val heavyList = (1..10000).map { "Heavy item $it" }
                println("Created heavy list: ${heavyList.size} items")
            }
        ) {
            Text("🚨 Trigger Compose Jank")
        }
        
        Button(
            onClick = {
                PerfScope.setCurrentScreen("DemoHome")
            }
        ) {
            Text("Reset to Home Screen")
        }
    }
}