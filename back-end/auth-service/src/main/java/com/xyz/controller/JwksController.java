package com.xyz.controller;

import com.xyz.security.JwkKeyProvider;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>Package Name: com.xyz.controller </p>
 * <p>Description: 提供公钥集合(JWKS)的端点控制器。JWKS (JSON Web Key Set) 是一个公开的端点，用于让客户端或资源服务器获取验证JWT签名所需的公钥。</p>
 * <p>Create Time: 2025/9/2 </p>
 *
 * @author <a href="https://github.com/yanzhe-xiao">YANZHE XIAO</a>
 * @version 1.0
 * @since
 */
@RestController
@RequestMapping("/auth")
public class JwksController {

    private final JwkKeyProvider keys;

    /**
     * JwksController的构造函数。
     * 通过依赖注入的方式获取JwkKeyProvider实例。
     *
     * @param keys JwkKeyProvider的实例，用于生成和提供JWKS。
     */
    public JwksController(JwkKeyProvider keys) {
        this.keys = keys;
    }

    /**
     * 处理对 "/auth/jwks" 的GET请求，返回JWKS。
     * 这是一个公开的、无需认证的端点，用于发布验证JWT签名所需的公钥。
     *
     * @return 包含JWKS的JSON字符串的ResponseEntity。
     */
    @GetMapping(value = "/jwks", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> jwks() {
        return ResponseEntity.ok(keys.jwksJson());
    }
}