'use client'

import { useState, useEffect } from 'react'
import { AlertTriangle, Smartphone, Clock, Zap } from 'lucide-react'
import { formatDistanceToNow } from 'date-fns'
import { motion, AnimatePresence } from 'framer-motion'

interface ViolationEvent {
  event_type: string
  timestamp: number
  app_id: string
  session_id: string
  screen: string
  violation_type?: string
  severity?: string
  actual_mb?: number
  budget_mb?: number
  actual_value?: number
  budget_value?: number
  jank_type?: string
  device?: {
    model: string
    manufacturer: string
  }
  attribution?: {
    likely_cause?: string
    details?: string
  }
}

interface Props {
  events: ViolationEvent[]
  isConnected: boolean
}

export function RealtimeViolations({ events, isConnected }: Props) {
  const [violations, setViolations] = useState<ViolationEvent[]>([])
  const [newViolationCount, setNewViolationCount] = useState(0)

  // Filter and sort violations
  useEffect(() => {
    const violationEvents = events
      .filter(event => event.event_type.includes('VIOLATION'))
      .sort((a, b) => b.timestamp - a.timestamp)
      .slice(0, 10) // Show last 10 violations

    setViolations(violationEvents)
    
    // Count new violations in last 30 seconds
    const thirtySecondsAgo = Date.now() - 30000
    const newCount = violationEvents.filter(v => v.timestamp > thirtySecondsAgo).length
    setNewViolationCount(newCount)
  }, [events])

  const getSeverityColor = (severity?: string) => {
    switch (severity?.toLowerCase()) {
      case 'critical': return 'danger'
      case 'warning': return 'warning'
      case 'info': return 'primary'
      default: return 'gray'
    }
  }

  const getViolationIcon = (eventType: string) => {
    if (eventType.includes('MEMORY')) return AlertTriangle
    if (eventType.includes('JANK')) return Zap
    return AlertTriangle
  }

  const formatViolationDetails = (violation: ViolationEvent) => {
    if (violation.event_type === 'MEMORY_VIOLATION') {
      return `${violation.actual_mb}MB / ${violation.budget_mb}MB budget`
    }
    if (violation.event_type === 'JANK_VIOLATION') {
      return `${violation.actual_value}% / ${violation.budget_value}% budget`
    }
    return 'Performance violation detected'
  }

  return (
    <div className="card">
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center space-x-3">
          <h3 className="text-lg font-semibold text-gray-900">Real-time Violations</h3>
          {newViolationCount > 0 && (
            <motion.div
              initial={{ scale: 0 }}
              animate={{ scale: 1 }}
              className="px-2 py-1 bg-danger-100 text-danger-700 text-xs font-medium rounded-full"
            >
              {newViolationCount} new
            </motion.div>
          )}
        </div>
        
        <div className="flex items-center space-x-2">
          <div className={`w-2 h-2 rounded-full ${
            isConnected ? 'bg-success-500 animate-pulse' : 'bg-gray-400'
          }`} />
          <span className="text-sm text-gray-500">
            {isConnected ? 'Live monitoring' : 'Disconnected'}
          </span>
        </div>
      </div>

      <div className="space-y-3 max-h-96 overflow-y-auto">
        <AnimatePresence>
          {violations.length > 0 ? (
            violations.map((violation, index) => {
              const Icon = getViolationIcon(violation.event_type)
              const severityColor = getSeverityColor(violation.severity)
              
              return (
                <motion.div
                  key={`${violation.session_id}-${violation.timestamp}`}
                  initial={{ opacity: 0, y: -20 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0, y: 20 }}
                  transition={{ duration: 0.3, delay: index * 0.05 }}
                  className={`p-4 rounded-lg border-l-4 border-${severityColor}-500 bg-${severityColor}-50 hover:bg-${severityColor}-100 transition-colors duration-200`}
                >
                  <div className="flex items-start justify-between">
                    <div className="flex items-start space-x-3">
                      <div className={`p-2 rounded-lg bg-${severityColor}-100`}>
                        <Icon className={`w-4 h-4 text-${severityColor}-600`} />
                      </div>
                      
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center space-x-2 mb-1">
                          <h4 className="font-medium text-gray-900 truncate">
                            {violation.violation_type?.replace('_', ' ') || 'Performance Violation'}
                          </h4>
                          <span className={`px-2 py-1 text-xs font-medium rounded-full bg-${severityColor}-100 text-${severityColor}-700`}>
                            {violation.severity || 'Unknown'}
                          </span>
                        </div>
                        
                        <p className="text-sm text-gray-600 mb-2">
                          {formatViolationDetails(violation)}
                        </p>
                        
                        <div className="flex items-center space-x-4 text-xs text-gray-500">
                          <div className="flex items-center space-x-1">
                            <Smartphone className="w-3 h-3" />
                            <span>{violation.screen}</span>
                          </div>
                          <div className="flex items-center space-x-1">
                            <Clock className="w-3 h-3" />
                            <span>{formatDistanceToNow(violation.timestamp)} ago</span>
                          </div>
                          {violation.device && (
                            <span>{violation.device.manufacturer} {violation.device.model}</span>
                          )}
                        </div>
                        
                        {violation.attribution?.details && (
                          <p className="text-xs text-gray-500 mt-2 italic">
                            {violation.attribution.details}
                          </p>
                        )}
                      </div>
                    </div>
                  </div>
                </motion.div>
              )
            })
          ) : (
            <div className="text-center py-8">
              <AlertTriangle className="w-12 h-12 text-gray-300 mx-auto mb-4" />
              <p className="text-gray-500">No violations detected</p>
              <p className="text-sm text-gray-400 mt-1">
                {isConnected 
                  ? 'Your applications are performing well!' 
                  : 'Connect to see real-time violations'
                }
              </p>
            </div>
          )}
        </AnimatePresence>
      </div>
      
      {violations.length > 10 && (
        <div className="mt-4 pt-4 border-t border-gray-200">
          <button className="text-sm text-primary-600 hover:text-primary-700 font-medium">
            View all violations →
          </button>
        </div>
      )}
    </div>
  )
}