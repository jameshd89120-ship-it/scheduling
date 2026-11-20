package com.example.scheduling.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 兼容已有 PostgreSQL 库：补齐 P0 新增列/表，避免 SQLGrammarException。
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class SchemaRepair implements ApplicationRunner {

    private final JdbcTemplate jdbc;

    @Override
    public void run(ApplicationArguments args) {
        try {
            repairEmployee();
            repairScheduleWeek();
            repairLeaveRequest();
            log.info("SchemaRepair completed");
        } catch (Exception e) {
            log.error("SchemaRepair failed: {}", e.getMessage(), e);
        }
    }

    private void repairEmployee() {
        execQuiet("ALTER TABLE employee ADD COLUMN IF NOT EXISTS department varchar(50)");
        execQuiet("ALTER TABLE employee ADD COLUMN IF NOT EXISTS status varchar(20) DEFAULT 'ACTIVE'");
        execQuiet("ALTER TABLE employee ADD COLUMN IF NOT EXISTS hire_date date");
        execQuiet("ALTER TABLE employee ADD COLUMN IF NOT EXISTS note varchar(255)");
        execQuiet("UPDATE employee SET status = 'ACTIVE' WHERE status IS NULL");
        execQuiet("UPDATE employee SET department = '门店' WHERE department IS NULL");
    }

    private void repairScheduleWeek() {
        execQuiet(
                "CREATE TABLE IF NOT EXISTS schedule_week (" +
                        "id bigserial PRIMARY KEY, " +
                        "week_start date NOT NULL, " +
                        "status varchar(20) NOT NULL DEFAULT 'DRAFT', " +
                        "published_at timestamp, " +
                        "CONSTRAINT uk_week_start UNIQUE (week_start)" +
                        ")"
        );
    }

    private void repairLeaveRequest() {
        execQuiet(
                "CREATE TABLE IF NOT EXISTS leave_request (" +
                        "id bigserial PRIMARY KEY, " +
                        "employee_id bigint NOT NULL, " +
                        "start_date date NOT NULL, " +
                        "end_date date NOT NULL, " +
                        "type varchar(20) NOT NULL DEFAULT 'PERSONAL', " +
                        "reason varchar(255), " +
                        "status varchar(20) NOT NULL DEFAULT 'PENDING', " +
                        "created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                        "reviewed_at timestamp" +
                        ")"
        );
    }

    private void execQuiet(String sql) {
        try {
            jdbc.execute(sql);
        } catch (Exception e) {
            // H2 本地可能不支持部分语法；忽略并继续
            log.warn("SchemaRepair skip: {} -> {}", sql, e.getMessage());
        }
    }
}
