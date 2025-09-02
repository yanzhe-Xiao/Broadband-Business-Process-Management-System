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

/**
 * <p>Package Name: com.xyz.security </p>
 * <p>Description: JWK (JSON Web Key) 提供者，负责管理用于JWT签名的RSA密钥对。
 * 支持两种模式：
 * 1. 'memory': 在内存中动态生成密钥对，适用于开发和测试环境。
 * 2. 'pem': 从PEM格式的文件加载密钥对，适用于生产环境，保证服务重启后密钥不变。</p>
 * <p>Create Time: 2025/9/2 </p>
 *
 * @author <a href="https://github.com/yanzhe-xiao">YANZHE XIAO</a>
 * @version 1.0
 * @since
 */
@Getter
@Component
public class JwkKeyProvider {

    /**
     * 包含公钥和私钥的完整RSA JWK。
     * 主要在服务内部用于对JWT进行签名。
     */
    private final RSAKey rsaJwk;

    /**
     * 仅包含公钥的JWK集合（JWKSet）。
     * 用于通过/jwks端点对外暴露，供资源服务器或客户端验证JWT签名。
     */
    private final JWKSet jwkSet;

    /**
     * 构造函数，根据配置初始化密钥。
     *
     * @param mode           密钥模式 ('memory' 或 'pem')。
     * @param configuredKid  可选的Key ID。如果未提供，则生成一个随机UUID。
     * @param pemPrivatePath 当 mode='pem' 时，PKCS#8格式的私钥文件路径。
     * @param pemPublicPath  可选的公钥文件路径。如果未提供，将尝试从私钥推导。
     * @throws Exception 如果密钥加载或生成失败。
     */
    public JwkKeyProvider(
            @Value("${auth.keys.mode:memory}") String mode,
            @Value("${auth.keys.kid:}") String configuredKid,
            @Value("${auth.keys.pem.private-path:}") String pemPrivatePath,
            @Value("${auth.keys.pem.public-path:}") String pemPublicPath
    ) throws Exception {
        // 确定Key ID (kid)，优先使用配置，否则生成UUID
        final String kid = (configuredKid != null && !configuredKid.isBlank())
                ? configuredKid
                : UUID.randomUUID().toString();

        RSAKey key;

        // 模式一：从PEM文件加载密钥
        if ("pem".equalsIgnoreCase(mode)) {
            if (pemPrivatePath == null || pemPrivatePath.isBlank()) {
                throw new IllegalStateException("auth.keys.mode=pem 时必须配置 auth.keys.pem.private-path (PKCS#8 PEM)");
            }
            // 加载私钥
            RSAPrivateKey privateKey = loadPkcs8RsaPrivateKey(pemPrivatePath);
            RSAPublicKey publicKey;

            // 如果配置了公钥路径，则直接加载；否则从私钥推导
            if (pemPublicPath != null && !pemPublicPath.isBlank()) {
                publicKey = loadRsaPublicKeyFromPem(pemPublicPath);
            } else {
                // 从私钥推导公钥需要私钥包含CRT（中国剩余定理）参数
                if (!(privateKey instanceof RSAPrivateCrtKey crt)) {
                    throw new IllegalStateException("无法从私钥推导公钥：请提供 auth.keys.pem.public-path 或使用包含 CRT 参数的 RSA 私钥");
                }
                var pubSpec = new RSAPublicKeySpec(crt.getModulus(), crt.getPublicExponent());
                publicKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(pubSpec);
            }

            // 构建Nimbus库的RSAKey对象
            key = new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyUse(KeyUse.SIGNATURE) // 密钥用途：签名
                    .algorithm(JWSAlgorithm.RS256) // 签名算法
                    .keyID(kid)
                    .build();

        } else { // 模式二：在内存中生成密钥（默认）
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

        this.rsaJwk = key; // 内部使用，包含私钥
        this.jwkSet = new JWKSet(key.toPublicJWK()); // 对外暴露，只包含公钥
    }

    /**
     * 获取当前密钥的ID (kid)。
     * @return 密钥ID字符串。
     */
    public String kid() {
        return rsaJwk.getKeyID();
    }

    /**
     * 从JWK对象中提取 {@link RSAPrivateKey}。
     * @return RSAPrivateKey 实例。
     * @throws JOSEException 如果提取失败。
     */
    public RSAPrivateKey privateKey() throws JOSEException {
        return rsaJwk.toRSAPrivateKey();
    }

    /**
     * 从JWK对象中提取 {@link RSAPublicKey}。
     * @return RSAPublicKey 实例。
     * @throws JOSEException 如果提取失败。
     */
    public RSAPublicKey publicKey() throws JOSEException {
        return rsaJwk.toRSAPublicKey();
    }

    /**
     * 返回仅包含公钥的 JWKS 的 JSON 字符串表示形式。
     * 此方法用于/jwks端点，向外界提供验证JWT签名所需的公钥信息。
     * @return JWKS 的 JSON 字符串。
     */
    public String jwksJson() {
        try {
            return JSONObjectUtils.toJSONString(jwkSet.toJSONObject());
        } catch (Exception e) {
            throw new RuntimeException("JWKS 序列化失败", e);
        }
    }

    // ========== 辅助方法：从PEM文件加载密钥 ==========

    /**
     * 从指定路径加载PKCS#8格式的RSA私钥。
     * @param path 私钥文件路径。
     * @return RSAPrivateKey 实例。
     * @throws Exception 如果文件读取或密钥解析失败。
     */
    private static RSAPrivateKey loadPkcs8RsaPrivateKey(String path) throws Exception {
        byte[] pem = Files.readAllBytes(Path.of(path));
        String txt = new String(pem, StandardCharsets.UTF_8);

        // 移除PEM文件的头尾标识和所有空白字符
        String begin = "-----BEGIN PRIVATE KEY-----";
        String end = "-----END PRIVATE KEY-----";
        if (!txt.contains(begin) || !txt.contains(end)) {
            throw new IllegalStateException("仅支持 PKCS#8 私钥 PEM（BEGIN/END PRIVATE KEY）：" + path);
        }
        String base64 = txt.substring(txt.indexOf(begin) + begin.length(), txt.indexOf(end))
                .replaceAll("\\s", "");
        byte[] der = Base64.getDecoder().decode(base64);

        var spec = new PKCS8EncodedKeySpec(der);
        return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    /**
     * 从指定路径加载X.509格式的RSA公钥。
     * @param path 公钥文件路径。
     * @return RSAPublicKey 实例。
     * @throws Exception 如果文件读取或密钥解析失败。
     */
    private static RSAPublicKey loadRsaPublicKeyFromPem(String path) throws Exception {
        byte[] pem = Files.readAllBytes(Path.of(path));
        String txt = new String(pem, StandardCharsets.UTF_8);

        // 移除PEM文件的头尾标识和所有空白字符
        String begin = "-----BEGIN PUBLIC KEY-----";
        String end = "-----END PUBLIC KEY-----";
        if (!txt.contains(begin) || !txt.contains(end)) {
            throw new IllegalStateException("需要 X.509 公钥 PEM（BEGIN/END PUBLIC KEY）：" + path);
        }
        String base64 = txt.substring(txt.indexOf(begin) + begin.length(), txt.indexOf(end))
                .replaceAll("\\s", "");
        byte[] der = Base64.getDecoder().decode(base64);

        var spec = new java.security.spec.X509EncodedKeySpec(der);
        return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(spec);
    }
}