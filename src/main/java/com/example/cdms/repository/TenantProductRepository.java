package com.example.cdms.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.cdms.dto.ShopifyProductDto;
import com.example.cdms.dto.VietfulProductEvent;

@Repository 
public class TenantProductRepository {
    private static final Logger log = LoggerFactory.getLogger(TenantProductRepository.class);

    private final JdbcTemplate jdbcTemplate;

    public TenantProductRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void upsertFromVietful(long tenantId, VietfulProductEvent event) {
        validateTenantId(tenantId);
        String schemaName = "tenant_" + tenantId;

        String sql = """
            INSERT INTO %s.product (source, external_id, sku, name, price, quantity, partner_sku)
            VALUES ('VIETFUL', ?, ?, ?, ?, ?, ?)
            ON CONFLICT (source, external_id) 
            DO UPDATE SET 
                sku = EXCLUDED.sku,
                name = EXCLUDED.name,
                price = EXCLUDED.price,
                quantity = EXCLUDED.quantity,
                partner_sku = EXCLUDED.partner_sku,
                synced_at = NOW()
            """.formatted(schemaName);

        try {
            jdbcTemplate.update(sql, 
                event.sku(),
                event.sku(),
                event.name(),
                event.price(),
                event.qty(),
                event.partnerSKU()
            );
            log.info("Upserted VIETFUL tenantId={} sku={} name={} price={} qty={}",
                    tenantId, event.sku(), event.name(), event.price(), event.qty());
        } catch (Exception e) {
            log.error("Failed to upsert VIETFUL tenantId={} sku={}: {}", tenantId, event.sku(), e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public void upsertFromShopify(long tenantId, ShopifyProductDto dto, String sku, String price, int qty) {
        validateTenantId(tenantId);
        String schemaName = "tenant_" + tenantId;

        String sql = """
            INSERT INTO %s.product (source, external_id, sku, name, price, quantity)
            VALUES ('SHOPIFY', ?, ?, ?, ?::numeric, ?)
            ON CONFLICT (source, external_id) 
            DO UPDATE SET 
                sku = EXCLUDED.sku,
                name = EXCLUDED.name,
                price = EXCLUDED.price,
                quantity = EXCLUDED.quantity,
                synced_at = NOW()
            """.formatted(schemaName);

        try {
            jdbcTemplate.update(sql, 
                dto.id(),
                sku,
                dto.title(),
                price,
                qty
            );
            log.info("Upserted SHOPIFY tenantId={} externalId={} sku={} name={} price={} qty={}",
                    tenantId, dto.id(), sku, dto.title(), price, qty);
        } catch (Exception e) {
            log.error("Failed to upsert SHOPIFY tenantId={} externalId={}: {}", tenantId, dto.id(), e.getMessage(), e);
            throw e;
        }
    }

    private void validateTenantId(long tenantId) {
        if (tenantId <= 0) {
            throw new IllegalArgumentException("tenantId must be greater than 0");
        }
    }
}
