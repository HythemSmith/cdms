package com.example.cdms.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.cdms.dto.MasterProductDto;

@Repository 
public class MasterProductRepository {
    private static final Logger log = LoggerFactory.getLogger(MasterProductRepository.class);

    private final JdbcTemplate jdbcTemplate;

    public MasterProductRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void upsertMasterProduct(MasterProductDto dto) {
        String sql = """
            INSERT INTO master_product (sku, name, manufacturer, regulated_info, updated_at)
            VALUES (?, ?, ?, ?, NOW())
            ON CONFLICT (sku) DO UPDATE SET
                name = EXCLUDED.name,
                manufacturer = EXCLUDED.manufacturer,
                regulated_info = EXCLUDED.regulated_info,
                updated_at = NOW()
            """;

        try {
            jdbcTemplate.update(sql,
                dto.sku(),
                dto.name(),
                dto.manufacturer(),
                dto.regulatedInfo() == null ? "{}" : dto.regulatedInfo());
            log.info("Upserted master product sku={} name={}", dto.sku(), dto.name());
        } catch (Exception e) {
            log.error("Failed to upsert master product sku={}: {}", dto.sku(), e.getMessage(), e);
            throw e;
        }
    }
}
