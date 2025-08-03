package com.example.prescription.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  private final Path uploadPath = Paths.get("upload")
    .toAbsolutePath()
    .normalize();

  /**
   * 配置跨域请求
   * @param registry CorsRegistry
   */

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
      .addMapping("/api/**")
      .allowedOriginPatterns("*")
      .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
      .allowedHeaders("*")
      .allowCredentials(true);
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // 映射规则：/upload/** -> 指向 upload 文件夹的绝对路径
    registry
      .addResourceHandler("/upload/**")
      .addResourceLocations("file:" + uploadPath + "/");
  }

  @Bean
  public WebMvcConfigurer webMvcConfigurer() {
    return new WebMvcConfigurer() {
      @Override
      public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("forward:/index.html");
        registry
          .addViewController("/register")
          .setViewName("forward:/index.html");
        registry
          .addViewController("/admin/**")
          .setViewName("forward:/index.html");
      }
    };
  }
}
