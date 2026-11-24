package com.example.cdms.repository;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import com.example.cdms.dto.MasterProductDto;

@ExtendWith(MockitoExtension.class)
class MasterProductRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    private MasterProductRepository repository;

    @BeforeEach
    void setUp() {
        repository = new MasterProductRepository(jdbcTemplate);
    }

    @Test
    void upsertMasterProduct_shouldSucceedWithValidDto() {
        MasterProductDto dto = new MasterProductDto("SKU-001", "Ao thun", null, null);

        when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);

        assertDoesNotThrow(() -> repository.upsertMasterProduct(dto));

        verify(jdbcTemplate, times(1))
                .update(org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.any(Object[].class));
    }

    @Test
    void upsertMasterProduct_shouldSucceedWithRegulatedInfo() {
        MasterProductDto dto = new MasterProductDto("SKU-002", "Quan jean", "Thoi Trang ABC",
                Map.of("origin", "VN", "material", "cotton"));

        when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);

        assertDoesNotThrow(() -> repository.upsertMasterProduct(dto));

        verify(jdbcTemplate, times(1))
                .update(org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.any(Object[].class));
    }

    @Test
    void upsertMasterProduct_shouldThrowWhenJdbcFails() {
        MasterProductDto dto = new MasterProductDto("SKU-003", "Ao so mi", null, null);

        when(jdbcTemplate.update(anyString(), any(Object[].class)))
                .thenThrow(new RuntimeException("DB connection lost"));

        assertThrows(RuntimeException.class, () -> repository.upsertMasterProduct(dto));
    }
}
