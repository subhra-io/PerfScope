'use client'

import { useState, useEffect } from 'react'
import { DashboardOverview } from '@/components/dashboard/DashboardOverview'
import { RealtimeViolations } from '@/components/dashboard/RealtimeViolations'
// import { PerformanceMetrics } from '@/components/dashboard/PerformanceMetrics'
import { SessionsList } from '@/components/dashboard/SessionsList'
import { useRealtimeEvents } from '@/hooks/useRealtimeEvents'
import { useDashboardData } from '@/hooks/useDashboardData'

export default function DashboardPage() {
  const { data: dashboardData, isLoading } = useDashboardData()
  const { events: realtimeEvents, isConnected } = useRealtimeEvents()
  const [selectedTimeRange, setSelectedTimeRange] = useState('24h')

  return (
    <div className="p-6 space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Performance Dashboard</h1>
          <p className="text-gray-600 mt-1">
            Real-time monitoring and analytics for your mobile applications
          </p>
        </div>
        
        <div className="flex items-center space-x-4">
          {/* Connection Status */}
          <div className={`flex items-center space-x-2 px-3 py-1 rounded-full text-sm font-medium ${
            isConnected ? 'bg-success-100 text-success-700' : 'bg-danger-100 text-danger-700'
          }`}>
            <div className={`w-2 h-2 rounded-full ${
              isConnected ? 'bg-success-500 animate-pulse-slow' : 'bg-danger-500'
            }`} />
            <span>{isConnected ? 'Live' : 'Disconnected'}</span>
          </div>
          
          {/* Time Range Selector */}
          <select
            value={selectedTimeRange}
            onChange={(e) => setSelectedTimeRange(e.target.value)}
            className="px-3 py-2 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-primary-500"
          >
            <option value="1h">Last Hour</option>
            <option value="24h">Last 24 Hours</option>
            <option value="7d">Last 7 Days</option>
            <option value="30d">Last 30 Days</option>
          </select>
        </div>
      </div>

      {/* Dashboard Overview */}
      <DashboardOverview 
        data={dashboardData} 
        isLoading={isLoading}
        timeRange={selectedTimeRange}
      />

      {/* Real-time Violations */}
      <RealtimeViolations 
        events={realtimeEvents}
        isConnected={isConnected}
      />

      {/* Performance Metrics Charts */}
      {/* <PerformanceMetrics 
        timeRange={selectedTimeRange}
      /> */}
      
      <div className="card">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">Performance Metrics</h3>
        <p className="text-gray-500">Charts will be displayed here showing memory usage, jank analysis, and violation trends over time.</p>
      </div>

      {/* Recent Sessions */}
      <SessionsList 
        timeRange={selectedTimeRange}
      />
    </div>
  )
}