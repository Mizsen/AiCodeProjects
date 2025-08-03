package com.example.prescription.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.example.prescription.mapper")  // 仅扫描Mapper接口
public class MyBatisConfig {
    // 可在此添加其他MyBatis相关配置（如分页插件、类型处理器等）

}
