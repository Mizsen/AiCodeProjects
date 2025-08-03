package com.example.prescription.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

@Slf4j
public class RoutingDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        DataSourceContextHolder.DbType type = DataSourceContextHolder.getDbType();
        log.info("当前路由数据源: {}", type);
        return type;
    }}