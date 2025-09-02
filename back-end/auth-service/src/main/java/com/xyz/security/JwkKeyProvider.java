// security/JwkKeyProvider.java
package com.xyz.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.*;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

@Component
public class JwkKeyProvider {
    @Getter
    private final RSAKey rsaJwk;
    @Getter
    private final JWKSet jwkSet;

    public JwkKeyProvider(
        @Value("${auth.keys.load-from:memory}") String mode,
        @Value("${auth.keys.pkcs8-pem-path:}") String pemPath
    ) throws Exception {
        RSAKey key;
        if ("file".equalsIgnoreCase(mode) && pemPath != null && !pemPath.isBlank()) {
            // 生产建议：从文件/Secret/KMS加载；这里留钩子（略去解析 PEM 细节）
            throw new IllegalStateException("请实现从文件加载RSA私钥（PKCS#8 PEM）或切换为内存生成");
        } else {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            KeyPair kp = gen.generateKeyPair();
            String kid = UUID.randomUUID().toString();
            key = new RSAKey.Builder((RSAPublicKey) kp.getPublic())
                    .privateKey((RSAPrivateKey) kp.getPrivate())
                    .algorithm(JWSAlgorithm.RS256)
                    .keyUse(KeyUse.SIGNATURE)
                    .keyID(kid)
                    .build();
        }
        this.rsaJwk = key;
        this.jwkSet = new JWKSet(rsaJwk.toPublicJWK());
    }

    public String jwksJson() { return jwkSet.toJSONObject().toString(); }
}
