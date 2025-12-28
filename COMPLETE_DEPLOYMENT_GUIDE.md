# 🚀 PerfScope - Complete Production Deployment Guide

This guide covers the complete deployment of the PerfScope performance monitoring platform across all components.

## 📋 Overview

PerfScope is a production-ready performance monitoring platform consisting of:

- **📱 Android SDK** - Real-time performance monitoring library
- **🖥️ Backend API** - Node.js + TypeScript + PostgreSQL event processing
- **📊 Web Dashboard** - React + Next.js real-time monitoring interface

## 🎯 Quick Deploy (Recommended)

### 1. Deploy Backend (Choose One)

**Railway (Recommended):**
[![Deploy on Railway](https://railway.app/button.svg)](https://railway.app/template/perfscope-backend)

**Render:**
[![Deploy to Render](https://render.com/images/deploy-to-render-button.svg)](https://render.com/deploy?repo=https://github.com/subhra-io/PerfScope)

### 2. Deploy Frontend

**Vercel:**
[![Deploy with Vercel](https://vercel.com/button)](https://vercel.com/new/clone?repository-url=https://github.com/subhra-io/PerfScope&project-name=perfscope-dashboard&repository-name=perfscope-dashboard&root-directory=web-portal)

### 3. Integrate Android SDK

```kotlin
// In your Android app
val config = PerfScopeConfig.railway("your-api-key")
PerfScope.init(this, config)
```

## 🏗️ Detailed Deployment Steps

### Step 1: Backend Deployment

#### Option A: Railway (Recommended)

1. **Fork the Repository**
   - Fork https://github.com/subhra-io/PerfScope to your GitHub

2. **Deploy to Railway**
   - Go to [Railway](https://railway.app)
   - Create new project from GitHub repo
   - Select the forked repository
   - Railway auto-detects the backend configuration

3. **Add PostgreSQL**
   - Add PostgreSQL service to your Railway project
   - Environment variables are auto-configured

4. **Get Your URLs**
   - Backend URL: `https://perfscope-backend.railway.app`
   - Database: Automatically provisioned

#### Option B: Render

1. **Deploy Backend**
   - Go to [Render](https://render.com)
   - Create web service from GitHub repo
   - Use `backend/render.yaml` configuration

2. **Add Database**
   - Create PostgreSQL database
   - Connection string auto-configured

#### Option C: Vercel (Serverless)

1. **Deploy Functions**
   ```bash
   cd backend
   vercel
   ```

2. **Add External Database**
   - Use Supabase, Neon, or PlanetScale
   - Set `DATABASE_URL` environment variable

### Step 2: Frontend Deployment

#### Vercel Deployment

1. **Deploy Dashboard**
   - Go to [Vercel](https://vercel.com)
   - Import your GitHub repository
   - Set root directory to `web-portal`

2. **Configure Environment Variables**
   ```bash
   NEXT_PUBLIC_API_URL=https://your-backend-url.com
   NEXT_PUBLIC_WS_URL=wss://your-backend-url.com
   ```

3. **Deploy**
   - Vercel automatically builds and deploys
   - Get your dashboard URL: `https://perfscope-dashboard.vercel.app`

### Step 3: Android SDK Integration

#### Add to Your Project

1. **Copy SDK Module**
   ```bash
   # Copy the perfscope module to your project
   cp -r perfscope/ /path/to/your/android/project/
   ```

2. **Update settings.gradle.kts**
   ```kotlin
   include(":perfscope")
   ```

3. **Add Dependency**
   ```kotlin
   // In your app/build.gradle.kts
   dependencies {
       implementation(project(":perfscope"))
   }
   ```

4. **Add Permissions**
   ```xml
   <!-- In AndroidManifest.xml -->
   <uses-permission android:name="android.permission.INTERNET" />
   <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
   ```

#### Configure SDK

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        val config = when (BuildConfig.BUILD_TYPE) {
            "release" -> {
                // Production with your deployed backend
                PerfScopeConfig.railway("your-production-api-key")
                // Or: PerfScopeConfig.render("your-api-key")
                // Or: PerfScopeConfig.vercel("your-api-key")
            }
            "staging" -> {
                PerfScopeConfig.staging(
                    endpoint = "https://your-staging-backend.com/api/events",
                    apiKey = "staging-api-key"
                )
            }
            else -> {
                PerfScopeConfig.development()
            }
        }
        
        PerfScope.init(this, config)
    }
}
```

#### Add to Your UI

```kotlin
@Composable
fun YourApp() {
    // Your app content
    YourAppContent()
    
    // Add PerfScope overlay
    PerfScope.OverlayContent()
}

@Composable
fun YourScreen() {
    // Set screen name for attribution
    LaunchedEffect(Unit) {
        PerfScope.setCurrentScreen("YourScreen")
    }
    
    // Your screen content
}
```

## 🔧 Environment Configuration

### Backend Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `NODE_ENV` | Environment | `production` |
| `DATABASE_URL` | PostgreSQL connection | `postgresql://user:pass@host:5432/db` |
| `CORS_ORIGIN` | Frontend URL | `https://perfscope-dashboard.vercel.app` |
| `API_KEY` | API authentication | `your-secure-api-key` |
| `PORT` | Server port | `3001` |

### Frontend Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `NEXT_PUBLIC_API_URL` | Backend API URL | `https://perfscope-backend.railway.app` |
| `NEXT_PUBLIC_WS_URL` | WebSocket URL | `wss://perfscope-backend.railway.app` |

### Android SDK Configuration

```kotlin
// Development
PerfScopeConfig.development()

// Staging
PerfScopeConfig.staging(endpoint, apiKey)

// Production - Railway
PerfScopeConfig.railway(apiKey)

// Production - Render
PerfScopeConfig.render(apiKey)

// Production - Vercel
PerfScopeConfig.vercel(apiKey)

// Custom
PerfScopeConfig.production(endpoint, apiKey)
```

## 📊 What You Get

### Real-Time Monitoring
- **Memory Attribution**: Automatic detection of memory leaks and spikes
- **Frame/Jank Attribution**: Frame drop analysis and root cause identification
- **Performance Budget Enforcement**: Configurable thresholds with violations
- **Screen-Level Tracking**: Performance issues tied to specific app screens

### Professional Dashboard
- **Live Violation Alerts**: Real-time performance issue notifications
- **Session Analytics**: User session analysis and patterns
- **Device Insights**: Performance across different devices and Android versions
- **Historical Trends**: Performance metrics over time

### Production-Ready Infrastructure
- **Scalable Backend**: Auto-scaling Node.js API with PostgreSQL
- **Global CDN**: Vercel edge network for dashboard delivery
- **Monitoring & Logging**: Built-in health checks and structured logging
- **Security**: Rate limiting, CORS, security headers

## 🔍 Testing Your Deployment

### 1. Test Backend Health

```bash
curl https://your-backend-url.com/health
# Should return: {"status":"ok","timestamp":"..."}
```

### 2. Test Dashboard

- Visit your dashboard URL
- Should show "Waiting for data..." initially
- Real-time connection status indicator

### 3. Test Android Integration

```kotlin
// Add test button to trigger violations
Button(onClick = {
    // This will trigger memory violation and send to backend
    val largeArray = ByteArray(100 * 1024 * 1024) // 100MB
}) {
    Text("Test Integration")
}
```

### 4. Verify Data Flow

1. **Android App** → Triggers violation
2. **Backend API** → Receives event data
3. **Web Dashboard** → Shows real-time violation alert

## 🚨 Troubleshooting

### Common Issues

**Backend Not Accessible**
- Check deployment logs in Railway/Render/Vercel
- Verify environment variables are set
- Test health endpoint: `/health`

**Dashboard Shows No Data**
- Check `NEXT_PUBLIC_API_URL` is correct
- Verify CORS settings in backend
- Check browser network tab for API calls

**Android SDK Not Sending Data**
- Enable debug logs: `enableDebugLogs = true`
- Check network permissions in AndroidManifest.xml
- Verify API endpoint is accessible from device
- Check logcat for PerfScope logs

**Database Connection Issues**
- Verify `DATABASE_URL` format
- Check database is running and accessible
- Run database migrations if needed

### Debug Commands

```bash
# Check backend logs
railway logs # For Railway
render logs # For Render

# Check Android logs
adb logcat | grep PerfScope

# Test API endpoint
curl -X POST https://your-backend.com/api/events \
  -H "Content-Type: application/json" \
  -H "X-API-Key: your-api-key" \
  -d '{"test": "data"}'
```

## 📈 Scaling & Optimization

### Backend Scaling
- **Railway**: Automatic scaling based on traffic
- **Render**: Horizontal scaling with load balancer
- **Database**: Connection pooling and read replicas

### Frontend Optimization
- **Vercel**: Global edge network and automatic optimization
- **Image Optimization**: Built-in Next.js image optimization
- **Code Splitting**: Automatic bundle optimization

### Android SDK Optimization
- **Batch Size Tuning**: Adjust based on app traffic
- **Network Optimization**: Configure timeouts and retries
- **Memory Management**: Efficient event queuing

## 🎯 Production Checklist

### Backend
- ✅ Environment variables configured
- ✅ Database migrations run
- ✅ Health checks passing
- ✅ CORS configured for frontend domain
- ✅ Rate limiting enabled
- ✅ Logging and monitoring set up

### Frontend
- ✅ Environment variables set
- ✅ API connection working
- ✅ WebSocket connection established
- ✅ Custom domain configured (optional)
- ✅ Analytics enabled (optional)

### Android SDK
- ✅ Network permissions added
- ✅ Production configuration set
- ✅ Screen tracking implemented
- ✅ Debug logs disabled in release
- ✅ API key secured

## 🎉 Success!

Your complete PerfScope platform is now deployed and ready for production use!

**Architecture:**
- ✅ **Android SDK** monitoring app performance
- ✅ **Backend API** processing and storing events
- ✅ **Web Dashboard** providing real-time insights
- ✅ **Database** storing performance data
- ✅ **CDN** delivering dashboard globally

**Features:**
- ✅ Real-time performance monitoring
- ✅ Automatic violation detection
- ✅ Screen-level attribution
- ✅ Historical analytics
- ✅ Multi-device support
- ✅ Production-ready scaling

**Next Steps:**
1. Monitor your app's performance in real-time
2. Analyze patterns and optimize based on data
3. Set up alerts for critical performance issues
4. Scale infrastructure as your user base grows

---

**Support:** Check deployment logs and health endpoints for troubleshooting. The platform is designed to be self-healing and production-ready out of the box.