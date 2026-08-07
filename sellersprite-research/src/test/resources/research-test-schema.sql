CREATE TABLE IF NOT EXISTS ai_conversation (
  conversation_id VARCHAR(36) PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS market_research_job (
  job_id VARCHAR(36) PRIMARY KEY,
  user_id VARCHAR(36) NOT NULL,
  report_name VARCHAR(128) NOT NULL,
  marketplace VARCHAR(16) NOT NULL,
  node_id_path VARCHAR(1024) NOT NULL,
  research_month VARCHAR(7) NOT NULL,
  keyword VARCHAR(256),
  seed_asins CLOB,
  collection_config CLOB NOT NULL,
  template_code VARCHAR(64) NOT NULL,
  data_source_mode VARCHAR(16) NOT NULL,
  workflow_version VARCHAR(64) NOT NULL,
  job_status VARCHAR(32) NOT NULL,
  current_node VARCHAR(64) NOT NULL,
  current_stage VARCHAR(32) DEFAULT 'SCREENING' NOT NULL,
  waiting_input_type VARCHAR(32),
  progress INT NOT NULL,
  attempt_count INT DEFAULT 0 NOT NULL,
  max_attempts INT DEFAULT 3 NOT NULL,
  next_run_at BIGINT NOT NULL,
  execution_owner VARCHAR(128),
  execution_token VARCHAR(36),
  lease_until BIGINT,
  heartbeat_at BIGINT,
  cancel_requested_at BIGINT,
  error_code VARCHAR(64) NOT NULL,
  error_message VARCHAR(512) NOT NULL,
  started_at BIGINT,
  finished_at BIGINT,
  created_at BIGINT DEFAULT 0 NOT NULL,
  updated_at BIGINT DEFAULT 0 NOT NULL,
  created_by VARCHAR(36) DEFAULT '' NOT NULL,
  updated_by VARCHAR(36) DEFAULT '' NOT NULL,
  deleted INT DEFAULT 0 NOT NULL,
  remark VARCHAR(512) DEFAULT '' NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_market_research_job_user_created
  ON market_research_job (user_id, created_at);

CREATE TABLE IF NOT EXISTS market_research_stage_input (
  input_id VARCHAR(36) PRIMARY KEY,
  job_id VARCHAR(36) NOT NULL,
  stage_code VARCHAR(32) NOT NULL,
  input_type VARCHAR(32) NOT NULL,
  decision VARCHAR(16) NOT NULL,
  input_payload CLOB NOT NULL,
  submitted_by VARCHAR(36) NOT NULL,
  submitted_at BIGINT NOT NULL,
  created_at BIGINT DEFAULT 0 NOT NULL,
  updated_at BIGINT DEFAULT 0 NOT NULL,
  created_by VARCHAR(36) DEFAULT '' NOT NULL,
  updated_by VARCHAR(36) DEFAULT '' NOT NULL,
  deleted INT DEFAULT 0 NOT NULL,
  remark VARCHAR(512) DEFAULT '' NOT NULL,
  CONSTRAINT uk_market_research_stage_input
    UNIQUE (job_id, stage_code, input_type, deleted),
  CONSTRAINT fk_market_research_stage_input_job FOREIGN KEY (job_id)
    REFERENCES market_research_job (job_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS market_research_node_execution (
  execution_id VARCHAR(36) PRIMARY KEY,
  job_id VARCHAR(36) NOT NULL,
  graph_code VARCHAR(32) NOT NULL,
  node_code VARCHAR(64) NOT NULL,
  node_name VARCHAR(128) NOT NULL,
  job_attempt INT NOT NULL,
  node_attempt INT NOT NULL,
  execution_status VARCHAR(32) NOT NULL,
  started_at BIGINT NOT NULL,
  finished_at BIGINT,
  duration_ms BIGINT,
  error_code VARCHAR(64) DEFAULT '' NOT NULL,
  error_message VARCHAR(512) DEFAULT '' NOT NULL,
  created_at BIGINT DEFAULT 0 NOT NULL,
  updated_at BIGINT DEFAULT 0 NOT NULL,
  created_by VARCHAR(36) DEFAULT '' NOT NULL,
  updated_by VARCHAR(36) DEFAULT '' NOT NULL,
  deleted INT DEFAULT 0 NOT NULL,
  remark VARCHAR(512) DEFAULT '' NOT NULL,
  CONSTRAINT uk_market_research_node_attempt
    UNIQUE (job_id, node_code, job_attempt, node_attempt, deleted)
);

CREATE TABLE IF NOT EXISTS market_research_dataset (
  dataset_id VARCHAR(36) PRIMARY KEY,
  job_id VARCHAR(36) NOT NULL,
  node_code VARCHAR(64) NOT NULL,
  operation VARCHAR(128) NOT NULL,
  dataset_code VARCHAR(128) NOT NULL,
  request_hash VARCHAR(64) NOT NULL,
  data_source_mode VARCHAR(16) NOT NULL,
  request_payload CLOB NOT NULL,
  source_payload CLOB NOT NULL,
  normalized_payload CLOB,
  record_count INT DEFAULT 0 NOT NULL,
  schema_version VARCHAR(32) NOT NULL,
  validation_status VARCHAR(32) NOT NULL,
  validation_summary VARCHAR(512) DEFAULT '' NOT NULL,
  sha256 VARCHAR(64) NOT NULL,
  fetched_at BIGINT NOT NULL,
  created_at BIGINT DEFAULT 0 NOT NULL,
  updated_at BIGINT DEFAULT 0 NOT NULL,
  created_by VARCHAR(36) DEFAULT '' NOT NULL,
  updated_by VARCHAR(36) DEFAULT '' NOT NULL,
  deleted INT DEFAULT 0 NOT NULL,
  remark VARCHAR(512) DEFAULT '' NOT NULL,
  CONSTRAINT uk_market_research_dataset_idempotency
    UNIQUE (job_id, node_code, operation, dataset_code, request_hash, deleted)
);

CREATE TABLE IF NOT EXISTS market_research_analysis_run (
  analysis_run_id VARCHAR(36) PRIMARY KEY,
  job_id VARCHAR(36) NOT NULL,
  user_id VARCHAR(36) NOT NULL,
  conversation_id VARCHAR(36),
  parent_run_id VARCHAR(36),
  run_type VARCHAR(32) NOT NULL,
  analysis_goal CLOB NOT NULL,
  run_status VARCHAR(32) DEFAULT 'WAITING_RESEARCH' NOT NULL,
  current_phase VARCHAR(64) DEFAULT 'waiting_research' NOT NULL,
  progress INT DEFAULT 0 NOT NULL,
  attempt_count INT DEFAULT 0 NOT NULL,
  max_attempts INT DEFAULT 3 NOT NULL,
  next_run_at BIGINT NOT NULL,
  execution_owner VARCHAR(128),
  execution_token VARCHAR(36),
  lease_until BIGINT,
  heartbeat_at BIGINT,
  cancel_requested_at BIGINT,
  model_call_count INT DEFAULT 0 NOT NULL,
  event_count INT DEFAULT 0 NOT NULL,
  final_summary CLOB,
  error_code VARCHAR(64) DEFAULT '' NOT NULL,
  error_message VARCHAR(512) DEFAULT '' NOT NULL,
  started_at BIGINT,
  finished_at BIGINT,
  created_at BIGINT DEFAULT 0 NOT NULL,
  updated_at BIGINT DEFAULT 0 NOT NULL,
  created_by VARCHAR(36) DEFAULT '' NOT NULL,
  updated_by VARCHAR(36) DEFAULT '' NOT NULL,
  deleted INT DEFAULT 0 NOT NULL,
  remark VARCHAR(512) DEFAULT '' NOT NULL,
  CONSTRAINT fk_market_research_analysis_job FOREIGN KEY (job_id)
    REFERENCES market_research_job (job_id) ON DELETE CASCADE,
  CONSTRAINT fk_market_research_analysis_conversation FOREIGN KEY (conversation_id)
    REFERENCES ai_conversation (conversation_id) ON DELETE SET NULL,
  CONSTRAINT fk_market_research_analysis_parent FOREIGN KEY (parent_run_id)
    REFERENCES market_research_analysis_run (analysis_run_id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_market_research_analysis_user_job
  ON market_research_analysis_run (user_id, job_id, created_at);
CREATE INDEX IF NOT EXISTS idx_market_research_analysis_job_status
  ON market_research_analysis_run (job_id, run_status, created_at);
CREATE INDEX IF NOT EXISTS idx_market_research_analysis_dispatch
  ON market_research_analysis_run (run_status, next_run_at, lease_until);
CREATE INDEX IF NOT EXISTS idx_market_research_analysis_token_lease
  ON market_research_analysis_run (execution_token, lease_until);

CREATE TABLE IF NOT EXISTS market_research_artifact (
  artifact_id VARCHAR(36) PRIMARY KEY,
  job_id VARCHAR(36) NOT NULL,
  analysis_run_id VARCHAR(36),
  artifact_scope_id VARCHAR(36) NOT NULL,
  workflow_version VARCHAR(64) NOT NULL,
  artifact_type VARCHAR(32) NOT NULL,
  file_name VARCHAR(255) NOT NULL,
  storage_key VARCHAR(512) NOT NULL,
  media_type VARCHAR(128) NOT NULL,
  file_size BIGINT,
  sha256 VARCHAR(64),
  artifact_status VARCHAR(32) NOT NULL,
  published_at BIGINT,
  created_at BIGINT DEFAULT 0 NOT NULL,
  updated_at BIGINT DEFAULT 0 NOT NULL,
  created_by VARCHAR(36) DEFAULT '' NOT NULL,
  updated_by VARCHAR(36) DEFAULT '' NOT NULL,
  deleted INT DEFAULT 0 NOT NULL,
  remark VARCHAR(512) DEFAULT '' NOT NULL,
  CONSTRAINT uk_market_research_artifact_scope
    UNIQUE (job_id, artifact_type, artifact_scope_id, deleted),
  CONSTRAINT fk_market_research_artifact_job FOREIGN KEY (job_id)
    REFERENCES market_research_job (job_id) ON DELETE CASCADE,
  CONSTRAINT fk_market_research_artifact_analysis_run FOREIGN KEY (analysis_run_id)
    REFERENCES market_research_analysis_run (analysis_run_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_market_research_artifact_analysis_run
  ON market_research_artifact (analysis_run_id);
CREATE INDEX IF NOT EXISTS idx_market_research_artifact_job
  ON market_research_artifact (job_id);

CREATE TABLE IF NOT EXISTS market_research_event_stream_lock (
  job_id VARCHAR(36) PRIMARY KEY,
  CONSTRAINT fk_market_research_event_stream_lock_job FOREIGN KEY (job_id)
    REFERENCES market_research_job (job_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS market_research_event (
  event_id VARCHAR(36) PRIMARY KEY,
  sequence_no BIGINT AUTO_INCREMENT NOT NULL,
  job_id VARCHAR(36) NOT NULL,
  conversation_id VARCHAR(36),
  analysis_run_id VARCHAR(36),
  scope VARCHAR(32) NOT NULL,
  event_type VARCHAR(64) NOT NULL,
  phase VARCHAR(64),
  sheet_name VARCHAR(128),
  node_code VARCHAR(64),
  message CLOB NOT NULL,
  payload CLOB NOT NULL,
  terminal INT DEFAULT 0 NOT NULL,
  created_at BIGINT DEFAULT 0 NOT NULL,
  updated_at BIGINT DEFAULT 0 NOT NULL,
  created_by VARCHAR(36) DEFAULT '' NOT NULL,
  updated_by VARCHAR(36) DEFAULT '' NOT NULL,
  deleted INT DEFAULT 0 NOT NULL,
  remark VARCHAR(512) DEFAULT '' NOT NULL,
  CONSTRAINT uk_market_research_event_sequence UNIQUE (sequence_no),
  CONSTRAINT fk_market_research_event_job FOREIGN KEY (job_id)
    REFERENCES market_research_job (job_id) ON DELETE CASCADE,
  CONSTRAINT fk_market_research_event_conversation FOREIGN KEY (conversation_id)
    REFERENCES ai_conversation (conversation_id) ON DELETE SET NULL,
  CONSTRAINT fk_market_research_event_analysis_run FOREIGN KEY (analysis_run_id)
    REFERENCES market_research_analysis_run (analysis_run_id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_market_research_event_job_sequence
  ON market_research_event (job_id, sequence_no);
CREATE INDEX IF NOT EXISTS idx_market_research_event_run_sequence
  ON market_research_event (analysis_run_id, sequence_no);
CREATE INDEX IF NOT EXISTS idx_market_research_event_conversation_sequence
  ON market_research_event (conversation_id, sequence_no);
