package com.xyz.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * <p>Package Name: com.xyz.dto </p>
 * <p>Description: 登录成功后返回的视图对象(VO)，包含了认证令牌(JWT)的详细信息以及用户的基本资料。它继承自AuthVo以包含用户的核心信息。</p>
 * <p>Create Time: 2025/9/2 </p>
 *
 * @author <a href="https://github.com/yanzhe-xiao">YANZHE XIAO</a>
 * @version 1.0
 * @since
 */
@Data
@SuperBuilder
@Schema(name = "AuthTokenVO", description = "登录成功后返回的令牌信息")
public class AuthTokenVO extends AuthVo {

    @Schema(description = "访问令牌（JWT）", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "令牌类型，固定为 Bearer", example = "Bearer")
    private String tokenType;

    @Schema(description = "过期秒数（例如：7天=604800）", example = "604800")
    private long expiresIn;

    @Schema(description = "JWT 的唯一标识符 (jti)，可用于登出或吊销令牌", example = "a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8")
    private String jti;

    @Schema(description = "令牌签发时间的时间戳（毫秒）", example = "1678886400000")
    private long issuedAt;

    @Schema(description = "令牌过期时间的时间戳（毫秒）", example = "1679491200000")
    private long expireAt;

    @Schema(description = "用户名", example = "john.doe")
    protected String username;

    @Schema(description = "用户全名或昵称", example = "John Doe")
    protected String fullName;

    @Schema(description = "用户联系电话", example = "13800138000")
    protected String phone;

    @Schema(description = "用户电子邮箱", example = "john.doe@example.com")
    protected String email;

    @Schema(description = "用户角色列表，用于前端进行权限控制", example = "ROLE_ADMIN")
    protected String roles;
}