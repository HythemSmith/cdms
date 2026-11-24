package com.example.cdms.repository;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.example.cdms.dto.SyncStatDto;

@ExtendWith(MockitoExtension.class)
class SyncStatRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    private SyncStatRepository repository;

    @BeforeEach
    void setUp() {
        repository = new SyncStatRepository(jdbcTemplate);
    }

    @Test
    void insertSyncStat_shouldReturnStatDto() {
        UUID batchId = UUID.randomUUID();
        Instant startedAt = Instant.parse("2026-01-01T00:00:00Z");
        Instant finishedAt = Instant.parse("2026-01-01T00:00:05Z");

        SyncStatDto expected = new SyncStatDto(
                1L, 1L, batchId, 10, 0, 5000L, startedAt, finishedAt, null);

        when(jdbcTemplate.queryForObject(
                any(String.class),
                (RowMapper<SyncStatDto>) any(),
                anyLong(), any(), anyInt(), anyInt(), anyLong(), any(), any(), any()))
                .thenReturn(expected);

        SyncStatDto result = repository.insertSyncStat(
                1L, batchId, 10, 0, 5000L, startedAt, finishedAt, null);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(1L, result.workerId());
        assertEquals(batchId, result.batchId());
        assertEquals(10, result.recordsOk());
        assertEquals(0, result.recordsFailed());
        assertEquals(5000L, result.latencyMs());
    }

    @Test
    void insertSyncStat_shouldThrowWhenJdbcFails() {
        UUID batchId = UUID.randomUUID();
        when(jdbcTemplate.queryForObject(
                any(String.class),
                (RowMapper<SyncStatDto>) any(),
                anyLong(), any(), anyInt(), anyInt(), anyLong(), any(), any(), any()))
                .thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class, () -> repository.insertSyncStat(
                1L, batchId, 10, 0, 5000L, Instant.now(), Instant.now(), null));
    }

    @Test
    void insertSyncStat_shouldNotThrowWhenSuccessful() {
        UUID batchId = UUID.randomUUID();
        SyncStatDto expected = new SyncStatDto(
                1L, 1L, batchId, 5, 1, 2000L, Instant.now(), Instant.now(), null);
        when(jdbcTemplate.queryForObject(
                any(String.class),
                (RowMapper<SyncStatDto>) any(),
                anyLong(), any(), anyInt(), anyInt(), anyLong(), any(), any(), any()))
                .thenReturn(expected);

        assertDoesNotThrow(() -> repository.insertSyncStat(
                1L, batchId, 5, 1, 2000L, Instant.now(), Instant.now(), null));
    }
}
