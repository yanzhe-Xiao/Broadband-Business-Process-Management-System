package com.xyz.service.impl;

import com.xyz.security.JwkKeyProvider;
import com.xyz.service.TokenService;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * <p>Package Name: com.xyz.service.impl </p>
 * <p>Description:  </p>
 * <p>Create Time: 2025/9/2 </p>
 *
 * @author <a href="https://github.com/yanzhe-xiao">YANZHE XIAO</a>
 * @version 1.0
 * @since
 */
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final JwkKeyProvider keys;
    private final StringRedisTemplate redis;

    @Value("${auth.jwt.ttl-seconds:604800}") // 默认7天
    private long ttlSeconds;

    private static String sessionKey(String jti)  { return "auth:jwt:sessions:" + jti; }
    private static String blacklistKey(String jti){ return "auth:jwt:blacklist:" + jti; }

    @Override
    public Issued issueToken(String username, List<String> roles, String tenantId) throws Exception {
        long now = Instant.now().getEpochSecond();
        String jti = UUID.randomUUID().toString();

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(username)
                .issueTime(Date.from(Instant.ofEpochSecond(now)))
                .expirationTime(Date.from(Instant.ofEpochSecond(now + ttlSeconds)))
                .jwtID(jti)
                .claim("roles", roles)
                .claim("tenantId", tenantId)
                .build();

        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(keys.getRsaJwk().getKeyID())
                .type(JOSEObjectType.JWT)
                .build();

        SignedJWT jwt = new SignedJWT(header, claims);
        jwt.sign(new RSASSASigner(keys.getRsaJwk().toPrivateKey()));
        String token = jwt.serialize();

        // 会话写入 Redis（7天 TTL）
        String sessionVal = "{\"sub\":\"" + username + "\",\"roles\":\"" + String.join(",", roles) + "\"}";
        redis.opsForValue().set(sessionKey(jti), sessionVal, ttlSeconds, TimeUnit.SECONDS);

        return new Issued(token, ttlSeconds);
    }

    @Override
    public Meta parseMeta(String jwtStr) throws Exception {
        SignedJWT jwt = SignedJWT.parse(jwtStr);
        JWTClaimsSet c = jwt.getJWTClaimsSet();
        String jti = c.getJWTID();
        long iat = Optional.ofNullable(c.getIssueTime()).map(Date::getTime).orElse(0L);
        long exp = Optional.ofNullable(c.getExpirationTime()).map(Date::getTime).orElse(0L);
        return new Meta(jti, iat, exp);
    }

    @Override
    public void revoke(String jti) {
        // 加入黑名单，TTL 设到过期为止；这里取会话剩余 TTL 复用
        Long remain = redis.getExpire(sessionKey(jti), TimeUnit.SECONDS);
        long ttl = (remain == null || remain < 0) ? ttlSeconds : remain;
        redis.opsForValue().set(blacklistKey(jti), "1", ttl, TimeUnit.SECONDS);
        // 也可以顺便删掉 sessionKey
        redis.delete(sessionKey(jti));
    }

    @Override
    public boolean isRevoked(String jti) {
        return Boolean.TRUE.equals(redis.hasKey(blacklistKey(jti)));
    }
}
