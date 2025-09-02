package com.xyz.controller;

import com.xyz.common.ResponseResult;
import com.xyz.dto.AuthTokenVO;
import com.xyz.security.JwkKeyProvider;
import com.xyz.service.AuthService;
import com.xyz.service.TokenService;

import com.xyz.user.AppUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final JwkKeyProvider keyProvider; // 仅用于 /jwks

    // ====== 登录（不变）======
    public record LoginReq(@NotBlank String username, @NotBlank String password) {}

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseResult<AuthTokenVO> login(@RequestBody @Valid LoginReq req) throws Exception {
        AppUser user = authService.authenticate(req.username(), req.password());
        List<String> roles = authService.loadUserRoles(user.getId());

        var issued = tokenService.issueToken(user.getUsername(), roles, user.getTenantId());
        var meta = tokenService.parseMeta(issued.accessToken());

        return ResponseResult.success("登录成功", AuthTokenVO.builder()
                .accessToken(issued.accessToken())
                .tokenType("Bearer")
                .expiresIn(issued.expiresInSeconds())
                .jti(meta.jti())
                .issuedAt(meta.issuedAtMs())
                .expireAt(meta.expireAtMs())
                .username(user.getUsername())
                .roles(roles)
                .fullName(user.getFullName())   // 你提的字段
                .phone(user.getPhone())
                .email(user.getEmail())
                .build());
    }

    // ====== 注册（新增）======
    public record RegisterReq(
            @NotBlank @Size(min = 3, max = 64) String username,
            @NotBlank @Size(min = 6, max = 128) String password,
            @NotBlank @Size(max = 128) String fullName,
            @Size(max = 32) String phone,
            @Email @Size(max = 128) String email,
            @Size(max = 64) String tenantId // 可选
    ) {}

    /**
     * 注册即登录：创建用户 -> 赋默认角色 ROLE_CUSTOMER -> 直接签发JWT返回。
     * 如果你想“注册成功但不自动登录”，把签发token那段去掉即可。
     */
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseResult<AuthTokenVO> register(@RequestBody @Valid RegisterReq req) throws Exception {
        // 1. 注册：去重用户名、BCrypt加密、持久化
        AppUser user = authService.register(req.username(), req.password(), req.fullName(),
                req.phone(), req.email(), req.tenantId());

        // 2. 赋默认角色（如果在 register 内已做，这里可以省略）
        authService.ensureRole(user.getId(), "ROLE_CUSTOMER");

        // 3. 直接签发JWT（省一次登录）
        List<String> roles = authService.loadUserRoles(user.getId());
        var issued = tokenService.issueToken(user.getUsername(), roles, user.getTenantId());
        var meta = tokenService.parseMeta(issued.accessToken());

        return ResponseResult.success("注册成功", AuthTokenVO.builder()
                .accessToken(issued.accessToken())
                .tokenType("Bearer")
                .expiresIn(issued.expiresInSeconds())
                .jti(meta.jti())
                .issuedAt(meta.issuedAtMs())
                .expireAt(meta.expireAtMs())
                .username(user.getUsername())
                .roles(roles)
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .build());
    }

    // ====== 登出（不变）======
    public record LogoutReq(@NotBlank String jti) {}
    @PostMapping(value = "/logout", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseResult<Void> logout(@RequestBody @Valid LogoutReq req) {
        tokenService.revoke(req.jti()); // 加入 Redis 黑名单等
        return ResponseResult.success("退出成功");
    }

    // ====== JWKS（不变，返回裸JSON）======
    @GetMapping(value = "/jwks", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> jwks() {
        return ResponseEntity.ok(keyProvider.jwksJson());
    }
}