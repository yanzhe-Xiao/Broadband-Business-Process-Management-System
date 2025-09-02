// service/TokenService.java
package com.xyz.service;

import java.util.List;

public interface TokenService {

    record Issued(String accessToken, long expiresInSeconds) {}
    record Meta(String jti, long issuedAtMs, long expireAtMs) {}

    Issued issueToken(String username, List<String> roles, String tenantId) throws Exception;

    /** 控制器里在用：parseMeta(jwt).jti()/issuedAtMs()/expireAtMs() */
    Meta parseMeta(String jwt) throws Exception;

    void revoke(String jti);

    boolean isRevoked(String jti);
}
