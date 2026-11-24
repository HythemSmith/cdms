-- ===== Tenant =====
CREATE TABLE tenant (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    config_json JSONB NOT NULL DEFAULT '{}'::jsonb,   -- messaging Vietful + Shopify creds
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ===== Worker =====
CREATE TABLE worker (
    id            BIGSERIAL PRIMARY KEY,
    tenant_id     BIGINT NOT NULL REFERENCES tenant(id) ON DELETE CASCADE,
    source        VARCHAR(20) NOT NULL CHECK (source IN ('VIETFUL','SHOPIFY')),
    name          VARCHAR(100) NOT NULL,
    schedule_cron VARCHAR(50),
    config_json   JSONB NOT NULL DEFAULT '{}'::jsonb,
    status        VARCHAR(20) NOT NULL DEFAULT 'STOPPED'
                  CHECK (status IN ('RUNNING','STOPPED','ERROR')),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (tenant_id, name)
);

-- ===== Worker checkpoint (1-1 với worker) =====
CREATE TABLE worker_checkpoint (
    worker_id    BIGINT PRIMARY KEY REFERENCES worker(id) ON DELETE CASCADE,
    last_cursor  TEXT,
    last_run_at  TIMESTAMPTZ,
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ===== Sync statistics (n-nhiều lần chạy) =====
CREATE TABLE sync_stat (
    id             BIGSERIAL PRIMARY KEY,
    worker_id      BIGINT NOT NULL REFERENCES worker(id) ON DELETE CASCADE,
    batch_id       UUID NOT NULL,
    records_ok     INT NOT NULL DEFAULT 0,
    records_failed INT NOT NULL DEFAULT 0,
    latency_ms     BIGINT,
    started_at     TIMESTAMPTZ NOT NULL,
    finished_at    TIMESTAMPTZ,
    error_message  TEXT
);
CREATE INDEX idx_sync_stat_worker_started ON sync_stat(worker_id, started_at DESC);

-- ===== Master product (không phụ thuộc tenant) =====
CREATE TABLE master_product (
    id             BIGSERIAL PRIMARY KEY,
    sku            VARCHAR(100) NOT NULL UNIQUE,     -- chống trùng lặp
    name           VARCHAR(500),
    manufacturer   VARCHAR(200),
    regulated_info JSONB NOT NULL DEFAULT '{}'::jsonb,
    first_seen_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);