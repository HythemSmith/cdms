# ADR-001: Multi-tenant Schema Isolation

## Status
Accepted

## Context
CDMS serves multiple tenants. Each tenant syncs product data from Shopify and/or Vietful. The requirement states: "Data of different tenants is not stored the same table" and "no duplicated data is allowed".

## Decision
Store each tenant's product data in a separate PostgreSQL schema named `tenant_{id}`. Each schema contains an identical `product` table. Tenant master data (tenant config, workers, checkpoints, statistics) lives in the public schema.

### Schema DDL (V2__demo_tenant_1.sql)
```sql
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
    UNIQUE (source, external_id)
);
```

## Rationale
- **Isolation**: Tenant data is physically separated; one tenant cannot read another tenant's data unless explicitly allowed.
- **Scalability**: Schemas can be moved to separate databases/servers later without code changes.
- **Simplicity**: No need for row-level security or tenant column filters in every query.
- **Unique constraint**: `UNIQUE (source, external_id)` per schema prevents duplicate sync records.

## Consequences
- **Positive**: Strong data isolation, simple queries, easy per-tenant backup/restore.
- **Negative**: Schema name must be validated to prevent SQL injection (see `validateTenantId()` in `TenantProductRepository`). Adding a new tenant requires creating a new schema (automated via migration or provisioning script).

## Related
- `TenantProductRepository.validateTenantId()` — rejects `tenantId <= 0` to prevent schema injection.
