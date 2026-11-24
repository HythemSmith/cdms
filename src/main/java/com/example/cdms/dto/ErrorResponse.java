package com.example.cdms.dto;

import java.time.Instant;

public record ErrorResponse(
    String timestamp,
    int status,
    String error,
    String message
) {
    public ErrorResponse {
        timestamp = timestamp == null ? Instant.now().toString() : timestamp;
    }
}
