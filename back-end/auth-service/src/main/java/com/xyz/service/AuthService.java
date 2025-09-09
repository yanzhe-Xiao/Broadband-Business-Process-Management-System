package com.xyz.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xyz.mapper.AppUserMapper;
import com.xyz.mapper.RoleMapper;
import com.xyz.user.AppUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>Package Name: com.xyz.service </p>
 * <p>Description: 认证服务类，负责处理用户的认证、注册以及角色管理等核心业务逻辑。</p>
 * <p>Create Time: 2025/9/2 </p>
 *
 * @author <a href="https://github.com/yanzhe-xiao">YANZHE XIAO</a>
 * @version 1.0
 * @since
 */
@Transactional
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final AppUserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder encoder;

    /**
     * 验证用户的凭证是否正确。
     *
     * @param username    用户输入的用户名。
     * @param rawPassword 用户输入的原始密码（未加密）。
     * @return 如果认证成功，返回完整的 AppUser 对象。
     * @throws BadCredentialsException 如果用户名不存在、用户状态非激活或密码不匹配，则抛出此异常。 [1]
     */
    public AppUser authenticate(String username, String rawPassword) {
        // 1. 根据用户名查询用户信息
        AppUser user = userMapper.selectOne(
                new LambdaQueryWrapper<AppUser>().eq(AppUser::getUsername, username)
        );
        // 2. 检查用户是否存在
        if (user == null) {
            throw new BadCredentialsException("user_not_found");
        }
        // 3. 检查用户账户状态是否为 "ACTIVE"
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new BadCredentialsException("user_inactive");
        }
        // 4. 使用 PasswordEncoder 校验原始密码和数据库中存储的加密密码是否匹配
        if (!encoder.matches(rawPassword, user.getPassword())) {
            throw new BadCredentialsException("bad_credentials");
        }
        // 5. 认证成功，返回用户信息
        return user;
    }

    /**
     * 根据用户ID加载该用户的所有角色编码。
     *
     * @param userId 用户的唯一标识ID。
     * @return 返回一个包含用户所有角色编码的字符串（当前实现只返回第一个角色）。
     */
    @Transactional(readOnly = true)
    public String loadUserRoles(Long userId) {
        // 调用RoleMapper查询用户的所有角色编码
        return roleMapper.selectRoleNameByUserId(userId);
    }

    /**
     * 处理新用户注册的逻辑。
     * 这是一个事务性操作，确保用户创建和角色分配的原子性。
     *
     * @param username    新用户的用户名。
     * @param rawPassword 新用户的原始密码。
     * @param fullName    新用户的全名。
     * @param phone       新用户的电话。
     * @param email       新用户的邮箱。
     * @param roleName    要分配给新用户的角色名称。
     * @return 返回创建成功后的 AppUser 对象（此时ID已由数据库生成）。
     */
    public AppUser register(String username, String rawPassword, String fullName,
                            String phone, String email, String roleName) {
        // 1. 创建一个新的AppUser实体
        AppUser appUser = new AppUser();
        appUser.setUsername(username);
        // 2. 对原始密码进行加密
        appUser.setPassword(encoder.encode(rawPassword));
        appUser.setFullName(fullName);
        appUser.setPhone(phone);
        appUser.setEmail(email);

        // 3. 将新用户信息插入数据库
        userMapper.insert(appUser);

        // 4. 为新创建的用户分配指定的角色
        ensureRole(appUser.getId(), roleName);

        // 5. 返回持久化后的用户对象
        return appUser;
    }

    /**
     * 确保用户拥有指定的角色。
     * 如果用户尚未拥有该角色，则在用户-角色关联表中创建一条新记录。
     * 这是一个事务性方法。
     *
     * @param userId   用户的唯一标识ID。
     * @param roleName 要确保用户拥有的角色名称。
     */
    public void ensureRole(Long userId, String roleName) {
        int rows = roleMapper.insertUserAndRoleByUserIdAndRoleName(userId, roleName);
        if (rows > 0) {
            log.info("用户 {} 成功分配角色 {}", userId, roleName);
        } else {
            log.info("用户 {} 已经拥有角色 {}", userId, roleName);
        }
    }
}