'use client'

import { useState, useEffect } from 'react'
import { 
  LineChart, 
  Line, 
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip, 
  ResponsiveContainer,
  AreaChart,
  Area,
  BarChart,
  Bar
} from 'recharts'
import { TrendingUp, Memory, Zap, Activity } from 'lucide-react'
// import { usePerformanceMetrics } from '@/hooks/usePerformanceMetrics'

interface Props {
  timeRange: string
}

export function PerformanceMetrics({ timeRange }: Props) {
  // const { data: metricsData, isLoading } = usePerformanceMetrics(timeRange)
  const [activeTab, setActiveTab] = useState<'memory' | 'jank' | 'violations'>('memory')
  const isLoading = false // Temporary for demo

  const tabs = [
    { id: 'memory', label: 'Memory Usage', icon: Memory },
    { id: 'jank', label: 'Jank Analysis', icon: Zap },
    { id: 'violations', label: 'Violations', icon: Activity },
  ]

  // Mock data for demonstration
  const mockMemoryData = [
    { time: '00:00', memory: 85, budget: 100 },
    { time: '04:00', memory: 92, budget: 100 },
    { time: '08:00', memory: 78, budget: 100 },
    { time: '12:00', memory: 105, budget: 100 }, // Violation
    { time: '16:00', memory: 88, budget: 100 },
    { time: '20:00', memory: 95, budget: 100 },
    { time: '24:00', memory: 82, budget: 100 },
  ]

  const mockJankData = [
    { time: '00:00', jank: 1.2, budget: 2.0 },
    { time: '04:00', jank: 0.8, budget: 2.0 },
    { time: '08:00', jank: 1.5, budget: 2.0 },
    { time: '12:00', jank: 3.2, budget: 2.0 }, // Violation
    { time: '16:00', jank: 1.1, budget: 2.0 },
    { time: '20:00', jank: 2.8, budget: 2.0 }, // Violation
    { time: '24:00', jank: 0.9, budget: 2.0 },
  ]

  const mockViolationData = [
    { time: '00:00', memory: 0, jank: 0 },
    { time: '04:00', memory: 0, jank: 0 },
    { time: '08:00', memory: 1, jank: 0 },
    { time: '12:00', memory: 2, jank: 1 },
    { time: '16:00', memory: 0, jank: 0 },
    { time: '20:00', memory: 1, jank: 2 },
    { time: '24:00', memory: 0, jank: 0 },
  ]

  const renderChart = () => {
    if (isLoading) {
      return (
        <div className="h-80 flex items-center justify-center">
          <div className="loading-shimmer w-full h-full rounded" />
        </div>
      )
    }

    switch (activeTab) {
      case 'memory':
        return (
          <ResponsiveContainer width="100%" height={320}>
            <AreaChart data={mockMemoryData}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis 
                dataKey="time" 
                stroke="#6b7280"
                fontSize={12}
              />
              <YAxis 
                stroke="#6b7280"
                fontSize={12}
                label={{ value: 'Memory (MB)', angle: -90, position: 'insideLeft' }}
              />
              <Tooltip 
                contentStyle={{
                  backgroundColor: 'white',
                  border: '1px solid #e5e7eb',
                  borderRadius: '8px',
                  boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)'
                }}
              />
              <Area
                type="monotone"
                dataKey="budget"
                stroke="#f59e0b"
                fill="transparent"
                strokeDasharray="5 5"
                strokeWidth={2}
                name="Budget"
              />
              <Area
                type="monotone"
                dataKey="memory"
                stroke="#3b82f6"
                fill="#3b82f6"
                fillOpacity={0.1}
                strokeWidth={2}
                name="Memory Usage"
              />
            </AreaChart>
          </ResponsiveContainer>
        )

      case 'jank':
        return (
          <ResponsiveContainer width="100%" height={320}>
            <LineChart data={mockJankData}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis 
                dataKey="time" 
                stroke="#6b7280"
                fontSize={12}
              />
              <YAxis 
                stroke="#6b7280"
                fontSize={12}
                label={{ value: 'Jank (%)', angle: -90, position: 'insideLeft' }}
              />
              <Tooltip 
                contentStyle={{
                  backgroundColor: 'white',
                  border: '1px solid #e5e7eb',
                  borderRadius: '8px',
                  boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)'
                }}
              />
              <Line
                type="monotone"
                dataKey="budget"
                stroke="#f59e0b"
                strokeDasharray="5 5"
                strokeWidth={2}
                dot={false}
                name="Budget"
              />
              <Line
                type="monotone"
                dataKey="jank"
                stroke="#ef4444"
                strokeWidth={2}
                dot={{ fill: '#ef4444', strokeWidth: 2, r: 4 }}
                name="Jank Percentage"
              />
            </LineChart>
          </ResponsiveContainer>
        )

      case 'violations':
        return (
          <ResponsiveContainer width="100%" height={320}>
            <BarChart data={mockViolationData}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis 
                dataKey="time" 
                stroke="#6b7280"
                fontSize={12}
              />
              <YAxis 
                stroke="#6b7280"
                fontSize={12}
                label={{ value: 'Violations', angle: -90, position: 'insideLeft' }}
              />
              <Tooltip 
                contentStyle={{
                  backgroundColor: 'white',
                  border: '1px solid #e5e7eb',
                  borderRadius: '8px',
                  boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)'
                }}
              />
              <Bar dataKey="memory" fill="#3b82f6" name="Memory Violations" />
              <Bar dataKey="jank" fill="#ef4444" name="Jank Violations" />
            </BarChart>
          </ResponsiveContainer>
        )

      default:
        return null
    }
  }

  return (
    <div className="card">
      <div className="flex items-center justify-between mb-6">
        <h3 className="text-lg font-semibold text-gray-900">Performance Metrics</h3>
        <div className="flex items-center space-x-1 bg-gray-100 rounded-lg p-1">
          {tabs.map((tab) => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id as any)}
              className={`flex items-center space-x-2 px-3 py-2 rounded-md text-sm font-medium transition-colors duration-200 ${
                activeTab === tab.id
                  ? 'bg-white text-primary-600 shadow-sm'
                  : 'text-gray-600 hover:text-gray-900'
              }`}
            >
              <tab.icon className="w-4 h-4" />
              <span>{tab.label}</span>
            </button>
          ))}
        </div>
      </div>

      {renderChart()}

      {/* Chart Legend */}
      <div className="mt-4 flex items-center justify-center space-x-6 text-sm">
        {activeTab === 'memory' && (
          <>
            <div className="flex items-center space-x-2">
              <div className="w-3 h-3 bg-blue-500 rounded-full" />
              <span className="text-gray-600">Memory Usage</span>
            </div>
            <div className="flex items-center space-x-2">
              <div className="w-3 h-1 bg-yellow-500 rounded" style={{ borderStyle: 'dashed' }} />
              <span className="text-gray-600">Budget Limit</span>
            </div>
          </>
        )}
        
        {activeTab === 'jank' && (
          <>
            <div className="flex items-center space-x-2">
              <div className="w-3 h-3 bg-red-500 rounded-full" />
              <span className="text-gray-600">Jank Percentage</span>
            </div>
            <div className="flex items-center space-x-2">
              <div className="w-3 h-1 bg-yellow-500 rounded" style={{ borderStyle: 'dashed' }} />
              <span className="text-gray-600">Budget Limit</span>
            </div>
          </>
        )}
        
        {activeTab === 'violations' && (
          <>
            <div className="flex items-center space-x-2">
              <div className="w-3 h-3 bg-blue-500 rounded" />
              <span className="text-gray-600">Memory Violations</span>
            </div>
            <div className="flex items-center space-x-2">
              <div className="w-3 h-3 bg-red-500 rounded" />
              <span className="text-gray-600">Jank Violations</span>
            </div>
          </>
        )}
      </div>
    </div>
  )
}