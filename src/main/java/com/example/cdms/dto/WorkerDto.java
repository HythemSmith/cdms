package com.example.cdms.dto;

public record WorkerDto(
    Long id,
    Long tenantId,
    String source,
    String name,
    String scheduleCron,
    String status
) {
}
