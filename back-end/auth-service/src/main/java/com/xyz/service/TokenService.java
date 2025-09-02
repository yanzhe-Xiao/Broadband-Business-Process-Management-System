package com.xyz.service;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.xyz.security.JwkKeyProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final JwkKeyProvider keys;
    private final StringRedisTemplate redis;

    public record TokenPair(String accessToken, long expiresInSeconds, String jti) {}

    public TokenPair issueToken(String subject, List<String> roles, String tenantId) throws Exception {
        long now = System.currentTimeMillis();
        long ttlSec = 7L * 24 * 3600; // 7 天
        Date iat = new Date(now);
        Date exp = new Date(now + ttlSec * 1000);
        String jti = UUID.randomUUID().toString();

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
            .subject(subject)
            .issuer("auth-service")
            .issueTime(iat)
            .expirationTime(exp)
            .jwtID(jti)
            .claim("roles", roles)        // e.g. ["ROLE_ADMIN","ROLE_TECH"]
            .claim("tenantId", tenantId)  // 可选
            .claim("typ", "access")
            .build();

        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
            .keyID(keys.getRsaJwk().getKeyID())
            .type(JOSEObjectType.JWT)
            .build();

        SignedJWT jwt = new SignedJWT(header, claims);
        JWSSigner signer = new RSASSASigner(keys.getRsaJwk().toPrivateKey());
        jwt.sign(signer);

        // 写 Redis 白名单（或会话）
        String key = "auth:session:" + jti;
        String val = "{\"sub\":\"" + subject + "\",\"roles\":" + roles + "}";
        redis.opsForValue().set(key, val, Duration.ofSeconds(ttlSec));

        return new TokenPair(jwt.serialize(), ttlSec, jti);
    }

    public void revoke(String jti) {
        redis.delete("auth:session:" + jti);
        // 可选：redis.opsForValue().set("auth:jwt:blacklist:"+jti,"1", Duration.ofDays(30));
    }

    public record JwtMeta(String jti, long issuedAtMs, long expireAtMs) {}

    public JwtMeta parseMeta(String jwt) throws Exception {
        var signed = SignedJWT.parse(jwt);
        var cs = signed.getJWTClaimsSet();
        String jti = cs.getJWTID();
        long iat = cs.getIssueTime() != null ? cs.getIssueTime().getTime() : 0L;
        long exp = cs.getExpirationTime() != null ? cs.getExpirationTime().getTime() : 0L;
        return new JwtMeta(jti, iat, exp);
    }
}