package com.xyz.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xyz.mapper.AppUserMapper;
import com.xyz.mapper.RoleMapper;
import com.xyz.user.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AppUser register(String username, String rawPassword, String fullName,
                            String phone, String email, String tenantId) {
        AppUser appUser = new AppUser();
        appUser.setUsername(username);
        appUser.setPassword(passwordEncoder.encode(rawPassword));
        int insert = userMapper.insert(appUser);

        return appUser;
    }

    /**
     * 确保用户拥有某角色；若无则插入 user_role 关系
     */
    public void ensureRole(Long userId, String roleCode) {

    }
}