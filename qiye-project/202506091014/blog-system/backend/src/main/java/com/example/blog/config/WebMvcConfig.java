package com.example.blog.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 首页和静态资源不拦截，其它都拦截
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/", "/index.html", "/static/**",
                        "/public/**",
                        "/api/users/login", "/api/users/register","/api/users/default/avatar",
                        "/api/posts", "/api/posts/*",
                        "/api/comments/post","/api/comments/post/*",
                        "/favicon.ico", "/css/**", "/js/**", "/img/**", "/assets/**");
    }
}
