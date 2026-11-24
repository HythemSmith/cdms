package com.example.cdms.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

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
class ShopifySyncServiceWithStatTest {

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
        syncService = new ShopifySyncService(
                shopifyRepository, tenantProductRepository, masterProductRepository, syncStatRepository);
    }

    @Test
    void syncData_withWorker_shouldInsertSyncStat() {
        ShopifyProductDto.VariantNode variant = new ShopifyProductDto.VariantNode("SKU-001", "199000", 50);
        ShopifyProductDto.Variants variants = new ShopifyProductDto.Variants(List.of(variant));
        ShopifyProductDto product = new ShopifyProductDto("gid://1", "Ao thun", variants);

        when(shopifyRepository.fetchProducts()).thenReturn(List.of(product));
        doNothing().when(tenantProductRepository).upsertFromShopify(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyInt());
        doNothing().when(masterProductRepository).upsertMasterProduct(org.mockito.ArgumentMatchers.any());
        when(syncStatRepository.insertSyncStat(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(null);

        assertDoesNotThrow(() -> syncService.syncData(1L, 10L));

        verify(syncStatRepository, times(1)).insertSyncStat(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void syncData_withoutWorker_shouldNotInsertSyncStat() {
        ShopifyProductDto.VariantNode variant = new ShopifyProductDto.VariantNode("SKU-001", "199000", 50);
        ShopifyProductDto.Variants variants = new ShopifyProductDto.Variants(List.of(variant));
        ShopifyProductDto product = new ShopifyProductDto("gid://1", "Ao thun", variants);

        when(shopifyRepository.fetchProducts()).thenReturn(List.of(product));
        doNothing().when(tenantProductRepository).upsertFromShopify(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyInt());
        doNothing().when(masterProductRepository).upsertMasterProduct(org.mockito.ArgumentMatchers.any());

        assertDoesNotThrow(() -> syncService.syncData(1L));

        verify(syncStatRepository, never()).insertSyncStat(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void syncData_shouldHandleEmptyProductListWithWorker() {
        when(shopifyRepository.fetchProducts()).thenReturn(List.of());
        when(syncStatRepository.insertSyncStat(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(null);

        assertDoesNotThrow(() -> syncService.syncData(1L, 10L));

        verify(syncStatRepository, times(1)).insertSyncStat(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void syncData_shouldContinueOnRepositoryExceptionAndStillRecordStat() {
        ShopifyProductDto.VariantNode variant = new ShopifyProductDto.VariantNode("SKU-003", "399000", 10);
        ShopifyProductDto.Variants variants = new ShopifyProductDto.Variants(List.of(variant));
        ShopifyProductDto product = new ShopifyProductDto("gid://4", "Ao so mi", variants);

        when(shopifyRepository.fetchProducts()).thenReturn(List.of(product));
        doThrow(new RuntimeException("DB error"))
                .when(tenantProductRepository)
                .upsertFromShopify(org.mockito.ArgumentMatchers.anyLong(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.anyInt());

        assertDoesNotThrow(() -> syncService.syncData(1L, 10L));

        verify(syncStatRepository, times(1)).insertSyncStat(any(), any(), any(), any(), any(), any(), any(), any());
    }
}
