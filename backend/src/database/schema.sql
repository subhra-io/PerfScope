-- PerfScope Backend Database Schema
-- Designed for high-throughput event ingestion and fast analytics queries

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Apps table - Each mobile app using PerfScope SDK
CREATE TABLE apps (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    package_id VARCHAR(255) UNIQUE NOT NULL, -- e.g., com.example.app
    api_key VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    -- App metadata
    description TEXT,
    team_id VARCHAR(255),
    is_active BOOLEAN DEFAULT true,
    
    -- Indexes
    INDEX idx_apps_package_id (package_id),
    INDEX idx_apps_api_key (api_key),
    INDEX idx_apps_team_id (team_id)
);

-- Sessions table - User sessions within apps
CREATE TABLE sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    app_id UUID NOT NULL REFERENCES apps(id) ON DELETE CASCADE,
    session_id VARCHAR(255) NOT NULL, -- From SDK
    
    -- Session timing
    started_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ended_at TIMESTAMP WITH TIME ZONE,
    duration_ms BIGINT,
    
    -- Device information (JSONB for flexibility)
    device JSONB NOT NULL,
    build JSONB NOT NULL,
    
    -- Session summary metrics
    total_violations INTEGER DEFAULT 0,
    max_memory_mb INTEGER DEFAULT 0,
    avg_jank_percent DECIMAL(5,2) DEFAULT 0,
    screen_count INTEGER DEFAULT 0,
    
    -- SDK metadata
    sdk_version VARCHAR(50),
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    -- Indexes for fast queries
    INDEX idx_sessions_app_id (app_id),
    INDEX idx_sessions_session_id (session_id),
    INDEX idx_sessions_started_at (started_at),
    INDEX idx_sessions_device_model ((device->>'model')),
    INDEX idx_sessions_android_version ((device->>'android_version')),
    INDEX idx_sessions_build_version ((build->>'version_name')),
    
    -- Composite indexes for analytics
    INDEX idx_sessions_app_started (app_id, started_at),
    INDEX idx_sessions_violations (app_id, total_violations DESC),
    
    -- Unique constraint
    UNIQUE(app_id, session_id)
);

-- Events table - All performance events (violations, health snapshots, etc.)
CREATE TABLE events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_id UUID NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
    
    -- Event identification
    event_type VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    screen VARCHAR(255) NOT NULL,
    
    -- Event payload (JSONB for flexibility and fast queries)
    payload JSONB NOT NULL,
    
    -- Extracted fields for fast filtering (denormalized for performance)
    violation_type VARCHAR(50), -- For violation events
    severity VARCHAR(20),       -- For violation events
    actual_value DECIMAL(10,2), -- For violation events
    budget_value DECIMAL(10,2), -- For violation events
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    -- Indexes for fast queries
    INDEX idx_events_session_id (session_id),
    INDEX idx_events_timestamp (timestamp),
    INDEX idx_events_type (event_type),
    INDEX idx_events_screen (screen),
    INDEX idx_events_violation_type (violation_type),
    INDEX idx_events_severity (severity),
    
    -- Composite indexes for analytics
    INDEX idx_events_session_timestamp (session_id, timestamp),
    INDEX idx_events_type_timestamp (event_type, timestamp),
    INDEX idx_events_violations (session_id, event_type, severity) WHERE event_type LIKE '%_VIOLATION',
    
    -- JSONB indexes for payload queries
    INDEX idx_events_payload_gin (payload) USING GIN
);

-- Screen changes table - Optimized for navigation flow analysis
CREATE TABLE screen_changes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_id UUID NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
    
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    from_screen VARCHAR(255),
    to_screen VARCHAR(255) NOT NULL,
    duration_ms BIGINT, -- Time spent on previous screen
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    -- Indexes
    INDEX idx_screen_changes_session (session_id),
    INDEX idx_screen_changes_timestamp (timestamp),
    INDEX idx_screen_changes_to_screen (to_screen),
    INDEX idx_screen_changes_flow (session_id, timestamp)
);

