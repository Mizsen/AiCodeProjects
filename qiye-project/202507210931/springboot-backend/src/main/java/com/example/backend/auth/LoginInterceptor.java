package com.example.backend.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Enumeration;
import java.io.BufferedReader;
import java.io.IOException;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(LoginInterceptor.class);
    
    @Autowired
    private TokenManager tokenManager;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 记录请求信息
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        String queryString = request.getQueryString();
        String remoteAddr = request.getRemoteAddr();
        
        // 记录请求基本信息
        logger.info("Request Info - URI: {}, Method: {}, QueryString: {}, RemoteAddr: {}", 
                   requestURI, method, queryString, remoteAddr);
        
        // 记录请求头信息
        Enumeration<String> headerNames = request.getHeaderNames();
        StringBuilder headersInfo = new StringBuilder();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            headersInfo.append(headerName).append(": ").append(headerValue).append("; ");
        }
        logger.info("Request Headers: {}", headersInfo.toString());

        // 记录POST/PUT请求体信息
        if (("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) 
            && request instanceof CacheRequestBodyHttpServletRequest) {
            CacheRequestBodyHttpServletRequest cachedRequest = (CacheRequestBodyHttpServletRequest) request;
            String body = cachedRequest.getBody();
            logger.info("Request Body: {}", body);
        }

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method m = handlerMethod.getMethod();
        boolean requireLogin = m.isAnnotationPresent(RequireLogin.class) || handlerMethod.getBeanType().isAnnotationPresent(RequireLogin.class);
        if (!requireLogin) {
            return true;
        }
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty() || tokenManager.getUsername(token) == null) {
            logger.warn("Unauthorized access to {} from {}", requestURI, remoteAddr);
            response.setStatus(401);
            response.getWriter().write("Unauthorized");
            return false;
        }
        logger.info("Authorized access to {} from {} with token", requestURI, remoteAddr);
        return true;
    }
    
}