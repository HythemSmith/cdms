package com.example.cdms.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
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

import com.example.cdms.dto.WorkerCheckpointDto;
import com.example.cdms.dto.WorkerDto;
import com.example.cdms.repository.WorkerRepository;

@ExtendWith(MockitoExtension.class)
class WorkerServiceTest {

    @Mock
    private WorkerRepository workerRepository;

    private WorkerService workerService;

    @BeforeEach
    void setUp() {
        workerService = new WorkerService(workerRepository);
    }

    @Test
    void listWorkers_shouldReturnList() {
        when(workerRepository.findAllWorkers()).thenReturn(Collections.emptyList());

        List<WorkerDto> result = workerService.listWorkers();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getWorker_shouldReturnEmptyWhenNotFound() {
        when(workerRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<WorkerDto> result = workerService.getWorker(99L);

        assertFalse(result.isPresent());
    }

    @Test
    void getCheckpoint_shouldReturnEmptyWhenNotFound() {
        when(workerRepository.findCheckpointByWorkerId(99L)).thenReturn(Optional.empty());

        Optional<WorkerCheckpointDto> result = workerService.getCheckpoint(99L);

        assertFalse(result.isPresent());
    }

    @Test
    void startWorker_shouldReturnTrueWhenWorkerFoundAndStarted() {
        WorkerDto worker = new WorkerDto(1L, 1L, "SHOPIFY", "worker-1", null, "STOPPED");
        when(workerRepository.findById(1L)).thenReturn(Optional.of(worker));
        when(workerRepository.startWorker(1L)).thenReturn(1);

        boolean result = workerService.startWorker(1L);

        assertTrue(result);
        verify(workerRepository, times(1)).startWorker(1L);
    }

    @Test
    void startWorker_shouldReturnFalseWhenWorkerNotFound() {
        when(workerRepository.findById(99L)).thenReturn(Optional.empty());

        boolean result = workerService.startWorker(99L);

        assertFalse(result);
        verify(workerRepository, never()).startWorker(anyLong());
    }

    @Test
    void startWorker_shouldReturnTrueWhenAlreadyRunning() {
        WorkerDto worker = new WorkerDto(1L, 1L, "SHOPIFY", "worker-1", null, "RUNNING");
        when(workerRepository.findById(1L)).thenReturn(Optional.of(worker));

        boolean result = workerService.startWorker(1L);

        assertTrue(result);
        verify(workerRepository, never()).startWorker(anyLong());
    }

    @Test
    void stopWorker_shouldReturnTrueWhenWorkerFoundAndStopped() {
        WorkerDto worker = new WorkerDto(1L, 1L, "SHOPIFY", "worker-1", null, "RUNNING");
        when(workerRepository.findById(1L)).thenReturn(Optional.of(worker));
        when(workerRepository.stopWorker(1L)).thenReturn(1);

        boolean result = workerService.stopWorker(1L);

        assertTrue(result);
        verify(workerRepository, times(1)).stopWorker(1L);
    }

    @Test
    void stopWorker_shouldReturnFalseWhenWorkerNotFound() {
        when(workerRepository.findById(99L)).thenReturn(Optional.empty());

        boolean result = workerService.stopWorker(99L);

        assertFalse(result);
        verify(workerRepository, never()).stopWorker(anyLong());
    }

    @Test
    void stopWorker_shouldReturnTrueWhenAlreadyStopped() {
        WorkerDto worker = new WorkerDto(1L, 1L, "SHOPIFY", "worker-1", null, "STOPPED");
        when(workerRepository.findById(1L)).thenReturn(Optional.of(worker));

        boolean result = workerService.stopWorker(1L);

        assertTrue(result);
        verify(workerRepository, never()).stopWorker(anyLong());
    }

    @Test
    void getLastCursor_shouldReturnNullWhenNoCheckpoint() {
        when(workerRepository.findCheckpointByWorkerId(1L)).thenReturn(Optional.empty());

        String cursor = workerService.getLastCursor(1L);

        assertNull(cursor);
    }

    @Test
    void getLastCursor_shouldReturnCursorWhenCheckpointExists() {
        WorkerCheckpointDto checkpoint = new WorkerCheckpointDto(1L, "cursor-abc", "2026-01-01T00:00:00Z");
        when(workerRepository.findCheckpointByWorkerId(1L)).thenReturn(Optional.of(checkpoint));

        String cursor = workerService.getLastCursor(1L);

        assertEquals("cursor-abc", cursor);
    }

    @Test
    void getLastCursor_shouldReturnNullWhenCheckpointHasNullCursor() {
        WorkerCheckpointDto checkpoint = new WorkerCheckpointDto(1L, null, "2026-01-01T00:00:00Z");
        when(workerRepository.findCheckpointByWorkerId(1L)).thenReturn(Optional.of(checkpoint));

        String cursor = workerService.getLastCursor(1L);

        assertNull(cursor);
    }

    @Test
    void getLastCursor_shouldNotThrow() {
        when(workerRepository.findCheckpointByWorkerId(1L)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> workerService.getLastCursor(1L));
    }
}
