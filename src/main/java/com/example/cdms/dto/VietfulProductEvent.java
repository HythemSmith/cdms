package com.example.cdms.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record VietfulProductEvent(
    @NotBlank String sku,
    @NotBlank String partnerSKU,
    @NotBlank String name,
    @NotNull @DecimalMin("0") Double price,
    @NotNull @PositiveOrZero Integer qty
) {
    
}
