package com.xyz.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * <p>Package Name: com.xyz.config </p>
 * <p>Description:  </p>
 * <p>Create Time: 2025/9/3 </p>
 *
 * @author <a href="https://github.com/yanzhe-xiao">YANZHE XIAO</a>
 * @version 1.0
 * @since
 */
@Configuration
@EnableMethodSecurity
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain security(HttpSecurity http, JwtAuthenticationConverter jwtConverter) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(reg -> reg
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/images/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter)))
                .build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        var conv = new JwtGrantedAuthoritiesConverter();
        conv.setAuthoritiesClaimName("roles"); // claim名
        conv.setAuthorityPrefix("");            // 你的roles已带ROLE_前缀就不再加
        var jwt = new JwtAuthenticationConverter();
        jwt.setJwtGrantedAuthoritiesConverter(conv);
        return jwt;
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

