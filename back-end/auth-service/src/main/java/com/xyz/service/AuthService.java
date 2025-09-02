package com.xyz.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xyz.mapper.AppUserMapper;
import com.xyz.mapper.RoleMapper;
import com.xyz.user.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AppUserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder encoder;

    public AppUser authenticate(String username, String rawPassword) {
        AppUser user = userMapper.selectOne(
                new LambdaQueryWrapper<AppUser>().eq(AppUser::getUsername, username)
        );
        if (user == null) throw new BadCredentialsException("user_not_found");
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus()))
            throw new BadCredentialsException("user_inactive");
        if (!encoder.matches(rawPassword, user.getPassword()))
            throw new BadCredentialsException("bad_credentials");
        return user;
    }

    public List<String> loadUserRoles(Long userId) {
        // 返回如 ["ROLE_ADMIN","ROLE_TECH"]
        return roleMapper.selectRoleCodesByUserId(userId);
    }
}