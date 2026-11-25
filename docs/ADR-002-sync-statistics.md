# ADR-002: Sync Statistics Tracking

## Status
Accepted

## Context
The requirement states: "Performance measurements and statistics for data sync, e.g., throughput, latency, wrong data records, etc." and "Able to start and stop workers; workers start from the last checkpoints when they stop."

## Decision
Track sync statistics in a shared `sync_stat` table (public schema). Each sync batch records:
- `worker_id` — which worker ran the sync
- `batch_id` — UUID for the batch (idempotency/traceability)
- `records_ok` — number of successfully synced products
- `records_failed` — number of skipped/failed products
- `latency_ms` — total sync duration
- `started_at` / `finished_at` — timestamps
- `error_message` — error text if the whole batch failed

### Table DDL (V1__init_global.sql)
```sql
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
```

## Rationale
- **Centralized statistics**: All tenant sync stats in one table for cross-tenant reporting.
- **Batch-level granularity**: One row per sync run, enabling throughput and latency trend analysis.
- **Failure tracking**: `records_failed` and `error_message` capture wrong/malformed data counts.
- **Worker linkage**: Foreign key to `worker` allows filtering stats per worker/tenant.

## Consequences
- **Positive**: Enables operational monitoring, alerting on high failure rates, and capacity planning.
- **Negative**: Statistics are only recorded when `workerId` is provided (manual sync without worker ID skips statistics).

## Related
- `ShopifySyncService.syncData(long tenantId, Long workerId)` — records stats in `finally` block.
- `SyncStatRepository.insertSyncStat()` — persists the stat row.
