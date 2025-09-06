// src/main/java/com/xyz/config/CorsConfig.java
package com.xyz.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration cfg = new CorsConfiguration();

        // 允许的前端来源
        cfg.addAllowedOriginPattern("http://127.0.0.1:3000");
        cfg.addAllowedOriginPattern("http://localhost:3000");

        // 允许携带 cookie/认证头（如果你需要的话）
        cfg.setAllowCredentials(true);

        // 允许的请求头/方法
        cfg.addAllowedHeader("*");
        cfg.addExposedHeader("Authorization");
        cfg.addExposedHeader("Content-Type");
        cfg.addAllowedMethod("*"); // GET/POST/PUT/DELETE/PATCH/OPTIONS…

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return new CorsWebFilter(source);
    }
}
