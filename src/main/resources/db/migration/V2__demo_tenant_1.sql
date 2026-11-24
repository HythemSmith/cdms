CREATE SCHEMA IF NOT EXISTS tenant_1;

CREATE TABLE tenant_1.product (
    id          BIGSERIAL PRIMARY KEY,
    source      VARCHAR(20) NOT NULL CHECK (source IN ('VIETFUL','SHOPIFY')),
    external_id VARCHAR(100) NOT NULL,
    sku         VARCHAR(100),
    name        VARCHAR(500),
    price       NUMERIC(15,2),
    quantity    INT NOT NULL DEFAULT 0,
    raw_payload JSONB NOT NULL DEFAULT '{}'::jsonb,
    synced_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (source, external_id)                     -- chống trùng dữ liệu
);

CREATE INDEX idx_tenant_1_product_sku ON tenant_1.product(sku);