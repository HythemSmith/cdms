package com.example.cdms.repository;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import com.example.cdms.dto.ShopifyProductDto;
import com.example.cdms.dto.VietfulProductEvent;

@ExtendWith(MockitoExtension.class)
class TenantProductRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    private TenantProductRepository repository;

    @BeforeEach
    void setUp() {
        repository = new TenantProductRepository(jdbcTemplate);
    }

    @Test
    void upsertFromVietful_shouldThrowWhenTenantIdIsZero() {
        VietfulProductEvent event = new VietfulProductEvent("SKU-1", "PARTNER-1", "San pham", 100.0, 10);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> repository.upsertFromVietful(0L, event));

        assertEquals("tenantId must be greater than 0", ex.getMessage());
    }

    @Test
    void upsertFromVietful_shouldThrowWhenTenantIdIsNegative() {
        VietfulProductEvent event = new VietfulProductEvent("SKU-1", "PARTNER-1", "San pham", 100.0, 10);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> repository.upsertFromVietful(-1L, event));

        assertEquals("tenantId must be greater than 0", ex.getMessage());
    }

    @Test
    void upsertFromVietful_shouldSucceedWhenTenantIdIsValid() {
        VietfulProductEvent event = new VietfulProductEvent("SKU-1", "PARTNER-1", "San pham", 100.0, 10);

        when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);

        assertDoesNotThrow(() -> repository.upsertFromVietful(1L, event));

        verify(jdbcTemplate, times(1))
                .update(org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.any(Object[].class));
    }

    @Test
    void upsertFromShopify_shouldThrowWhenTenantIdIsZero() {
        ShopifyProductDto.VariantNode variant = new ShopifyProductDto.VariantNode("SKU-1", "100000", 5);
        ShopifyProductDto.Variants variants = new ShopifyProductDto.Variants(List.of(variant));
        ShopifyProductDto dto = new ShopifyProductDto("gid://1", "San pham", variants);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> repository.upsertFromShopify(0L, dto, "SKU-1", "100000", 5));

        assertEquals("tenantId must be greater than 0", ex.getMessage());
    }

    @Test
    void upsertFromShopify_shouldSucceedWhenTenantIdIsValid() {
        ShopifyProductDto.VariantNode variant = new ShopifyProductDto.VariantNode("SKU-1", "100000", 5);
        ShopifyProductDto.Variants variants = new ShopifyProductDto.Variants(List.of(variant));
        ShopifyProductDto dto = new ShopifyProductDto("gid://1", "San pham", variants);

        when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);

        assertDoesNotThrow(() -> repository.upsertFromShopify(1L, dto, "SKU-1", "100000", 5));

        verify(jdbcTemplate, times(1))
                .update(org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.any(Object[].class));
    }
}
