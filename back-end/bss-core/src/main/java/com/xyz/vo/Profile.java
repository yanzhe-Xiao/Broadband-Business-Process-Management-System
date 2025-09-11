package com.xyz.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(name = "Profile", description = "用户信息")
@Builder
public record Profile(
        String username,
        String fullName,
        String roleName,
        String status,
        String email,
        String phone
) {}