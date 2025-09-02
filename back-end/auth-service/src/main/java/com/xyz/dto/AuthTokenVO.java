package com.xyz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(name = "AuthTokenVO", description = "登录成功后返回的令牌信息")
public class AuthTokenVO {
    @Schema(description = "访问令牌（JWT）")
    private String accessToken;

    @Schema(description = "令牌类型，固定为 Bearer")
    private String tokenType;

    @Schema(description = "过期秒数（7天=604800）")
    private long expiresIn;

    @Schema(description = "JWT 的 jti，用于登出/踢下线等")
    private String jti;

    @Schema(description = "签发时间戳（ms）")
    private long issuedAt;

    @Schema(description = "过期时间戳（ms）")
    private long expireAt;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "全名")
    private String fullName;

    @Schema(description = "电话")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "角色（鉴权用）")
    private List<String> roles;
}