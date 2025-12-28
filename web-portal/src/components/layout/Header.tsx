'use client'

import { useState } from 'react'
import { Bell, Search, RefreshCw } from 'lucide-react'
import { useRealtimeEvents } from '@/hooks/useRealtimeEvents'

export function Header() {
  const [searchQuery, setSearchQuery] = useState('')
  const { events, isConnected } = useRealtimeEvents()
  
  // Count unread violations (last 5 minutes)
  const recentViolations = events.filter(event => 
    event.event_type.includes('VIOLATION') && 
    Date.now() - event.timestamp < 5 * 60 * 1000
  ).length

  return (
    <header className="bg-white shadow-sm border-b border-gray-200 px-6 py-4">
      <div className="flex items-center justify-between">
        {/* Search */}
        <div className="flex-1 max-w-md">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400" />
            <input
              type="text"
              placeholder="Search sessions, violations, or devices..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
            />
          </div>
        </div>

        {/* Actions */}
        <div className="flex items-center space-x-4">
          {/* Refresh Button */}
          <button className="p-2 text-gray-400 hover:text-gray-600 rounded-md hover:bg-gray-100 transition-colors duration-200">
            <RefreshCw className="w-5 h-5" />
          </button>

          {/* Notifications */}
          <div className="relative">
            <button className="p-2 text-gray-400 hover:text-gray-600 rounded-md hover:bg-gray-100 transition-colors duration-200">
              <Bell className="w-5 h-5" />
              {recentViolations > 0 && (
                <span className="absolute -top-1 -right-1 w-5 h-5 bg-danger-500 text-white text-xs rounded-full flex items-center justify-center animate-bounce-slow">
                  {recentViolations > 9 ? '9+' : recentViolations}
                </span>
              )}
            </button>
          </div>

          {/* Connection Status */}
          <div className={`flex items-center space-x-2 px-3 py-1 rounded-full text-sm ${
            isConnected 
              ? 'bg-success-100 text-success-700' 
              : 'bg-danger-100 text-danger-700'
          }`}>
            <div className={`w-2 h-2 rounded-full ${
              isConnected 
                ? 'bg-success-500 animate-pulse' 
                : 'bg-danger-500'
            }`} />
            <span className="font-medium">
              {isConnected ? 'Connected' : 'Offline'}
            </span>
          </div>

          {/* User Menu */}
          <div className="flex items-center space-x-3">
            <div className="w-8 h-8 bg-primary-600 rounded-full flex items-center justify-center">
              <span className="text-white text-sm font-medium">U</span>
            </div>
          </div>
        </div>
      </div>
    </header>
  )
}