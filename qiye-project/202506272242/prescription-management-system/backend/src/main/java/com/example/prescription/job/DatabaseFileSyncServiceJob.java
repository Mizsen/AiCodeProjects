package com.example.prescription.job;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.sqlite.jdbc4.JDBC4Connection;
import org.sqlite.jdbc4.JDBC4DatabaseMetaData;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import com.example.prescription.config.DataSourceConfig;


@Slf4j
@Component
public class DatabaseFileSyncServiceJob {

    private final DataSource primaryDataSource;
    private volatile DataSource slaveDataSource;
    private final DataSourceConfig dataSourceConfig;

    @Autowired
    public DatabaseFileSyncServiceJob(@Qualifier("primaryDataSource") DataSource primaryDataSource,
                                      @Qualifier("slaveDataSource") DataSource slaveDataSource,
                                      DataSourceConfig dataSourceConfig) {
        this.primaryDataSource = primaryDataSource;
        this.slaveDataSource = slaveDataSource;
        this.dataSourceConfig = dataSourceConfig;
    }

    private void closeSlaveConnections() {
        if (slaveDataSource instanceof HikariDataSource) {
            ((HikariDataSource) slaveDataSource).close();
            log.info("slaveDataSource 已关闭所有连接。");
        }
    }

    /**
     * 每5分钟同步一次
     */
    // @Scheduled(fixedRate = 1 * 60 * 1000)
    public void syncDatabaseFiles() {
        log.info("定时同步任务开始执行...");
        // closeSlaveConnections(); // 同步前关闭slaveDataSource连接
        try (Connection sourceConn = primaryDataSource.getConnection()) {
            // 1. 检查连接状态（调试用）
            log.info("Auto-commit状态: {}", sourceConn.getAutoCommit());

            // 2. 检查SQLite版本（可选）
            try (Statement checkStmt = sourceConn.createStatement();
                 ResultSet rs = checkStmt.executeQuery("SELECT sqlite_version()")) {
                if (rs.next()) {
                    log.info("SQLite版本: {}", rs.getString(1));
                }
            }

            // // 3. 删除旧备份文件
            Path backupPath = Paths.get("data/prescription_read.db");
            try {
                Files.deleteIfExists(backupPath);
                // 再次确认文件已删除
                if (Files.exists(backupPath)) {
                    log.warn("备份文件仍然存在，跳过本次同步！");
                    return;
                }
            } catch (IOException e) {
                log.info("删除旧备份文件失败（可能文件不存在或无权访问）: {}", e.getMessage());
                if (Files.exists(backupPath)) {
                    log.warn("备份文件仍然存在，跳过本次同步！");
                    return;
                }
            }

            // 4. 执行VACUUM（关键操作）
            sourceConn.setAutoCommit(true); // 确保无事务
            try (Statement stmt = sourceConn.createStatement()) {
                stmt.execute("VACUUM INTO 'file:" + backupPath + "'");
                log.info("数据库同步成功: {}", new Date());
            }
        } catch (SQLException e) {
            log.info("数据库同步失败: {}", e.getMessage(), e);
        }
        // // 同步后重建slaveDataSource
        // this.slaveDataSource = dataSourceConfig.createNewSlaveDataSource();
        // log.info("slaveDataSource 已重新初始化。");
    }

}