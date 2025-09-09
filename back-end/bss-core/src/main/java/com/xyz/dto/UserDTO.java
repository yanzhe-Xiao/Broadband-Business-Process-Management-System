package com.xyz.dto;

import com.xyz.annotation.ValidByPrefix;
import com.xyz.constraints.RoleNames;
import com.xyz.constraints.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/**
 * <p>Package Name: com.xyz.dto </p>
 * <p>Description:  </p>
 * <p>Create Time: 2025/9/8 </p>
 *
 * @author <a href="https://github.com/yanzhe-xiao">YANZHE XIAO</a>
 * @version 1.0
 * @since
 */
public class UserDTO {
    // 新增/更新用户
    public record UpsertUserDTO(
            @NotBlank @Size(min = 3, max = 64) String username,
            @NotBlank @Size(min = 6, max = 128) String password,   // 更新时也要求传入
            @NotBlank @Size(max = 128) String fullName,
            @Size(max = 32) String phone,
            @Email @Size(max = 128) String email,
            @ValidByPrefix(prefix = "ROLE_NAME_", sources = {RoleNames.class})
            @NotBlank String roleName, // 直接用 RoleNames 常量
            @ValidByPrefix(prefix = "USER_STATUS_", sources = {UserStatus.class})
            @NotBlank String status    // ACTIVE / INACTIVE
    ) {}

    public record ResetPasswordDTO(
            @NotBlank String username,
            @NotBlank @Size(min = 6, max = 128) String newPassword
    ) {}

}
