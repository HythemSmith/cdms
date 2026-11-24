package com.example.cdms.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.cdms.dto.SyncStatDto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;

@Repository 
public class SyncStatRepository {
    private static final Logger log = LoggerFactory.getLogger(SyncStatRepository.class);

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<SyncStatDto> syncStatRowMapper = (rs, rowNum) -> new SyncStatDto(
        rs.getLong("id"),
        rs.getLong("worker_id"),
        UUID.fromString(rs.getString("batch_id")),
        rs.getInt("records_ok"),
        rs.getInt("records_failed"),
        rs.getLong("latency_ms"),
        rs.getTimestamp("started_at") == null ? null : rs.getTimestamp("started_at").toInstant(),
        rs.getTimestamp("finished_at") == null ? null : rs.getTimestamp("finished_at").toInstant(),
        rs.getString("error_message")
    );

    public SyncStatRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public SyncStatDto insertSyncStat(Long workerId, UUID batchId, Integer recordsOk,
                                      Integer recordsFailed, Long latencyMs,
                                      java.time.Instant startedAt, java.time.Instant finishedAt,
                                      String errorMessage) {
        String sql = """
            INSERT INTO sync_stat (worker_id, batch_id, records_ok, records_failed,
                                   latency_ms, started_at, finished_at, error_message)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id, worker_id, batch_id, records_ok, records_failed,
                      latency_ms, started_at, finished_at, error_message
            """;

        try {
            SyncStatDto stat = jdbcTemplate.queryForObject(sql, syncStatRowMapper,
                workerId, batchId, recordsOk, recordsFailed, latencyMs,
                startedAt != null ? java.sql.Timestamp.from(startedAt) : null,
                finishedAt != null ? java.sql.Timestamp.from(finishedAt) : null,
                errorMessage);
            log.info("Inserted sync_stat id={} workerId={} batchId={} recordsOk={} recordsFailed={} latencyMs={}",
                    stat.id(), workerId, batchId, recordsOk, recordsFailed, latencyMs);
            return stat;
        } catch (Exception e) {
            log.error("Failed to insert sync_stat workerId={} batchId={}: {}", workerId, batchId, e.getMessage(), e);
            throw e;
        }
    }
}
