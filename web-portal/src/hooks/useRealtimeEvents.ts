'use client'

import { useState, useEffect, useRef } from 'react'
import { io, Socket } from 'socket.io-client'

interface PerfEvent {
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
    android_version: number
    ram_mb: number
  }
  build?: {
    version_name: string
    version_code: number
    build_type: string
  }
  attribution?: {
    likely_cause?: string
    details?: string
    jank_percent?: number
    avg_frame_ms?: number
    frame_count?: number
    jank_frame_count?: number
  }
}

export function useRealtimeEvents() {
  const [events, setEvents] = useState<PerfEvent[]>([])
  const [isConnected, setIsConnected] = useState(false)
  const socketRef = useRef<Socket | null>(null)

  useEffect(() => {
    // For now, we'll simulate real-time events since we don't have WebSocket in our test server
    // In production, this would connect to a WebSocket endpoint
    
    const simulateRealtimeEvents = () => {
      // Fetch events from our REST API periodically
      const fetchEvents = async () => {
        try {
          const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/events/received`)
          if (response.ok) {
            const data = await response.json()
            setEvents(data.events || [])
            setIsConnected(true)
          } else {
            setIsConnected(false)
          }
        } catch (error) {
          console.error('Failed to fetch events:', error)
          setIsConnected(false)
        }
      }

      // Initial fetch
      fetchEvents()

      // Poll every 5 seconds for new events
      const interval = setInterval(fetchEvents, 5000)

      return () => clearInterval(interval)
    }

    const cleanup = simulateRealtimeEvents()

    return cleanup
  }, [])

  // Simulate WebSocket connection for demo purposes
  useEffect(() => {
    // This would be the real WebSocket implementation:
    /*
    const socket = io(process.env.NEXT_PUBLIC_WS_URL || 'ws://localhost:3001')
    socketRef.current = socket

    socket.on('connect', () => {
      setIsConnected(true)
      console.log('Connected to PerfScope WebSocket')
    })

    socket.on('disconnect', () => {
      setIsConnected(false)
      console.log('Disconnected from PerfScope WebSocket')
    })

    socket.on('violation', (event: PerfEvent) => {
      setEvents(prev => [event, ...prev.slice(0, 99)]) // Keep last 100 events
    })

    socket.on('event', (event: PerfEvent) => {
      setEvents(prev => [event, ...prev.slice(0, 99)])
    })

    return () => {
      socket.disconnect()
    }
    */
  }, [])

  return {
    events,
    isConnected,
    disconnect: () => {
      if (socketRef.current) {
        socketRef.current.disconnect()
      }
    }
  }
}