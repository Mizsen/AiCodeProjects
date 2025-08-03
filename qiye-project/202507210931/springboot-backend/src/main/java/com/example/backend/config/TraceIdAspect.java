package com.example.backend.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * <pre></pre>
 *
 * @author Chen.Mr
 * @date 2025年08月02日 13:21
 **/
// 1. 定义切面
@Aspect
@Component
public class TraceIdAspect {
    // 拦截Controller层所有方法
    @Pointcut("execution(* com.example.backend.controller..*(..))")
    public void controllerPointcut() {
    }

    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            if (MDC.get("traceId") == null) {
                MDC.put("traceId", UUID.randomUUID().toString().replace("-", ""));
            }
            return joinPoint.proceed(); // 执行目标方法
        } finally {
            MDC.remove("traceId");
        }
    }
}
