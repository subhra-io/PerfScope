'use client'

import { useState } from 'react'
import { 
  Smartphone, 
  Clock, 
  AlertTriangle, 
  CheckCircle,
  ExternalLink,
  Filter
} from 'lucide-react'
import { formatDistanceToNow, format } from 'date-fns'
// import { useSessions } from '@/hooks/useSessions'

interface Props {
  timeRange: string
}

export function SessionsList({ timeRange }: Props) {
  // const { data: sessions, isLoading } = useSessions(timeRange)
  const [filter, setFilter] = useState<'all' | 'violations' | 'healthy'>('all')
  const isLoading = false // Temporary for demo

  // Mock sessions data for demonstration
  const mockSessions = [
    {
      id: 'session-1',
      app_id: 'demo-app',
      app_name: 'PerfScope Demo',
      session_id: 'android-demo-1766860641',
      started_at: Date.now() - 3600000, // 1 hour ago
      ended_at: Date.now() - 3555000,
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
      started_at: Date.now() - 7200000, // 2 hours ago
      ended_at: Date.now() - 7140000,
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
      started_at: Date.now() - 10800000, // 3 hours ago
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

  const filteredSessions = mockSessions.filter(session => {
    if (filter === 'violations') return session.violations > 0
    if (filter === 'healthy') return session.violations === 0
    return true
  })

  const getStatusColor = (status: string, violations: number) => {
    if (status === 'active') return 'primary'
    if (violations > 0) return 'danger'
    return 'success'
  }

  const getStatusText = (status: string, violations: number) => {
    if (status === 'active') return 'Active'
    if (violations > 0) return 'Issues'
    return 'Healthy'
  }

  if (isLoading) {
    return (
      <div className="card">
        <div className="flex items-center justify-between mb-6">
          <div className="loading-shimmer h-6 w-32" />
          <div className="loading-shimmer h-8 w-24" />
        </div>
        <div className="space-y-4">
          {[...Array(3)].map((_, i) => (
            <div key={i} className="loading-shimmer h-20 w-full rounded-lg" />
          ))}
        </div>
      </div>
    )
  }

  return (
    <div className="card">
      <div className="flex items-center justify-between mb-6">
        <h3 className="text-lg font-semibold text-gray-900">Recent Sessions</h3>
        
        <div className="flex items-center space-x-3">
          {/* Filter */}
          <div className="flex items-center space-x-2">
            <Filter className="w-4 h-4 text-gray-400" />
            <select
              value={filter}
              onChange={(e) => setFilter(e.target.value as any)}
              className="text-sm border border-gray-300 rounded-md px-3 py-1 focus:outline-none focus:ring-2 focus:ring-primary-500"
            >
              <option value="all">All Sessions</option>
              <option value="violations">With Violations</option>
              <option value="healthy">Healthy</option>
            </select>
          </div>
        </div>
      </div>

      <div className="space-y-4">
        {filteredSessions.length > 0 ? (
          filteredSessions.map((session) => {
            const statusColor = getStatusColor(session.status, session.violations)
            const statusText = getStatusText(session.status, session.violations)
            
            return (
              <div
                key={session.id}
                className="p-4 border border-gray-200 rounded-lg hover:border-gray-300 hover:shadow-sm transition-all duration-200"
              >
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-4">
                    {/* App Icon */}
                    <div className="w-12 h-12 bg-primary-100 rounded-lg flex items-center justify-center">
                      <Smartphone className="w-6 h-6 text-primary-600" />
                    </div>
                    
                    {/* Session Info */}
                    <div>
                      <div className="flex items-center space-x-2 mb-1">
                        <h4 className="font-medium text-gray-900">{session.app_name}</h4>
                        <span className={`px-2 py-1 text-xs font-medium rounded-full bg-${statusColor}-100 text-${statusColor}-700`}>
                          {statusText}
                        </span>
                      </div>
                      
                      <div className="flex items-center space-x-4 text-sm text-gray-500">
                        <div className="flex items-center space-x-1">
                          <Clock className="w-3 h-3" />
                          <span>
                            {session.ended_at 
                              ? `${Math.round(session.duration_ms! / 1000)}s duration`
                              : 'Active session'
                            }
                          </span>
                        </div>
                        <span>•</span>
                        <span>{session.device.manufacturer} {session.device.model}</span>
                        <span>•</span>
                        <span>v{session.build.version_name}</span>
                      </div>
                      
                      <div className="flex items-center space-x-2 mt-2">
                        <span className="text-xs text-gray-400">Screens:</span>
                        <div className="flex items-center space-x-1">
                          {session.screens.slice(0, 3).map((screen, index) => (
                            <span key={screen} className="text-xs bg-gray-100 text-gray-600 px-2 py-1 rounded">
                              {screen}
                            </span>
                          ))}
                          {session.screens.length > 3 && (
                            <span className="text-xs text-gray-400">
                              +{session.screens.length - 3} more
                            </span>
                          )}
                        </div>
                      </div>
                    </div>
                  </div>
                  
                  {/* Session Stats */}
                  <div className="flex items-center space-x-6">
                    <div className="text-center">
                      <div className={`flex items-center space-x-1 ${
                        session.violations > 0 ? 'text-danger-600' : 'text-success-600'
                      }`}>
                        {session.violations > 0 ? (
                          <AlertTriangle className="w-4 h-4" />
                        ) : (
                          <CheckCircle className="w-4 h-4" />
                        )}
                        <span className="font-semibold">{session.violations}</span>
                      </div>
                      <p className="text-xs text-gray-500 mt-1">Violations</p>
                    </div>
                    
                    <div className="text-center">
                      <p className="font-semibold text-gray-900">{session.screens.length}</p>
                      <p className="text-xs text-gray-500 mt-1">Screens</p>
                    </div>
                    
                    <div className="text-center">
                      <p className="text-xs text-gray-500">
                        {formatDistanceToNow(session.started_at)} ago
                      </p>
                      <p className="text-xs text-gray-400 mt-1">
                        {format(session.started_at, 'HH:mm')}
                      </p>
                    </div>
                    
                    <button className="p-2 text-gray-400 hover:text-primary-600 rounded-md hover:bg-gray-50 transition-colors duration-200">
                      <ExternalLink className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              </div>
            )
          })
        ) : (
          <div className="text-center py-8">
            <Smartphone className="w-12 h-12 text-gray-300 mx-auto mb-4" />
            <p className="text-gray-500">No sessions found</p>
            <p className="text-sm text-gray-400 mt-1">
              {filter === 'violations' 
                ? 'No sessions with violations in this time range'
                : filter === 'healthy'
                ? 'No healthy sessions in this time range'
                : 'No sessions in this time range'
              }
            </p>
          </div>
        )}
      </div>
      
      {filteredSessions.length > 0 && (
        <div className="mt-6 pt-4 border-t border-gray-200 text-center">
          <button className="text-sm text-primary-600 hover:text-primary-700 font-medium">
            View all sessions →
          </button>
        </div>
      )}
    </div>
  )
}