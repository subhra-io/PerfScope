import { z } from 'zod';

// Device information schema
export const DeviceInfoSchema = z.object({
  model: z.string(),
  manufacturer: z.string(),
  android_version: z.number().int().min(21).max(50),
  ram_mb: z.number().int().min(0),
  screen_density: z.string(),
  screen_resolution: z.string()
});

// Build information schema
export const BuildInfoSchema = z.object({
  version_name: z.string(),
  version_code: z.number().int().min(0),
  build_type: z.enum(['debug', 'release', 'unknown']),
  flavor: z.string().optional()
});

// Memory attribution schema
export const MemoryAttributionSchema = z.object({
  likely_cause: z.enum([
    'BITMAP_HEAVY',
    'COLLECTION_HEAVY', 
    'OBJECT_HEAVY',
    'NATIVE_HEAVY',
    'UNKNOWN',
    'NORMAL'
  ]),
  delta_mb: z.number().int(),
  details: z.string()
});

// Jank attribution schema
export const JankAttributionSchema = z.object({
  jank_percent: z.number().min(0).max(100),
  avg_frame_ms: z.number().min(0),
  frame_count: z.number().int().min(0),
  jank_frame_count: z.number().int().min(0),
  details: z.string()
});

// Base event schema
const BaseEventSchema = z.object({
  timestamp: z.number().int().positive(),
  app_id: z.string().min(1),
  session_id: z.string().uuid(),
  screen: z.string().min(1),
  device: DeviceInfoSchema,
  build: BuildInfoSchema
});

// Session start event
export const SessionStartEventSchema = BaseEventSchema.extend({
  event_type: z.literal('SESSION_START'),
  sdk_version: z.string()
});

// Session end event
export const SessionEndEventSchema = BaseEventSchema.extend({
  event_type: z.literal('SESSION_END'),
  duration_ms: z.number().int().min(0),
  total_violations: z.number().int().min(0)
});

// Screen change event
export const ScreenChangeEventSchema = BaseEventSchema.extend({
  event_type: z.literal('SCREEN_CHANGE'),
  previous_screen: z.string(),
  time_on_previous_screen_ms: z.number().int().min(0)
});

// Memory violation event
export const MemoryViolationEventSchema = BaseEventSchema.extend({
  event_type: z.literal('MEMORY_VIOLATION'),
  violation_type: z.enum([
    'HEAP_MEMORY',
    'SCREEN_MEMORY_DELTA',
    'BITMAP_SPIKE',
    'COLLECTION_SPIKE',
    'OBJECT_SPIKE',
    'NATIVE_SPIKE'
  ]),
  actual_mb: z.number().int().min(0),
  budget_mb: z.number().int().min(0),
  severity: z.enum(['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']),
  attribution: MemoryAttributionSchema
});

// Jank violation event
export const JankViolationEventSchema = BaseEventSchema.extend({
  event_type: z.literal('JANK_VIOLATION'),
  violation_type: z.enum([
    'JANK_PERCENT',
    'FRAME_TIME', 
    'SEVERE_JANK'
  ]),
  actual_value: z.number().min(0),
  budget_value: z.number().min(0),
  severity: z.enum(['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']),
  jank_type: z.enum([
    'MAIN_THREAD_BLOCKING',
    'LAYOUT_THRASH',
    'COMPOSE_RECOMPOSITION',
    'OVERDRAW',
    'MEMORY_PRESSURE',
    'GPU_BOTTLENECK',
    'UNKNOWN',
    'SMOOTH'
  ]),
  attribution: JankAttributionSchema
});

// Health snapshot event
export const HealthSnapshotEventSchema = BaseEventSchema.extend({
  event_type: z.literal('HEALTH_SNAPSHOT'),
  memory_mb: z.number().int().min(0),
  jank_percent: z.number().min(0).max(100),
  avg_frame_ms: z.number().min(0),
  cpu_percent: z.number().int().min(0).max(100)
});

// Union of all event types
export const PerfEventSchema = z.discriminatedUnion('event_type', [
  SessionStartEventSchema,
  SessionEndEventSchema,
  ScreenChangeEventSchema,
  MemoryViolationEventSchema,
  JankViolationEventSchema,
  HealthSnapshotEventSchema
]);

// Batch request schema
export const EventBatchSchema = z.object({
  api_key: z.string().min(1),
  events: z.array(PerfEventSchema).min(1).max(100) // Limit batch size
});

// Type exports
export type DeviceInfo = z.infer<typeof DeviceInfoSchema>;
export type BuildInfo = z.infer<typeof BuildInfoSchema>;
export type MemoryAttribution = z.infer<typeof MemoryAttributionSchema>;
export type JankAttribution = z.infer<typeof JankAttributionSchema>;
export type PerfEvent = z.infer<typeof PerfEventSchema>;
export type EventBatch = z.infer<typeof EventBatchSchema>;
export type SessionStartEvent = z.infer<typeof SessionStartEventSchema>;
export type SessionEndEvent = z.infer<typeof SessionEndEventSchema>;
export type ScreenChangeEvent = z.infer<typeof ScreenChangeEventSchema>;
export type MemoryViolationEvent = z.infer<typeof MemoryViolationEventSchema>;
export type JankViolationEvent = z.infer<typeof JankViolationEventSchema>;
export type HealthSnapshotEvent = z.infer<typeof HealthSnapshotEventSchema>;