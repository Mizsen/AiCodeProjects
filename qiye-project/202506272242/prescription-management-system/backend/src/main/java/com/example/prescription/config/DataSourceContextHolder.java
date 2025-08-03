package com.example.prescription.config;

public class DataSourceContextHolder {
    private static final ThreadLocal<DbType> contextHolder = new ThreadLocal<>();

    public static void setDbType(DbType dbType) {
        if (dbType == null) throw new IllegalArgumentException("数据源类型不能为null");
        contextHolder.set(dbType);
    }

    public static DbType getDbType() {
        return contextHolder.get() == null ? DbType.PRIMARY : contextHolder.get();
    }

    public static void clear() {
        contextHolder.remove();
    }

    public enum DbType {
        PRIMARY, SLAVE
    }
}