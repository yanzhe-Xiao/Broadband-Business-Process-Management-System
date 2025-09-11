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
            String username,
            String password,
            @Size(max = 128) String fullName,
            @Size(max = 32) String phone,
            @Email @Size(max = 128) String email,
            String roleName, // 直接用 RoleNames 常量
            String status
    ) {}

    public record ResetPasswordDTO(
            @NotBlank String username,
            @NotBlank @Size(min = 6, max = 128) String newPassword
    ) {}

}
