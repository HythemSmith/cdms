package com.example.cdms.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.cdms.dto.WorkerCheckpointDto;
import com.example.cdms.dto.WorkerDto;
import com.example.cdms.service.WorkerService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/workers")
public class WorkerController {
    private final WorkerService workerService;

    public WorkerController(WorkerService workerService) {
        this.workerService = workerService;
    }

    @GetMapping
    public List<WorkerDto> listWorkers() {
        return workerService.listWorkers();
    }

    @GetMapping("/{workerId}")
    public WorkerDto getWorker(@PathVariable Long workerId) {
        Optional<WorkerDto> worker = workerService.getWorker(workerId);
        return worker.orElse(null);
    }

    @GetMapping("/{workerId}/checkpoint")
    public WorkerCheckpointDto getCheckpoint(@PathVariable Long workerId) {
        Optional<WorkerCheckpointDto> checkpoint = workerService.getCheckpoint(workerId);
        return checkpoint.orElse(null);
    }

    @PostMapping("/{workerId}/start")
    public String startWorker(@PathVariable Long workerId) {
        boolean started = workerService.startWorker(workerId);
        return started ? "Worker " + workerId + " started" : "Failed to start worker " + workerId;
    }

    @PostMapping("/{workerId}/stop")
    public String stopWorker(@PathVariable Long workerId) {
        boolean stopped = workerService.stopWorker(workerId);
        return stopped ? "Worker " + workerId + " stopped" : "Failed to stop worker " + workerId;
    }

    @GetMapping("/{workerId}/last-cursor")
    public String getLastCursor(@PathVariable Long workerId) {
        String cursor = workerService.getLastCursor(workerId);
        return cursor == null ? "no-checkpoint" : cursor;
    }
}
