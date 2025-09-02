package com.xyz;

import com.github.tomakehurst.wiremock.jetty12.Jetty12HttpServerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jwt.*;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Instant;
import java.util.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.web-application-type=reactive",
                // 彻底关掉 Servlet 的自动配置，防止有人把 MVC 带进来
                "spring.autoconfigure.exclude=" +
                        "org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.websocket.servlet.WebSocketServletAutoConfiguration"
        }
)@ActiveProfiles("test")
class ApiGatewayAuthFlowTests {

    // 1) 启动 WireMock（动态端口）
    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig()
                    .dynamicPort()
                    .httpServerFactory(new Jetty12HttpServerFactory()))
            .build();

    // 2) 在上下文创建前，把端口注入 spring 配置占位符
    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("wiremock.port", () -> wm.getPort());
        // 关键：把资源服务器的 JWKS 指向 WireMock
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
                () -> "http://localhost:" + wm.getPort() + "/auth/jwks");
        // 注意：这里不要调用 prepareStubs()
    }
    @BeforeEach
    void beforeEach() throws Exception {
        prepareStubs(); // 每个用例前重新注册桩，避免被 reset 掉
    }

    // 3) 生成 RSA、布置桩
    static RSAKey rsaJwk;
    static String kid;

    static void prepareStubs() throws Exception {
        if (rsaJwk == null) {
            // 首次生成 RSA/JWKS
            var gen = java.security.KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            var kp = gen.generateKeyPair();
            kid = java.util.UUID.randomUUID().toString();
            rsaJwk = new com.nimbusds.jose.jwk.RSAKey.Builder(
                    (java.security.interfaces.RSAPublicKey) kp.getPublic())
                    .privateKey((java.security.interfaces.RSAPrivateKey) kp.getPrivate())
                    .keyUse(com.nimbusds.jose.jwk.KeyUse.SIGNATURE)
                    .algorithm(com.nimbusds.jose.JWSAlgorithm.RS256)
                    .keyID(kid).build();
        }

        var jwks = new com.nimbusds.jose.jwk.JWKSet(rsaJwk.toPublicJWK()).toJSONObject();

        // 先清一次，保证幂等
        wm.resetAll();

        wm.stubFor(get(urlEqualTo("/auth/jwks"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(com.nimbusds.jose.util.JSONObjectUtils.toJSONString(jwks))));

        wm.stubFor(post(urlEqualTo("/auth/login"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"code\":200,\"status\":true,\"message\":\"登录成功\",\"data\":{\"accessToken\":\"dummy\",\"tokenType\":\"Bearer\",\"expiresIn\":604800,\"jti\":\"jti-123\"}}")));

        wm.stubFor(get(urlEqualTo("/api/orders/1"))
                .withHeader("Authorization", matching("Bearer .*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":1,\"status\":\"DEMO\"}")));
    }

    @Autowired
    WebTestClient web;

    // —— 工具：生成与 /auth/jwks 匹配的 RS256 JWT ——
    private String signJwt(String sub, List<String> roles, long ttlSeconds) throws Exception {
        long now = Instant.now().getEpochSecond();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(sub)
                .issueTime(Date.from(Instant.ofEpochSecond(now)))
                .expirationTime(Date.from(Instant.ofEpochSecond(now + ttlSeconds)))
                .jwtID(UUID.randomUUID().toString())
                .claim("roles", roles)
                .build();

        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(kid).type(JOSEObjectType.JWT).build();

        SignedJWT jwt = new SignedJWT(header, claims);
        jwt.sign(new RSASSASigner(rsaJwk.toPrivateKey()));
        return jwt.serialize();
    }

    // ================== 用例 ==================

    @Test
    void login_should_be_forwarded() {
        web.post().uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"username\":\"admin\",\"password\":\"123456\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.code").isEqualTo(200)
                .jsonPath("$.message").isEqualTo("登录成功");
    }

    @Test
    void api_without_token_should_401() {
        web.get().uri("/api/orders/1")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void api_with_valid_token_should_pass() throws Exception {
        String jwt = signJwt("admin", List.of("ROLE_ADMIN"), 600);
        web.get().uri("/api/orders/1")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.status").isEqualTo("DEMO");
    }

    @Test
    void api_with_bad_kid_should_401() throws Exception {
        // kid 不匹配：资源服务器从 JWKS 找不到公钥 -> 401
        String bad = tamperedKidToken();
        web.get().uri("/api/orders/1")
                .header("Authorization", "Bearer " + bad)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    private String tamperedKidToken() throws Exception {
        long now = Instant.now().getEpochSecond();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("admin")
                .issueTime(Date.from(Instant.ofEpochSecond(now)))
                .expirationTime(Date.from(Instant.ofEpochSecond(now + 600)))
                .jwtID(UUID.randomUUID().toString())
                .claim("roles", List.of("ROLE_ADMIN"))
                .build();

        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID("not-exists-kid")
                .type(JOSEObjectType.JWT)
                .build();

        SignedJWT jwt = new SignedJWT(header, claims);
        jwt.sign(new RSASSASigner(rsaJwk.toPrivateKey()));
        return jwt.serialize();
    }
}
