# CDMS - Change Data Management System

A simple Change Data Management System (CDMS) for multiple tenants, built with Java 21 and Spring Boot 4.1.

## Overview

CDMS synchronizes inventory product data from two sources:
- **Vietful Inventory Service** — pushed via Kafka (near real-time, tenant-specific messaging configuration)
- **Shopify** — polled via REST API (tenant-specific access)

Synced data is stored in a single PostgreSQL database with per-tenant schema isolation (`tenant_{id}`). A shared `master_product` table holds consolidated product information across all tenants.

## Features

- Multi-tenant data isolation (separate schema per tenant)
- Shopify product sync (REST pull + master product update)
- Vietful product event consumption (Kafka push)
- Worker lifecycle management (start/stop, checkpoint resume)
- Sync statistics tracking (throughput, latency, success/failed counts)
- Global exception handling with structured error responses
- Input validation (DTO constraints + tenant ID validation)
- Structured logging (SLF4J)
- Docker Compose for Postgres + Kafka

## Tech Stack

- Java 21
- Spring Boot 4.1.2
- Spring Web, Spring JDBC, Spring Kafka, Spring Validation
- Flyway (database migration)
- PostgreSQL 16
- Apache Kafka 4.2.2
- Gradle
- JUnit 5 + Mockito

## Project Structure

```
src/main/java/com/example/cdms/
├── controller/          # REST controllers
├── dto/                 # Data transfer objects
├── exception/           # Global exception handler
├── repository/          # JDBC repositories
└── service/             # Business services
src/main/resources/
├── db/migration/        # Flyway SQL migrations
└── application.yml      # Application configuration
src/test/java/           # Unit tests (JUnit 5 + Mockito)
```

## Database Schema

- `tenant` — tenant master data
- `worker` — sync workers per tenant (Shopify/Vietful)
- `worker_checkpoint` — last cursor for resume-on-restart
- `sync_stat` — sync statistics (records OK/failed, latency)
- `master_product` — consolidated product catalog (unique SKU)
- `tenant_{id}.product` — per-tenant product storage

## How to Run

### Prerequisites
- Java 21+
- Gradle
- Docker & Docker Compose

### 1. Start Infrastructure

```bash
docker-compose up -d
```

This starts PostgreSQL (port 5432) and Kafka (port 9092).

### 2. Run the Application

```bash
./gradlew bootRun
```

The app starts on `http://localhost:8080`.

### 3. Run Tests

```bash
./gradlew test
```

## API Endpoints

### Shopify Sync
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/sync/shopify?tenant={id}` | Trigger Shopify sync for a tenant |
| POST | `/sync/shopify?tenant={id}&worker={id}` | Trigger sync with worker ID for statistics |

### Worker Management
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/workers` | List all workers |
| GET | `/workers/{id}` | Get worker by ID |
| GET | `/workers/{id}/checkpoint` | Get worker checkpoint |
| GET | `/workers/{id}/last-cursor` | Get last sync cursor |
| POST | `/workers/{id}/start` | Start a worker |
| POST | `/workers/{id}/stop` | Stop a worker |

### Mock Data Sources
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/mock/shopify/products` | Mock Shopify products |
| GET | `/mock/error/shopify/invalid-variant` | Mock product with no variant |
| GET | `/mock/error/shopify/null-fields` | Mock product with null fields |
| POST | `/mock/vietful/push` | Push Vietful event to Kafka |
| POST | `/mock/error/vietful/invalid` | Push invalid Vietful event (validation error) |

### Example: Sync Shopify for tenant 1

```bash
curl -X POST "http://localhost:8080/sync/shopify?tenant=1"
```

### Example: Push Vietful Event

```bash
curl -X POST "http://localhost:8080/mock/vietful/push" \
  -H "Content-Type: application/json" \
  -d '{"sku":"VIET-001","partnerSKU":"SHOP-01","name":"Product","price":100.0,"qty":100}'
```

## Error Handling

Invalid data (missing SKU, negative quantity, invalid price) is logged as a warning and skipped. The sync continues with remaining products. All unexpected exceptions return a structured JSON error response:

```json
{
  "timestamp": "2026-11-24T13:00:00Z",
  "status": 400,
  "error": "Validation failed",
  "message": "sku: sku is required;"
}
```

## Testing

Unit tests use JUnit 5 + Mockito to cover:
- `ShopifySyncService` — sync logic, null-safety, invalid data skipping, statistics
- `TenantProductRepository` — upsert, tenant ID validation
- `MasterProductRepository` — master product upsert
- `SyncStatRepository` — statistics insert
- `WorkerRepository` / `WorkerService` — worker lifecycle, checkpoint
- `GlobalExceptionHandler` — error response structure
- `SyncController` / `WorkerController` — endpoint behavior

Run all unit tests:

```bash
./gradlew test
```

## Architecture Decision Records

See `docs/` for ADRs:
- `ADR-001-multi-tenant-schema.md`
- `ADR-002-sync-statistics.md`

## License

MIT License
