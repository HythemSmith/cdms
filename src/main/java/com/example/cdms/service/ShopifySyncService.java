package com.example.cdms.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.cdms.dto.MasterProductDto;
import com.example.cdms.dto.ShopifyProductDto;
import com.example.cdms.dto.SyncStatDto;
import com.example.cdms.repository.MasterProductRepository;
import com.example.cdms.repository.ShopifyRepository;
import com.example.cdms.repository.SyncStatRepository;
import com.example.cdms.repository.TenantProductRepository;

@Service 
public class ShopifySyncService {
    private static final Logger log = LoggerFactory.getLogger(ShopifySyncService.class);

    private final ShopifyRepository shopifyRepository;
    private final TenantProductRepository tenantProductRepository;
    private final MasterProductRepository masterProductRepository;
    private final SyncStatRepository syncStatRepository;

    public ShopifySyncService(ShopifyRepository repository,
                              TenantProductRepository tenantProductRepository,
                              MasterProductRepository masterProductRepository,
                              SyncStatRepository syncStatRepository) {
        this.shopifyRepository = repository;
        this.tenantProductRepository = tenantProductRepository;
        this.masterProductRepository = masterProductRepository;
        this.syncStatRepository = syncStatRepository;
    }

    @Transactional
    public void syncData(long tenantId) {
        syncData(tenantId, null);
    }

    @Transactional
    public void syncData(long tenantId, Long workerId) {
        Instant startedAt = Instant.now();
        UUID batchId = UUID.randomUUID();
        int success = 0;
        int failed = 0;
        String errorMessage = null;

        try {
            log.info("Starting Shopify sync for tenantId={} batchId={}", tenantId, batchId);
            List<ShopifyProductDto> products = shopifyRepository.fetchProducts();
            log.info("Received {} products from Shopify", products.size());

            for (ShopifyProductDto product : products) {
                try {
                    List<ShopifyProductDto.VariantNode> nodes = product.variants() == null
                            ? List.of()
                            : product.variants().nodes();
                    if (nodes.isEmpty()) {
                        log.warn("Product {} has no variant, skipping", product.id());
                        failed++;
                        continue;
                    }

                    ShopifyProductDto.VariantNode node = nodes.get(0);
                    Integer qty = node.inventoryQuantity() == null ? 0 : node.inventoryQuantity();
                    tenantProductRepository.upsertFromShopify(
                            tenantId,
                            product,
                            node.sku() == null ? "UNKNOWN" : node.sku(),
                            node.price() == null ? "0" : node.price(),
                            qty);

                    MasterProductDto masterDto = new MasterProductDto(
                            node.sku() == null ? "UNKNOWN" : node.sku(),
                            product.title(),
                            null,
                            null);
                    masterProductRepository.upsertMasterProduct(masterDto);

                    log.info("Synced product id={} title={} sku={} price={} qty={}",
                            product.id(), product.title(), node.sku(), node.price(), qty);
                    success++;
                } catch (Exception e) {
                    log.error("Failed to sync product id={} title={}: {}", product.id(), product.title(), e.getMessage(), e);
                    failed++;
                }
            }

            log.info("Completed sync tenantId={}: success={} failed={}", tenantId, success, failed);
        } catch (Exception e) {
            errorMessage = e.getMessage();
            log.error("Sync failed for tenantId={} batchId={}: {}", tenantId, batchId, e.getMessage(), e);
            throw e;
        } finally {
            Instant finishedAt = Instant.now();
            long latencyMs = java.time.Duration.between(startedAt, finishedAt).toMillis();
            if (workerId != null) {
                syncStatRepository.insertSyncStat(
                        workerId, batchId, success, failed, latencyMs,
                        startedAt, finishedAt, errorMessage);
            }
        }
    }
}
