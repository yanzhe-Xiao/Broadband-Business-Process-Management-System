package com.xyz.controller;

import com.xyz.common.ResponseResult;
import com.xyz.dto.AuthTokenVO;
import com.xyz.dto.AuthVo;
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

/**
 * <p>Package Name: com.xyz.controller </p>
 * <p>Description: 认证控制器，处理用户登录、注册、登出、身份验证等相关请求。</p>
 * <p>Create Time: 2025/9/2 </p>
 *
 * @author <a href="https://github.com/yanzhe-xiao">YANZHE XIAO</a>
 * @version 1.0
 * @since
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;
    private final JwkKeyProvider keyProvider; // 仅用于 /jwks 端点

    /**
     * 登录请求的数据传输对象 (DTO)。
     * 使用 Java 16+ 的 record 类型，简洁地定义一个不可变的数据载体。
     */
    public record LoginReq(@NotBlank String username, @NotBlank String password) {}

    /**
     * 处理用户登录请求。
     *
     * @param req 包含用户名和密码的登录请求体。
     * @return 包含JWT访问令牌及用户信息的响应结果。
     * @throws Exception 如果认证失败。
     */
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseResult<AuthTokenVO> login(@RequestBody @Valid LoginReq req) throws Exception {
        // 1. 调用认证服务，验证用户名和密码
        AppUser user = authService.authenticate(req.username(), req.password());
        // 2. 加载用户角色
        String roleName = authService.loadUserRoles(user.getId());

        // 3. 签发JWT令牌
        var issued = tokenService.issueToken(user.getUsername(), roleName, user.getTenantId());
        // 4. 解析令牌元数据（如JTI、过期时间等）
        var meta = tokenService.parseMeta(issued.accessToken());

        // 5. 构建并返回成功的响应
        return ResponseResult.success("登录成功", AuthTokenVO.builder()
                .accessToken(issued.accessToken())
                .tokenType("Bearer")
                .expiresIn(issued.expiresInSeconds())
                .jti(meta.jti())
                .issuedAt(meta.issuedAtMs())
                .expireAt(meta.expireAtMs())
                .username(user.getUsername())
                .roles(roleName)
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .build());
    }

    /**
     * 注册请求的数据传输对象 (DTO)。
     * 包含了创建新用户所需的所有信息。
     */
    public record RegisterReq(
            @NotBlank @Size(min = 3, max = 64) String username,
            @NotBlank @Size(min = 6, max = 128) String password,
            @NotBlank @Size(max = 128) String fullName,
            @Size(max = 32) String phone,
            @Email @Size(max = 128) String email,
            @Size(max = 64) String tenantId, // 可选
            @NotBlank String roleName
    ) {}

    /**
     * 处理用户注册请求，并直接登录。
     * 流程：创建用户 -> 分配角色 -> 签发JWT返回。
     * 如果希望注册后不自动登录，可以移除签发token的逻辑。
     *
     * @param req 包含新用户信息的注册请求体。
     * @return 包含JWT访问令牌及用户信息的响应结果。
     * @throws Exception 如果注册过程中发生错误（例如用户名已存在）。
     */
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseResult<AuthTokenVO> register(@RequestBody @Valid RegisterReq req) throws Exception {
        // 1. 调用认证服务进行注册，包括用户名查重、密码加密和数据持久化
        AppUser user = authService.register(req.username(), req.password(), req.fullName(),
                req.phone(), req.email(),req.roleName);

        // 2. 直接为新注册的用户签发JWT，实现注册即登录的功能
        String roles = authService.loadUserRoles(user.getId());
        var issued = tokenService.issueToken(user.getUsername(), roles, user.getTenantId());
        var meta = tokenService.parseMeta(issued.accessToken());

        // 3. 构建并返回成功的响应
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

    /**
     * 登出请求的数据传输对象 (DTO)。
     */
    public record LogoutReq(@NotBlank String jti) {}

    /**
     * 处理用户登出请求。
     *
     * @param req 包含要注销的JWT的唯一标识符（jti）。
     * @return 操作成功的响应结果。
     */
    @PostMapping(value = "/logout", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseResult<Void> logout(@RequestBody @Valid LogoutReq req) {
        // 调用令牌服务，将该jti加入黑名单（例如存储在Redis中并设置与令牌相同的过期时间）
        tokenService.revoke(req.jti());
        return ResponseResult.success("退出成功");
    }

    /**
     * 通过令牌获取当前登录用户信息。
     *
     * @param authorizationHeader HTTP请求头中的 "Authorization" 字段。
     * @return 包含当前用户基本信息的响应结果。
     * @throws Exception 如果令牌无效或解析失败。
     */
    @PostMapping(value = "/me")
    public ResponseResult<AuthVo> loginByToken(
            @RequestHeader("Authorization") String authorizationHeader) throws Exception {
        // 1. 校验并提取 "Bearer " 前缀后的 token 字符串
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseResult.fail("缺少 Authorization Bearer token");
        }
        String token = authorizationHeader.substring(7);

        // 2. 解码并校验JWT的签名、过期时间等
        var meta = tokenService.parseMeta(token);
        String jti = meta.jti();

        // 3. 检查令牌是否已被吊销（是否存在于黑名单中）
        if (tokenService.isRevoked(meta.jti())) {
            return ResponseResult.fail("token 已失效或被吊销");
        }

        // 4. 根据jti从持久化存储（如Redis）中反查用户信息和角色
        AppUser user = tokenService.parseJtiToUser(jti);
        String role = tokenService.parseJtiToRole(jti);

        // 5. 返回用户信息，结构与登录时类似，但不重新签发token
        return ResponseResult.success("已登录", AuthVo.builder()
                .username(user.getUsername())
                .roles(role)
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .build());
    }

    /**
     * 公开 JWKS (JSON Web Key Set) 端点。
     * JWKS 包含了用于验证JWT签名的公钥。资源服务器或其他服务可以从此端点获取公钥。
     *
     * @return 包含 JWK Set 的裸JSON字符串。
     */
    @GetMapping(value = "/jwks/wrapper", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> jwks() {
        return ResponseEntity.ok(keyProvider.jwksJson());
    }
}