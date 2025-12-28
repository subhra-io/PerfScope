'use client'

import useSWR from 'swr'

interface DashboardData {
  total_events: number
  summary: {
    violations: number
    sessions: number
    screen_changes: number
    health_snapshots: number
  }
  apps: Array<{
    id: string
    name: string
    package_id: string
    violations: number
    sessions: number
  }>
}

const fetcher = async (url: string): Promise<DashboardData> => {
  const response = await fetch(url)
  if (!response.ok) {
    throw new Error('Failed to fetch dashboard data')
  }
  const data = await response.json()
  
  // Transform the API response to match our interface
  return {
    total_events: data.total_events || 0,
    summary: data.summary || {
      violations: 0,
      sessions: 0,
      screen_changes: 0,
      health_snapshots: 0
    },
    apps: [
      {
        id: 'demo-app',
        name: 'PerfScope Demo (OnePlus CPH2691)',
        package_id: 'io.perfscope.demo',
        violations: data.summary?.violations || 0,
        sessions: data.summary?.sessions || 0
      }
    ]
  }
}

export function useDashboardData() {
  const { data, error, isLoading, mutate } = useSWR<DashboardData>(
    `${process.env.NEXT_PUBLIC_API_URL}/api/events/received`,
    fetcher,
    {
      refreshInterval: 30000, // Refresh every 30 seconds
      revalidateOnFocus: true,
      revalidateOnReconnect: true,
    }
  )

  return {
    data,
    error,
    isLoading,
    refresh: mutate
  }
}