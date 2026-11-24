package com.example.cdms.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
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

import com.example.cdms.dto.ShopifyProductDto;
import com.example.cdms.repository.MasterProductRepository;
import com.example.cdms.repository.ShopifyRepository;
import com.example.cdms.repository.SyncStatRepository;
import com.example.cdms.repository.TenantProductRepository;

@ExtendWith(MockitoExtension.class)
class ShopifySyncServiceTest {

    @Mock
    private ShopifyRepository shopifyRepository;

    @Mock
    private TenantProductRepository tenantProductRepository;

    @Mock
    private MasterProductRepository masterProductRepository;

    @Mock
    private SyncStatRepository syncStatRepository;

    private ShopifySyncService syncService;

    @BeforeEach
    void setUp() {
        syncService = new ShopifySyncService(shopifyRepository, tenantProductRepository, masterProductRepository, syncStatRepository);
    }

    @Test
    void syncData_shouldProcessProductsSuccessfully() {
        ShopifyProductDto.VariantNode variant = new ShopifyProductDto.VariantNode("SKU-001", "199000", 50);
        ShopifyProductDto.Variants variants = new ShopifyProductDto.Variants(List.of(variant));
        ShopifyProductDto product = new ShopifyProductDto("gid://1", "Ao thun", variants);

        when(shopifyRepository.fetchProducts()).thenReturn(List.of(product));
        doNothing().when(tenantProductRepository).upsertFromShopify(
                1L, product, "SKU-001", "199000", 50);
        doNothing().when(masterProductRepository).upsertMasterProduct(
                org.mockito.ArgumentMatchers.any());

        assertDoesNotThrow(() -> syncService.syncData(1L));

        verify(tenantProductRepository, times(1))
                .upsertFromShopify(1L, product, "SKU-001", "199000", 50);
        verify(masterProductRepository, times(1)).upsertMasterProduct(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void syncData_shouldSkipProductWithNoVariants() {
        ShopifyProductDto.Variants emptyVariants = new ShopifyProductDto.Variants(List.of());
        ShopifyProductDto product = new ShopifyProductDto("gid://2", "Ao thun", emptyVariants);

        when(shopifyRepository.fetchProducts()).thenReturn(List.of(product));

        assertDoesNotThrow(() -> syncService.syncData(1L));

        verify(tenantProductRepository, never())
                .upsertFromShopify(anyLong(), any(), anyString(), anyString(), anyInt());
        verify(masterProductRepository, never()).upsertMasterProduct(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void syncData_shouldHandleNullInventoryQuantity() {
        ShopifyProductDto.VariantNode variant = new ShopifyProductDto.VariantNode("SKU-002", "299000", null);
        ShopifyProductDto.Variants variants = new ShopifyProductDto.Variants(List.of(variant));
        ShopifyProductDto product = new ShopifyProductDto("gid://3", "Quan jean", variants);

        when(shopifyRepository.fetchProducts()).thenReturn(List.of(product));
        doNothing().when(tenantProductRepository).upsertFromShopify(
                1L, product, "SKU-002", "299000", 0);
        doNothing().when(masterProductRepository).upsertMasterProduct(org.mockito.ArgumentMatchers.any());

        assertDoesNotThrow(() -> syncService.syncData(1L));

        verify(tenantProductRepository, times(1))
                .upsertFromShopify(1L, product, "SKU-002", "299000", 0);
        verify(masterProductRepository, times(1)).upsertMasterProduct(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void syncData_shouldContinueOnRepositoryException() {
        ShopifyProductDto.VariantNode variant = new ShopifyProductDto.VariantNode("SKU-003", "399000", 10);
        ShopifyProductDto.Variants variants = new ShopifyProductDto.Variants(List.of(variant));
        ShopifyProductDto product = new ShopifyProductDto("gid://4", "Ao so mi", variants);

        when(shopifyRepository.fetchProducts()).thenReturn(List.of(product));
        doThrow(new RuntimeException("DB error"))
                .when(tenantProductRepository)
                .upsertFromShopify(anyLong(), any(), anyString(), anyString(), anyInt());

        assertDoesNotThrow(() -> syncService.syncData(1L));
    }

    @Test
    void syncData_shouldHandleEmptyProductList() {
        when(shopifyRepository.fetchProducts()).thenReturn(List.of());

        assertDoesNotThrow(() -> syncService.syncData(1L));

        verify(tenantProductRepository, never())
                .upsertFromShopify(anyLong(), any(), anyString(), anyString(), anyInt());
        verify(masterProductRepository, never()).upsertMasterProduct(org.mockito.ArgumentMatchers.any());
    }
}
