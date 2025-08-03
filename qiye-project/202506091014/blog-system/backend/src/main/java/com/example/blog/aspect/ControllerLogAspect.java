package com.example.blog.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.http.HttpServletRequest;

import java.io.*;
import java.util.Arrays;
import java.util.Enumeration;

@Aspect
@Component
public class ControllerLogAspect {

    private static final Logger logger = LoggerFactory.getLogger(ControllerLogAspect.class);

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void controllerMethods() {}

    @Before("controllerMethods()")
    public void logRequest(JoinPoint joinPoint) throws Exception {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) return;

        HttpServletRequest request = attributes.getRequest();
        StringBuffer url = request.getRequestURL();
        String queryString = request.getQueryString();

        // 打印完整URL（含GET参数）
        logger.info("[{}]Request URL: {}?{}", request.getMethod(),url, queryString != null ? queryString : "");

        // 打印所有参数（GET/POST通用）
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String name = paramNames.nextElement();
            String[] values = request.getParameterValues(name);
            logger.info("Param: {} = {}", name, Arrays.toString(values));
        }

        // 打印POST的JSON body（需配合过滤器）
        if ("POST".equalsIgnoreCase(request.getMethod())
                && request.getContentType() != null
                && request.getContentType().contains("application/json")) {
            logger.warn("JSON body需通过ContentCachingRequestWrapper读取");

            if (request instanceof ContentCachingRequestWrapper wrapper) {
                String body = new String(wrapper.getContentAsByteArray(),wrapper.getCharacterEncoding());

                logger.info("JSON Body: {}", body);
            } else {
                try (InputStream is = request.getInputStream();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(is))
                ) {
                        StringBuilder body = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            body.append(line);
                        }
                        logger.info("RAW JSON Body: {}", body);
                }
            }
        }

        Object[] args = joinPoint.getArgs();
        logger.info("Request args: {}", Arrays.toString(args));
    }

    @AfterReturning(pointcut = "controllerMethods()", returning = "result")
    public void logResponse(JoinPoint joinPoint, Object result) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            logger.info("Response for [{} {}] Response body: {}", request.getMethod(), request.getRequestURI(), result);
        }
    }
}
