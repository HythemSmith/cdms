package com.example.cdms.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.cdms.dto.WorkerCheckpointDto;
import com.example.cdms.dto.WorkerDto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository 
public class WorkerRepository {
    private static final Logger log = LoggerFactory.getLogger(WorkerRepository.class);

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<WorkerDto> workerRowMapper = (rs, rowNum) -> new WorkerDto(
        rs.getLong("id"),
        rs.getLong("tenant_id"),
        rs.getString("source"),
        rs.getString("name"),
        rs.getString("schedule_cron"),
        rs.getString("status")
    );

    private final RowMapper<WorkerCheckpointDto> checkpointRowMapper = (rs, rowNum) -> new WorkerCheckpointDto(
        rs.getLong("worker_id"),
        rs.getString("last_cursor"),
        rs.getString("last_run_at")
    );

    public WorkerRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public List<WorkerDto> findAllWorkers() {
        String sql = "SELECT id, tenant_id, source, name, schedule_cron, status FROM worker ORDER BY id";
        return jdbcTemplate.query(sql, workerRowMapper);
    }

    @Transactional(readOnly = true)
    public Optional<WorkerDto> findById(Long workerId) {
        String sql = "SELECT id, tenant_id, source, name, schedule_cron, status FROM worker WHERE id = ?";
        List<WorkerDto> result = jdbcTemplate.query(sql, workerRowMapper, workerId);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Transactional(readOnly = true)
    public Optional<WorkerCheckpointDto> findCheckpointByWorkerId(Long workerId) {
        String sql = "SELECT worker_id, last_cursor, last_run_at FROM worker_checkpoint WHERE worker_id = ?";
        List<WorkerCheckpointDto> result = jdbcTemplate.query(sql, checkpointRowMapper, workerId);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Transactional
    public int startWorker(Long workerId) {
        String sql = """
            UPDATE worker
            SET status = 'RUNNING', updated_at = NOW()
            WHERE id = ? AND status != 'RUNNING'
            """;
        int updated = jdbcTemplate.update(sql, workerId);
        log.info("Started workerId={}, rowsUpdated={}", workerId, updated);
        return updated;
    }

    @Transactional
    public int stopWorker(Long workerId) {
        String sql = """
            UPDATE worker
            SET status = 'STOPPED', updated_at = NOW()
            WHERE id = ? AND status != 'STOPPED'
            """;
        int updated = jdbcTemplate.update(sql, workerId);
        log.info("Stopped workerId={}, rowsUpdated={}", workerId, updated);
        return updated;
    }

    @Transactional
    public int upsertCheckpoint(Long workerId, String lastCursor, String lastRunAt) {
        String sql = """
            INSERT INTO worker_checkpoint (worker_id, last_cursor, last_run_at, updated_at)
            VALUES (?, ?, ?, NOW())
            ON CONFLICT (worker_id) DO UPDATE SET
                last_cursor = EXCLUDED.last_cursor,
                last_run_at = EXCLUDED.last_run_at,
                updated_at = NOW()
            """;
        int updated = jdbcTemplate.update(sql, workerId, lastCursor, lastRunAt);
        log.info("Upserted checkpoint workerId={} lastCursor={} lastRunAt={}",
                workerId, lastCursor, lastRunAt);
        return updated;
    }

    private static final class WorkerRowMapper implements RowMapper<WorkerDto> {
        @Override
        public WorkerDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new WorkerDto(
                rs.getLong("id"),
                rs.getLong("tenant_id"),
                rs.getString("source"),
                rs.getString("name"),
                rs.getString("schedule_cron"),
                rs.getString("status")
            );
        }
    }

    private static final class CheckpointRowMapper implements RowMapper<WorkerCheckpointDto> {
        @Override
        public WorkerCheckpointDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new WorkerCheckpointDto(
                rs.getLong("worker_id"),
                rs.getString("last_cursor"),
                rs.getString("last_run_at")
            );
        }
    }
}
