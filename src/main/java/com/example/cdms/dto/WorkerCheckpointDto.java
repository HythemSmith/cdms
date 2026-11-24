package com.example.cdms.dto;

public record WorkerCheckpointDto(
    Long workerId,
    String lastCursor,
    String lastRunAt
) {
}
