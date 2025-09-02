package com.xyz.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * <p>Package Name: com.xyz.config </p>
 * <p>Description: Spring Security 配置类，用于定义安全过滤链和密码编码器。</p>
 * <p>Create Time: 2025/9/2 </p>
 *
 * @author <a href="https://github.com/yanzhe-xiao">YANZHE XIAO</a>
 * @version 1.0
 * @since
 */
@Configuration
public class SecurityConfig {

    /**
     * 配置安全过滤链，定义HTTP请求的安全规则。
     *
     * @param http HttpSecurity对象，用于构建安全配置。
     * @return 返回构建好的 SecurityFilterChain 实例。
     * @throws Exception 如果配置过程中发生错误。
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // 禁用CSRF（跨站请求伪造）保护，通常在无状态API（如RESTful API）中使用。
                .csrf(AbstractHttpConfigurer::disable)
                // 配置请求授权规则。
                .authorizeHttpRequests(reg -> reg
                        // 对 "/auth/**" 和 "/actuator/health" 路径的请求允许所有用户访问。
                        .requestMatchers("/auth/**", "/actuator/health").permitAll()
                        // 除了上述明确允许的路径外，所有其他请求都必须经过身份验证。
                        .anyRequest().authenticated()
                )
                // 启用HTTP Basic认证，这是一种简单的基于用户名和密码的认证方式。
                // 通常用于开发和调试，生产环境中可能会使用更安全的认证机制（如JWT）。
                .httpBasic(Customizer.withDefaults())
                // 构建并返回配置好的SecurityFilterChain。
                .build();
    }

    /**
     * 创建并注册一个密码编码器（PasswordEncoder）的Bean。
     * Spring Security要求密码必须以加密形式存储。
     *
     * @return 返回一个支持多种加密算法的委派密码编码器实例。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 使用 PasswordEncoderFactories.createDelegatingPasswordEncoder() 创建一个委派密码编码器。
        // 这种编码器支持多种密码哈希算法，并通过前缀（如"{bcrypt}"）来识别使用哪种算法。
        // 这是目前推荐的最佳实践。
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}