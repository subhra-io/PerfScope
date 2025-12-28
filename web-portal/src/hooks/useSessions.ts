'use client'

import useSWR from 'swr'

interface Session {
  id: string
  app_id: string
  app_name: string
  session_id: string
  started_at: number
  ended_at: number | null
  duration_ms: number | null
  violations: number
  screens: string[]
  device: {
    model: string
    manufacturer: string
    android_version: number
  }
  build: {
    version_name: string
    version_code: number
  }
  status: 'active' | 'completed'
}

const fetcher = async (url: string): Promise<Session[]> => {
  // For now, return mock data since we don't have sessions endpoint yet
  // In production, this would fetch from /api/sessions?timeRange={timeRange}
  
  const now = Date.now()
  
  return [
    {
      id: 'session-1',
      app_id: 'demo-app',
      app_name: 'PerfScope Demo',
      session_id: 'android-demo-1766860641',
      started_at: now - 3600000, // 1 hour ago
      ended_at: now - 3555000,
      duration_ms: 45000,
      violations: 2,
      screens: ['MainActivity', 'BitmapViolationTest', 'MainThreadBlockTest'],
      device: {
        model: 'Pixel 6',
        manufacturer: 'Google',
        android_version: 34
      },
      build: {
        version_name: '1.0.0',
        version_code: 1
      },
      status: 'completed'
    },
    {
      id: 'session-2',
      app_id: 'demo-app',
      app_name: 'PerfScope Demo',
      session_id: 'android-demo-1766860500',
      started_at: now - 7200000, // 2 hours ago
      ended_at: now - 7140000,
      duration_ms: 60000,
      violations: 0,
      screens: ['MainActivity', 'SettingsScreen'],
      device: {
        model: 'Galaxy S23',
        manufacturer: 'Samsung',
        android_version: 33
      },
      build: {
        version_name: '1.0.0',
        version_code: 1
      },
      status: 'completed'
    },
    {
      id: 'session-3',
      app_id: 'demo-app',
      app_name: 'PerfScope Demo',
      session_id: 'android-demo-1766860300',
      started_at: now - 10800000, // 3 hours ago
      ended_at: null, // Still active
      duration_ms: null,
      violations: 1,
      screens: ['MainActivity', 'ProfileScreen'],
      device: {
        model: 'OnePlus 11',
        manufacturer: 'OnePlus',
        android_version: 34
      },
      build: {
        version_name: '1.0.0',
        version_code: 1
      },
      status: 'active'
    }
  ]
}

export function useSessions(timeRange: string) {
  const { data, error, isLoading, mutate } = useSWR<Session[]>(
    `sessions-${timeRange}`,
    () => fetcher(`/api/sessions?timeRange=${timeRange}`),
    {
      refreshInterval: 30000, // Refresh every 30 seconds
      revalidateOnFocus: true,
    }
  )

  return {
    data,
    error,
    isLoading,
    refresh: mutate
  }
}