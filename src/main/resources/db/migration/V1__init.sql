-- Users
CREATE TABLE users (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(255) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL
);
CREATE UNIQUE INDEX uk_users_email ON users (email);

-- Monitored APIs
CREATE TABLE monitored_apis (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  name VARCHAR(100) NOT NULL,
  spec_url VARCHAR(2048) NOT NULL,
  check_interval_seconds INT NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL,
  last_checked_at TIMESTAMPTZ,
  last_changed_at TIMESTAMPTZ
);
CREATE UNIQUE INDEX uk_user_api_name ON monitored_apis (user_id, name);
CREATE INDEX idx_monitored_user ON monitored_apis (user_id);
CREATE INDEX idx_monitored_active ON monitored_apis (active);

-- Spec Snapshots
CREATE TABLE spec_snapshots (
  id BIGSERIAL PRIMARY KEY,
  monitored_api_id BIGINT NOT NULL REFERENCES monitored_apis(id) ON DELETE CASCADE,
  version INT NOT NULL,
  content TEXT NOT NULL,
  content_hash VARCHAR(64) NOT NULL,
  fetched_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_snapshot_api_fetched ON spec_snapshots (monitored_api_id, fetched_at DESC);
CREATE INDEX idx_snapshot_api_version ON spec_snapshots (monitored_api_id, version);

-- Change Reports
CREATE TABLE change_reports (
  id BIGSERIAL PRIMARY KEY,
  monitored_api_id BIGINT NOT NULL REFERENCES monitored_apis(id) ON DELETE CASCADE,
  from_snapshot_id BIGINT NOT NULL,
  to_snapshot_id BIGINT NOT NULL,
  from_version INT NOT NULL,
  to_version INT NOT NULL,
  breaking_count INT NOT NULL DEFAULT 0,
  non_breaking_count INT NOT NULL DEFAULT 0,
  deprecation_count INT NOT NULL DEFAULT 0,
  has_breaking_changes BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_report_api_created ON change_reports (monitored_api_id, created_at DESC);

-- Change Items
CREATE TABLE change_items (
  id BIGSERIAL PRIMARY KEY,
  change_report_id BIGINT NOT NULL REFERENCES change_reports(id) ON DELETE CASCADE,
  type VARCHAR(40) NOT NULL,
  severity VARCHAR(20) NOT NULL,
  path VARCHAR(500),
  description VARCHAR(500) NOT NULL
);
CREATE INDEX idx_item_report ON change_items (change_report_id);

-- Alert Channels
CREATE TABLE alert_channels (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  type VARCHAR(30) NOT NULL,
  target VARCHAR(2048) NOT NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_alert_channel_user ON alert_channels (user_id);

-- Alert Logs
CREATE TABLE alert_logs (
  id BIGSERIAL PRIMARY KEY,
  change_report_id BIGINT NOT NULL REFERENCES change_reports(id) ON DELETE CASCADE,
  channel_type VARCHAR(30) NOT NULL,
  target VARCHAR(2048) NOT NULL,
  status VARCHAR(20) NOT NULL,
  error_message VARCHAR(1000),
  sent_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_alertlog_report ON alert_logs (change_report_id);
CREATE INDEX idx_alertlog_status ON alert_logs (status);