-- Violations summary table - Pre-aggregated for fast dashboard queries
CREATE TABLE violation_summaries (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    app_id UUID NOT NULL REFERENCES apps(id) ON DELETE CASCADE,
    
    -- Time bucket (hourly aggregation)
    time_bucket TIMESTAMP WITH TIME ZONE NOT NULL,
    
    -- Dimensions
    violation_type VARCHAR(50) NOT NULL,
    screen VARCHAR(255),
    device_model VARCHAR(255),
    build_version VARCHAR(255),
    
    -- Metrics
    violation_count INTEGER NOT NULL DEFAULT 0,
    unique_sessions INTEGER NOT NULL DEFAULT 0,
    avg_actual_value DECIMAL(10,2),
    avg_budget_value DECIMAL(10,2),
    severity_distribution JSONB, -- {"LOW": 5, "MEDIUM": 3, "HIGH": 2, "CRITICAL": 1}
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    -- Indexes
    INDEX idx_violation_summaries_app_time (app_id, time_bucket),
    INDEX idx_violation_summaries_type (violation_type),
    INDEX idx_violation_summaries_screen (screen),
    
    -- Unique constraint for upserts
    UNIQUE(app_id, time_bucket, violation_type, screen, device_model, build_version)
);

-- Health metrics table - Optimized for time-series queries
CREATE TABLE health_metrics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_id UUID NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
    
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    screen VARCHAR(255) NOT NULL,
    
    -- Performance metrics
    memory_mb INTEGER NOT NULL,
    jank_percent DECIMAL(5,2) NOT NULL,
    avg_frame_ms DECIMAL(6,2) NOT NULL,
    cpu_percent INTEGER NOT NULL,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    -- Indexes for time-series queries
    INDEX idx_health_metrics_session_time (session_id, timestamp),
    INDEX idx_health_metrics_timestamp (timestamp),
    INDEX idx_health_metrics_screen (screen)
);

-- Create updated_at trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply updated_at triggers
CREATE TRIGGER update_apps_updated_at BEFORE UPDATE ON apps
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_violation_summaries_updated_at BEFORE UPDATE ON violation_summaries
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Create views for common queries

-- Active sessions view
CREATE VIEW active_sessions AS
SELECT 
    s.*,
    a.name as app_name,
    a.package_id,
    EXTRACT(EPOCH FROM (NOW() - s.started_at)) as session_age_seconds
FROM sessions s
JOIN apps a ON s.app_id = a.id
WHERE s.ended_at IS NULL;

-- Recent violations view (last 24 hours)
CREATE VIEW recent_violations AS
SELECT 
    e.*,
    s.app_id,
    a.name as app_name,
    s.device,
    s.build
FROM events e
JOIN sessions s ON e.session_id = s.id
JOIN apps a ON s.app_id = a.id
WHERE e.event_type LIKE '%_VIOLATION'
    AND e.timestamp > NOW() - INTERVAL '24 hours'
ORDER BY e.timestamp DESC;

-- Performance summary by app (last 7 days)
CREATE VIEW app_performance_summary AS
SELECT 
    a.id as app_id,
    a.name as app_name,
    a.package_id,
    COUNT(DISTINCT s.id) as total_sessions,
    COUNT(DISTINCT CASE WHEN s.ended_at IS NULL THEN s.id END) as active_sessions,
    COALESCE(SUM(s.total_violations), 0) as total_violations,
    COALESCE(AVG(s.avg_jank_percent), 0) as avg_jank_percent,
    COALESCE(AVG(s.max_memory_mb), 0) as avg_max_memory_mb,
    COUNT(DISTINCT s.device->>'model') as unique_devices
FROM apps a
LEFT JOIN sessions s ON a.id = s.app_id 
    AND s.started_at > NOW() - INTERVAL '7 days'
WHERE a.is_active = true
GROUP BY a.id, a.name, a.package_id;

-- Add comments for documentation
COMMENT ON TABLE apps IS 'Mobile applications using PerfScope SDK';
COMMENT ON TABLE sessions IS 'User sessions within mobile apps';
COMMENT ON TABLE events IS 'All performance events (violations, health snapshots, navigation)';
COMMENT ON TABLE screen_changes IS 'Screen navigation events for flow analysis';
COMMENT ON TABLE violation_summaries IS 'Pre-aggregated violation data for fast dashboard queries';
COMMENT ON TABLE health_metrics IS 'Time-series performance health data';

COMMENT ON COLUMN sessions.device IS 'Device information: model, manufacturer, android_version, ram_mb, screen_density, screen_resolution';
COMMENT ON COLUMN sessions.build IS 'Build information: version_name, version_code, build_type, flavor';
COMMENT ON COLUMN events.payload IS 'Full event data as JSON for flexibility and detailed analysis';