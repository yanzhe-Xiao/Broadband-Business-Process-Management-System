package com.xyz.controller;

import com.xyz.common.ResponseResult;
import com.xyz.dto.AuthTokenVO;
import com.xyz.security.JwkKeyProvider;
import com.xyz.service.AuthService;
import com.xyz.service.TokenService;

import com.xyz.user.AppUser;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;

    // ============ 登录 ============
    public record LoginReq(@NotBlank String username, @NotBlank String password) {}

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseResult<AuthTokenVO> login(@RequestBody LoginReq req) throws Exception {
        AppUser user = authService.authenticate(req.username(), req.password());
        List<String> roles = authService.loadUserRoles(user.getId());

        var issued = tokenService.issueToken(user.getUsername(), roles, user.getTenantId());
        var jwtMeta = tokenService.parseMeta(issued.accessToken()); // 读 iat/exp/jti

        AuthTokenVO vo = AuthTokenVO.builder()
                .accessToken(issued.accessToken())
                .tokenType("Bearer")
                .expiresIn(issued.expiresInSeconds())
                .jti(jwtMeta.jti())
                .issuedAt(jwtMeta.issuedAtMs())
                .expireAt(jwtMeta.expireAtMs())
                .username(user.getUsername())
                .roles(roles)
                .build();

        return ResponseResult.success("登录成功", vo);
    }

    // ============ 登出 ============
    public record LogoutReq(@NotBlank String jti) {}

    @PostMapping(value = "/logout", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseResult<Void> logout(@RequestBody LogoutReq req) {
        tokenService.revoke(req.jti());
        return ResponseResult.success("退出成功");
    }

    // ============ JWKS（必须保持原生 JSON，不能用统一包装） ============
    private final JwkKeyProvider keyProvider;

    @GetMapping(value = "/jwks", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> jwks() {
        // 注意：这里返回**裸 JSON**，不能用 ResponseResult 包裹
        return ResponseEntity.ok(keyProvider.jwksJson());
    }
}