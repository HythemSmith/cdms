package com.example.cdms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ShopifyProductDto(
    @NotBlank String id,
    @NotBlank String title,
    @NotNull Variants variants
) {
    public record Variants(@NotNull List<VariantNode> nodes) {}
    
    public record VariantNode(
        @NotBlank String sku, 
        @NotBlank String price, 
        @NotNull @JsonProperty("inventory_quantity") Integer inventoryQuantity
    ) {}
}