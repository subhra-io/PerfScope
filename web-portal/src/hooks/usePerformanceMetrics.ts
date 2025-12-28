'use client'

import useSWR from 'swr'

interface PerformanceMetrics {
  memory: Array<{
    timestamp: number
    value: number
    budget: number
  }>
  jank: Array<{
    timestamp: number
    value: number
    budget: number
  }>
  violations: Array<{
    timestamp: number
    memory_violations: number
    jank_violations: number
  }>
}

const fetcher = async (url: string): Promise<PerformanceMetrics> => {
  // For now, return mock data since we don't have time-series endpoints yet
  // In production, this would fetch from /api/metrics/{timeRange}
  
  const now = Date.now()
  const hoursBack = 24
  const interval = (hoursBack * 60 * 60 * 1000) / 7 // 7 data points
  
  return {
    memory: Array.from({ length: 7 }, (_, i) => ({
      timestamp: now - (hoursBack * 60 * 60 * 1000) + (i * interval),
      value: 80 + Math.random() * 40, // 80-120 MB
      budget: 100
    })),
    jank: Array.from({ length: 7 }, (_, i) => ({
      timestamp: now - (hoursBack * 60 * 60 * 1000) + (i * interval),
      value: Math.random() * 4, // 0-4% jank
      budget: 2
    })),
    violations: Array.from({ length: 7 }, (_, i) => ({
      timestamp: now - (hoursBack * 60 * 60 * 1000) + (i * interval),
      memory_violations: Math.floor(Math.random() * 3),
      jank_violations: Math.floor(Math.random() * 2)
    }))
  }
}

export function usePerformanceMetrics(timeRange: string) {
  const { data, error, isLoading, mutate } = useSWR<PerformanceMetrics>(
    `performance-metrics-${timeRange}`,
    () => fetcher(`/api/metrics/${timeRange}`),
    {
      refreshInterval: 60000, // Refresh every minute
      revalidateOnFocus: false,
    }
  )

  return {
    data,
    error,
    isLoading,
    refresh: mutate
  }
}