// security/JwkKeyProvider.java
package com.xyz.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.util.JSONObjectUtils;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.UUID;

@Getter
@Component
public class JwkKeyProvider {

    private final RSAKey rsaJwk;   // 包含私钥的 JWK（仅在服务内使用）
    private final JWKSet jwkSet;   // 仅含公钥的 JWKS（对外暴露）

    public JwkKeyProvider(
            @Value("${auth.keys.mode:memory}") String mode,                    // memory | pem
            @Value("${auth.keys.kid:}") String configuredKid,                 // 可选，手动指定 KID
            @Value("${auth.keys.pem.private-path:}") String pemPrivatePath,   // 当 mode=pem 时必填（PKCS#8）
            @Value("${auth.keys.pem.public-path:}") String pemPublicPath      // 可选，不填则从私钥推导
    ) throws Exception {
        final String kid = (configuredKid != null && !configuredKid.isBlank())
                ? configuredKid
                : UUID.randomUUID().toString();

        RSAKey key;

        if ("pem".equalsIgnoreCase(mode)) {
            if (pemPrivatePath == null || pemPrivatePath.isBlank()) {
                throw new IllegalStateException("auth.keys.mode=pem 时必须配置 auth.keys.pem.private-path (PKCS#8 PEM)");
            }
            RSAPrivateKey privateKey = loadPkcs8RsaPrivateKey(pemPrivatePath);

            RSAPublicKey publicKey;
            if (pemPublicPath != null && !pemPublicPath.isBlank()) {
                publicKey = loadRsaPublicKeyFromPem(pemPublicPath);
            } else {
                // 从私钥推导公钥（需要 PKCS#1 CRT 参数）
                if (!(privateKey instanceof RSAPrivateCrtKey crt)) {
                    throw new IllegalStateException("无法从私钥推导公钥：请提供 auth.keys.pem.public-path 或使用包含 CRT 参数的 RSA 私钥");
                }
                var pubSpec = new RSAPublicKeySpec(crt.getModulus(), crt.getPublicExponent());
                publicKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(pubSpec);
            }

            key = new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyUse(KeyUse.SIGNATURE)
                    .algorithm(JWSAlgorithm.RS256)
                    .keyID(kid)
                    .build();

        } else {
            // 默认：内存生成 2048 位 RSA（适合开发/测试；生产建议用 pem / KMS）
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            KeyPair kp = gen.generateKeyPair();

            key = new RSAKey.Builder((RSAPublicKey) kp.getPublic())
                    .privateKey((RSAPrivateKey) kp.getPrivate())
                    .keyUse(KeyUse.SIGNATURE)
                    .algorithm(JWSAlgorithm.RS256)
                    .keyID(kid)
                    .build();
        }

        this.rsaJwk = key;                           // 内部用，含私钥
        this.jwkSet = new JWKSet(key.toPublicJWK()); // 对外只发公钥
    }

    public String kid() {
        return rsaJwk.getKeyID();
    }

    public RSAPrivateKey privateKey() throws JOSEException {
        return rsaJwk.toRSAPrivateKey();
    }

    public RSAPublicKey publicKey() throws JOSEException {
        return rsaJwk.toRSAPublicKey();
    }

    /** 返回仅含公钥的 JWKS JSON（给网关/资源服务器验证 JWT） */
    public String jwksJson() {
        try {
            return JSONObjectUtils.toJSONString(jwkSet.toJSONObject());
        } catch (Exception e) {
            throw new RuntimeException("JWKS 序列化失败", e);
        }
    }

    // ========== 辅助：加载 PEM ==========
    private static RSAPrivateKey loadPkcs8RsaPrivateKey(String path) throws Exception {
        byte[] pem = Files.readAllBytes(Path.of(path));
        String txt = new String(pem, StandardCharsets.UTF_8);

        // 仅支持 PKCS#8（-----BEGIN PRIVATE KEY-----）
        String begin = "-----BEGIN PRIVATE KEY-----";
        String end = "-----END PRIVATE KEY-----";
        int s = txt.indexOf(begin);
        int e = txt.indexOf(end);
        if (s < 0 || e < 0) {
            throw new IllegalStateException("仅支持 PKCS#8 私钥 PEM（BEGIN/END PRIVATE KEY）：" + path);
        }
        String base64 = txt.substring(s + begin.length(), e).replaceAll("\\s", "");
        byte[] der = Base64.getDecoder().decode(base64);

        var spec = new PKCS8EncodedKeySpec(der);
        return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    private static RSAPublicKey loadRsaPublicKeyFromPem(String path) throws Exception {
        // 支持 -----BEGIN PUBLIC KEY-----（X.509 SubjectPublicKeyInfo）
        byte[] pem = Files.readAllBytes(Path.of(path));
        String txt = new String(pem, StandardCharsets.UTF_8);

        String begin = "-----BEGIN PUBLIC KEY-----";
        String end = "-----END PUBLIC KEY-----";
        int s = txt.indexOf(begin);
        int e = txt.indexOf(end);
        if (s < 0 || e < 0) {
            throw new IllegalStateException("需要 X.509 公钥 PEM（BEGIN/END PUBLIC KEY）：" + path);
        }
        String base64 = txt.substring(s + begin.length(), e).replaceAll("\\s", "");
        byte[] der = Base64.getDecoder().decode(base64);

        var spec = new java.security.spec.X509EncodedKeySpec(der);
        return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(spec);
    }
}
