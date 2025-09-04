package com.xyz.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * <p>Package Name: com.xyz.dto </p>
 * <p>Description: 认证成功的用户视图对象(VO)基类，包含了用户的核心基本信息。也用于通过令牌获取用户信息接口的返回体。</p>
 * <p>Create Time: 2025/9/2 </p>
 *
 * @author <a href="https://github.com/yanzhe-xiao">YANZHE XIAO</a>
 * @version 1.0
 * @since
 */
@Data
@SuperBuilder
@Schema(name = "AuthVo", description = "认证用户信息")
public class AuthVo {

    @Schema(description = "用户名", example = "john.doe")
    protected String username;

    @Schema(description = "用户全名或昵称", example = "John Doe")
    protected String fullName;

    @Schema(description = "用户联系电话", example = "13800138000")
    protected String phone;

    @Schema(description = "用户电子邮箱", example = "john.doe@example.com")
    protected String email;

    @Schema(description = "用户角色列表，用于权限控制", example = "ADMIN,USER")
    protected String roles;
}