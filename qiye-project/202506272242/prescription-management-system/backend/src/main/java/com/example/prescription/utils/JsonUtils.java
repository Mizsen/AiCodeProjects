package com.example.prescription.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * JSON 工具类
 * 基于 Jackson 实现，支持 Java 21 特性
 */
public class JsonUtils {
    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        // 配置 ObjectMapper
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // 不序列化 null 值
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // 忽略未知属性
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false); // 日期格式化
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false); // 允许序列化空对象
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")); // 设置日期格式
        objectMapper.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai")); // 设置时区
        objectMapper.registerModule(new JavaTimeModule()); // 注册 Java 8 时间模块
    }

    /**
     * 将对象转换为 JSON 字符串
     *
     * @param obj 要转换的对象
     * @return JSON 字符串
     */
    public static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error("Convert object to JSON string failed", e);
            throw new RuntimeException("Convert object to JSON string failed", e);
        }
    }

    /**
     * 将 JSON 字符串转换为对象
     *
     * @param json  JSON 字符串
     * @param clazz 目标类型
     * @return 转换后的对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            logger.error("Convert JSON string to object failed", e);
            throw new RuntimeException("Convert JSON string to object failed", e);
        }
    }

    /**
     * 将 JSON 字符串转换为复杂类型对象
     *
     * @param json          JSON 字符串
     * @param typeReference 类型引用
     * @return 转换后的对象
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            logger.error("Convert JSON string to complex object failed", e);
            throw new RuntimeException("Convert JSON string to complex object failed", e);
        }
    }

    /**
     * 将对象转换为格式化的 JSON 字符串（美化输出）
     *
     * @param obj 要转换的对象
     * @return 格式化后的 JSON 字符串
     */
    public static String toPrettyJson(Object obj) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error("Convert object to pretty JSON string failed", e);
            throw new RuntimeException("Convert object to pretty JSON string failed", e);
        }
    }

    /**
     * 判断字符串是否为有效的 JSON
     *
     * @param json 要检查的字符串
     * @return 是否为有效的 JSON
     */
    public static boolean isValidJson(String json) {
        try {
            objectMapper.readTree(json);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 获取 ObjectMapper 实例
     * 注意：建议使用工具类提供的方法，而不是直接使用 ObjectMapper
     *
     * @return ObjectMapper 实例
     */
    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }
} 