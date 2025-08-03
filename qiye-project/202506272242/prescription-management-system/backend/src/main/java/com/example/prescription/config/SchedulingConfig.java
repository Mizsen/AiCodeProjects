package com.example.prescription.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling  // 启用定时任务
public class SchedulingConfig {
    // 可在此添加定时任务相关配置（如自定义线程池）
}