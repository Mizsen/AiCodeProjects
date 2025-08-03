package com.example.prescription.aop;

import com.example.prescription.config.DataSourceContextHolder;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(-1)// 确保先于@Transactional执行
public class DataSourceSwitchAspect {
    private static final Logger logger = LoggerFactory.getLogger(DataSourceSwitchAspect.class);
    // 读操作（slave）
    @Around("execution(* com.example.prescription.service.*.get*(..)) || execution(* com.example.prescription.service.*.find*(..)) || execution(* com.example.prescription.service.*.list*(..)) || execution(* com.example.prescription.service.*.select*(..))")
    public Object setReadDataSource(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取方法名称
        String methodName = joinPoint.getSignature().getName();
        // 获取类名
        String className = joinPoint.getTarget().getClass().getSimpleName();

        logger.info("切换到从库执行查询 - 类: {}, 方法: {}", className, methodName);


        try {
            // DataSourceContextHolder.setDbType(DataSourceContextHolder.DbType.SLAVE);
            logger.info("已切换到从库 - {}.{}", className, methodName);
            Object result = joinPoint.proceed();
            logger.info("从库查询完成 - 类: {}, 方法: {}", className, methodName);
            return result;
        } catch (Throwable e) {
            logger.error("从库查询异常 - 类: {}, 方法: {}", className, methodName, e);
            throw e;
        } finally {
            DataSourceContextHolder.clear();
            logger.debug("恢复默认数据源");
        }
    }

    // 写操作（primary）
    @Around("execution(* com.example.prescription.service.*.save*(..)) || execution(* com.example.prescription.service.*.add*(..)) || execution(* com.example.prescription.service.*.update*(..)) || execution(* com.example.prescription.service.*.delete*(..))")
    public Object setWriteDataSource(ProceedingJoinPoint joinPoint) throws Throwable {
        DataSourceContextHolder.setDbType(DataSourceContextHolder.DbType.PRIMARY);
        try {
            return joinPoint.proceed();
        } finally {
            DataSourceContextHolder.clear();
        }
    }
}