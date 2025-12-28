# PerfScope Dashboard - Vercel Deployment Guide

This guide will help you deploy the PerfScope web dashboard to Vercel.

## Prerequisites

1. **Vercel Account**: Sign up at [vercel.com](https://vercel.com)
2. **GitHub Repository**: Your PerfScope repository should be on GitHub
3. **Backend API**: Deploy your backend first (see backend/README.md)

## Quick Deploy

[![Deploy with Vercel](https://vercel.com/button)](https://vercel.com/new/clone?repository-url=https://github.com/subhra-io/PerfScope&project-name=perfscope-dashboard&repository-name=perfscope-dashboard&root-directory=web-portal)

## Manual Deployment Steps

### 1. Connect Repository to Vercel

1. Go to [vercel.com/dashboard](https://vercel.com/dashboard)
2. Click "New Project"
3. Import your GitHub repository
4. Select the `web-portal` folder as the root directory

### 2. Configure Build Settings

Vercel should auto-detect Next.js, but verify these settings:

- **Framework Preset**: Next.js
- **Root Directory**: `web-portal`
- **Build Command**: `npm run build`
- **Output Directory**: `.next`
- **Install Command**: `npm install`

### 3. Set Environment Variables

In your Vercel project settings, add these environment variables:

```bash
NEXT_PUBLIC_API_URL=https://your-backend-api.com
NEXT_PUBLIC_WS_URL=wss://your-backend-api.com
```

**Important**: Replace with your actual backend URLs.

### 4. Deploy

Click "Deploy" and wait for the build to complete.

## Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `NEXT_PUBLIC_API_URL` | Backend API endpoint | `https://api.perfscope.com` |
| `NEXT_PUBLIC_WS_URL` | WebSocket endpoint for real-time updates | `wss://api.perfscope.com` |

## Backend Integration

The dashboard expects these API endpoints:

- `GET /api/events` - Performance events
- `GET /api/sessions` - User sessions
- `GET /api/metrics` - Performance metrics
- `WebSocket /` - Real-time updates

Make sure your backend is deployed and accessible before deploying the dashboard.

## Custom Domain (Optional)

1. Go to your Vercel project settings
2. Navigate to "Domains"
3. Add your custom domain
4. Update DNS records as instructed

## Troubleshooting

### Build Errors

- Check that all dependencies are in `package.json`
- Verify TypeScript types are correct
- Ensure environment variables are set

### API Connection Issues

- Verify `NEXT_PUBLIC_API_URL` is correct
- Check CORS settings in your backend
- Ensure backend is deployed and accessible

### Real-time Updates Not Working

- Verify `NEXT_PUBLIC_WS_URL` is correct
- Check WebSocket connection in browser dev tools
- Ensure backend WebSocket server is running

## Performance Optimization

The dashboard is optimized for production with:

- Static generation where possible
- Image optimization
- Code splitting
- Compression
- CDN delivery via Vercel Edge Network

## Monitoring

Monitor your deployment with:

- Vercel Analytics (built-in)
- Performance metrics in Vercel dashboard
- Error tracking via Vercel Functions logs

## Support

For deployment issues:
1. Check Vercel deployment logs
2. Review browser console for errors
3. Verify backend connectivity
4. Check environment variables

---

**Next Steps**: After deployment, test the dashboard with real Android SDK data to ensure end-to-end functionality.