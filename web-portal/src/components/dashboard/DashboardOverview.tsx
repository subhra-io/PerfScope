'use client'

import { 
  Smartphone, 
  AlertTriangle, 
  Users, 
  TrendingUp,
  TrendingDown,
  Activity
} from 'lucide-react'

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

interface Props {
  data?: DashboardData
  isLoading: boolean
  timeRange: string
}

export function DashboardOverview({ data, isLoading, timeRange }: Props) {
  if (isLoading) {
    return (
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {[...Array(4)].map((_, i) => (
          <div key={i} className="card">
            <div className="loading-shimmer h-4 w-20 mb-2" />
            <div className="loading-shimmer h-8 w-16 mb-2" />
            <div className="loading-shimmer h-3 w-24" />
          </div>
        ))}
      </div>
    )
  }

  const metrics = [
    {
      title: 'Total Sessions',
      value: data?.summary.sessions || 0,
      change: '+12%',
      trend: 'up',
      icon: Users,
      color: 'primary'
    },
    {
      title: 'Active Violations',
      value: data?.summary.violations || 0,
      change: '-8%',
      trend: 'down',
      icon: AlertTriangle,
      color: 'danger'
    },
    {
      title: 'Screen Changes',
      value: data?.summary.screen_changes || 0,
      change: '+5%',
      trend: 'up',
      icon: Activity,
      color: 'success'
    },
    {
      title: 'Health Snapshots',
      value: data?.summary.health_snapshots || 0,
      change: '+15%',
      trend: 'up',
      icon: TrendingUp,
      color: 'warning'
    }
  ]

  return (
    <div className="space-y-6">
      {/* Key Metrics */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {metrics.map((metric) => (
          <div key={metric.title} className="metric-card">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">{metric.title}</p>
                <p className="text-2xl font-bold text-gray-900 mt-1">
                  {metric.value.toLocaleString()}
                </p>
                <div className="flex items-center mt-2">
                  {metric.trend === 'up' ? (
                    <TrendingUp className="w-4 h-4 text-success-500 mr-1" />
                  ) : (
                    <TrendingDown className="w-4 h-4 text-danger-500 mr-1" />
                  )}
                  <span className={`text-sm font-medium ${
                    metric.trend === 'up' ? 'text-success-600' : 'text-danger-600'
                  }`}>
                    {metric.change}
                  </span>
                  <span className="text-sm text-gray-500 ml-1">vs last {timeRange}</span>
                </div>
              </div>
              <div className={`p-3 rounded-lg bg-${metric.color}-100`}>
                <metric.icon className={`w-6 h-6 text-${metric.color}-600`} />
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* App Performance Summary */}
      <div className="card">
        <div className="flex items-center justify-between mb-6">
          <h3 className="text-lg font-semibold text-gray-900">Application Performance</h3>
          <span className="text-sm text-gray-500">Last {timeRange}</span>
        </div>
        
        {data?.apps && data.apps.length > 0 ? (
          <div className="space-y-4">
            {data.apps.map((app) => (
              <div key={app.id} className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
                <div className="flex items-center space-x-3">
                  <div className="w-10 h-10 bg-primary-100 rounded-lg flex items-center justify-center">
                    <Smartphone className="w-5 h-5 text-primary-600" />
                  </div>
                  <div>
                    <h4 className="font-medium text-gray-900">{app.name}</h4>
                    <p className="text-sm text-gray-500">{app.package_id}</p>
                  </div>
                </div>
                <div className="flex items-center space-x-6 text-sm">
                  <div className="text-center">
                    <p className="font-semibold text-gray-900">{app.sessions}</p>
                    <p className="text-gray-500">Sessions</p>
                  </div>
                  <div className="text-center">
                    <p className={`font-semibold ${
                      app.violations > 0 ? 'text-danger-600' : 'text-success-600'
                    }`}>
                      {app.violations}
                    </p>
                    <p className="text-gray-500">Violations</p>
                  </div>
                  <div className={`px-3 py-1 rounded-full text-xs font-medium ${
                    app.violations === 0 
                      ? 'bg-success-100 text-success-700'
                      : app.violations < 5
                      ? 'bg-warning-100 text-warning-700'
                      : 'bg-danger-100 text-danger-700'
                  }`}>
                    {app.violations === 0 ? 'Healthy' : 
                     app.violations < 5 ? 'Warning' : 'Critical'}
                  </div>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="text-center py-8">
            <Smartphone className="w-12 h-12 text-gray-300 mx-auto mb-4" />
            <p className="text-gray-500">No applications found</p>
            <p className="text-sm text-gray-400 mt-1">
              Start sending events from your mobile app to see data here
            </p>
          </div>
        )}
      </div>
    </div>
  )
}