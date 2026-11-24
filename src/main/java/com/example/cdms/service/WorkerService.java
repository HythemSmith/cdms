package com.example.cdms.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.cdms.dto.WorkerCheckpointDto;
import com.example.cdms.dto.WorkerDto;
import com.example.cdms.repository.WorkerRepository;

import java.util.List;
import java.util.Optional;

@Service 
public class WorkerService {
    private static final Logger log = LoggerFactory.getLogger(WorkerService.class);

    private final WorkerRepository workerRepository;

    public WorkerService(WorkerRepository workerRepository) {
        this.workerRepository = workerRepository;
    }

    @Transactional(readOnly = true)
    public List<WorkerDto> listWorkers() {
        return workerRepository.findAllWorkers();
    }

    @Transactional(readOnly = true)
    public Optional<WorkerDto> getWorker(Long workerId) {
        return workerRepository.findById(workerId);
    }

    @Transactional(readOnly = true)
    public Optional<WorkerCheckpointDto> getCheckpoint(Long workerId) {
        return workerRepository.findCheckpointByWorkerId(workerId);
    }

    @Transactional
    public boolean startWorker(Long workerId) {
        Optional<WorkerDto> worker = workerRepository.findById(workerId);
        if (worker.isEmpty()) {
            log.warn("Cannot start worker: workerId={} not found", workerId);
            return false;
        }

        if ("RUNNING".equals(worker.get().status())) {
            log.info("WorkerId={} is already RUNNING", workerId);
            return true;
        }

        int updated = workerRepository.startWorker(workerId);
        boolean started = updated > 0;
        if (started) {
            log.info("WorkerId={} started successfully", workerId);
        } else {
            log.warn("Failed to start workerId={}", workerId);
        }
        return started;
    }

    @Transactional
    public boolean stopWorker(Long workerId) {
        Optional<WorkerDto> worker = workerRepository.findById(workerId);
        if (worker.isEmpty()) {
            log.warn("Cannot stop worker: workerId={} not found", workerId);
            return false;
        }

        if ("STOPPED".equals(worker.get().status())) {
            log.info("WorkerId={} is already STOPPED", workerId);
            return true;
        }

        int updated = workerRepository.stopWorker(workerId);
        boolean stopped = updated > 0;
        if (stopped) {
            log.info("WorkerId={} stopped successfully", workerId);
        } else {
            log.warn("Failed to stop workerId={}", workerId);
        }
        return stopped;
    }

    @Transactional(readOnly = true)
    public String getLastCursor(Long workerId) {
        Optional<WorkerCheckpointDto> checkpoint = workerRepository.findCheckpointByWorkerId(workerId);
        if (checkpoint.isPresent() && checkpoint.get().lastCursor() != null) {
            log.info("Retrieved lastCursor for workerId={}: {}", workerId, checkpoint.get().lastCursor());
            return checkpoint.get().lastCursor();
        }
        log.info("No checkpoint found for workerId={}, starting from beginning", workerId);
        return null;
    }
}
