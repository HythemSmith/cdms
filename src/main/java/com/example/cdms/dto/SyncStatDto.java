package com.example.cdms.dto;

import java.time.Instant;
import java.util.UUID;

public record SyncStatDto(
    Long id,
    Long workerId,
    UUID batchId,
    Integer recordsOk,
    Integer recordsFailed,
    Long latencyMs,
    Instant startedAt,
    Instant finishedAt,
    String errorMessage
) {
}
