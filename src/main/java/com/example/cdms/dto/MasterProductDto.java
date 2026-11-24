package com.example.cdms.dto;

import java.util.Map;

public record MasterProductDto(
    String sku,
    String name,
    String manufacturer,
    Map<String, Object> regulatedInfo
) {
}
