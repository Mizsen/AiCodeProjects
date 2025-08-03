package com.example.backend.auth;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CacheRequestBodyFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String method = httpRequest.getMethod();
        
        // 只对POST和PUT请求包装，以便缓存请求体
        if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) {
            CacheRequestBodyHttpServletRequest cachedRequest = new CacheRequestBodyHttpServletRequest(httpRequest);
            chain.doFilter(cachedRequest, response);
        } else {
            chain.doFilter(request, response);
        }
    }
}