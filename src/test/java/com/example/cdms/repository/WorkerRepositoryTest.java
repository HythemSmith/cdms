package com.example.cdms.repository;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.example.cdms.dto.WorkerCheckpointDto;
import com.example.cdms.dto.WorkerDto;

@ExtendWith(MockitoExtension.class)
class WorkerRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    private WorkerRepository repository;

    @BeforeEach
    void setUp() {
        repository = new WorkerRepository(jdbcTemplate);
    }

    @Test
    void findAllWorkers_shouldReturnList() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class)))
                .thenReturn(Collections.emptyList());

        List<WorkerDto> result = repository.findAllWorkers();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), anyLong()))
                .thenReturn(Collections.emptyList());

        Optional<WorkerDto> result = repository.findById(1L);

        assertFalse(result.isPresent());
    }

    @Test
    void findCheckpointByWorkerId_shouldReturnEmptyWhenNotFound() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), anyLong()))
                .thenReturn(Collections.emptyList());

        Optional<WorkerCheckpointDto> result = repository.findCheckpointByWorkerId(1L);

        assertFalse(result.isPresent());
    }

    @Test
    void startWorker_shouldReturnUpdatedCount() {
        when(jdbcTemplate.update(anyString(), anyLong())).thenReturn(1);

        int result = repository.startWorker(1L);

        assertEquals(1, result);
    }

    @Test
    void stopWorker_shouldReturnUpdatedCount() {
        when(jdbcTemplate.update(anyString(), anyLong())).thenReturn(1);

        int result = repository.stopWorker(1L);

        assertEquals(1, result);
    }

    @Test
    void upsertCheckpoint_shouldReturnUpdatedCount() {
        when(jdbcTemplate.update(anyString(), anyLong(), any(), any())).thenReturn(1);

        int result = repository.upsertCheckpoint(1L, "cursor-123", "2026-01-01T00:00:00Z");

        assertEquals(1, result);
    }

    @Test
    void upsertCheckpoint_shouldNotThrow() {
        when(jdbcTemplate.update(anyString(), anyLong(), any(), any())).thenReturn(1);

        assertDoesNotThrow(() -> repository.upsertCheckpoint(1L, "cursor-123", "2026-01-01T00:00:00Z"));
    }
}
