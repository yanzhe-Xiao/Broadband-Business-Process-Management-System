package com.xyz.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xyz.constraints.RoleNames;
import com.xyz.constraints.UserStatus;
import com.xyz.dto.UserDTO;
import com.xyz.mapper.RoleMapper;
import com.xyz.mapper.UserRoleMapper;
import com.xyz.service.EngineerStatusService;
import com.xyz.user.AppUser;
import com.xyz.service.AppUserService;
import com.xyz.mapper.AppUserMapper;
import com.xyz.user.EngineerStatus;
import com.xyz.user.Role;
import com.xyz.user.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
* @author X
* @description 针对表【APP_USER】的数据库操作Service实现
* @createDate 2025-09-07 22:56:35
*/
@Service
public class AppUserServiceImpl extends ServiceImpl<AppUserMapper, AppUser>
    implements AppUserService{

    @Autowired
    AppUserMapper appUserMapper;

    @Autowired
    RoleMapper roleMapper;

    @Autowired
    UserRoleMapper userRoleMapper;

    @Autowired
    EngineerStatusService engineerStatusService;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int createUser(UserDTO.UpsertUserDTO dto) {
        // 用户名唯一
        if (appUserMapper.exists(Wrappers.<AppUser>lambdaQuery().eq(AppUser::getUsername, dto.username()))) {
            throw new IllegalStateException("用户名已存在: " + dto.username());
        }

        // 插入用户
        AppUser user = new AppUser();
        user.setUsername(dto.username());
        user.setPassword(encode(dto.password()));
        user.setFullName(dto.fullName());
        user.setPhone(dto.phone());
        user.setEmail(dto.email());
        user.setStatus(dto.status());
//        user.setCreatedAt(new Date());
        appUserMapper.insert(user);
        Long userId = user.getId();

        // 插入角色
        Role role = roleMapper.selectOne(Wrappers.<Role>lambdaQuery().eq(Role::getRoleName, dto.roleName()));
        if (role == null) {
            role = new Role();
            role.setRoleName(dto.roleName());
            roleMapper.insert(role);
        }
        UserRole ur = new UserRole();
        ur.setUserId(userId);
        ur.setRoleId(role.getId());
        userRoleMapper.insert(ur);

        // 如果是工程师，初始化 engineer_status
        if (RoleNames.ROLE_NAME_ENGINEER.equals(dto.roleName())) {
            EngineerStatus es = new EngineerStatus();
            es.setUserId(userId);
            es.setIsIdle(1);
            es.setActiveTicketCnt(0);
//            es.setUpdatedAt(new Date());
            engineerStatusService.save(es);
        }
        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateUser(UserDTO.UpsertUserDTO dto) {
        AppUser user = appUserMapper.selectOne(Wrappers.<AppUser>lambdaQuery().eq(AppUser::getUsername, dto.username()));
        if (user == null) throw new IllegalArgumentException("用户不存在: " + dto.username());

        user.setPassword(encode(dto.password()));
        user.setFullName(dto.fullName());
        user.setPhone(dto.phone());
        user.setEmail(dto.email());
        user.setStatus(dto.status());
//        user.setUpdatedAt(new Date());
        appUserMapper.updateById(user);

        // 更新角色：先删后加
        userRoleMapper.delete(Wrappers.<UserRole>lambdaQuery().eq(UserRole::getUserId, user.getId()));
        Role role = roleMapper.selectOne(Wrappers.<Role>lambdaQuery().eq(Role::getRoleName, dto.roleName()));
        if (role == null) {
            role = new Role();
            role.setRoleName(dto.roleName());
            roleMapper.insert(role);
        }
        UserRole ur = new UserRole();
        ur.setUserId(user.getId());
        ur.setRoleId(role.getId());
        userRoleMapper.insert(ur);

        // 如果是工程师，保证 engineer_status 存在
        if (RoleNames.ROLE_NAME_ENGINEER.equals(dto.roleName())) {
            EngineerStatus es = engineerStatusService.getOne(
                    Wrappers.<EngineerStatus>lambdaQuery().eq(EngineerStatus::getUserId, user.getId()));
            if (es == null) {
                es = new EngineerStatus();
                es.setUserId(user.getId());
                es.setIsIdle(1);
                es.setActiveTicketCnt(0);
//                es.setUpdatedAt(new Date());
                engineerStatusService.save(es);
            }
        } else {
            // 非工程师，移除 engineer_status（可选）
            engineerStatusService.remove(Wrappers.<EngineerStatus>lambdaQuery().eq(EngineerStatus::getUserId, user.getId()));
        }
        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteUser(String username) {
        AppUser user = appUserMapper.selectOne(Wrappers.<AppUser>lambdaQuery().eq(AppUser::getUsername, username));
        if (user == null) return 0;

        Long userId = user.getId();
        userRoleMapper.delete(Wrappers.<UserRole>lambdaQuery().eq(UserRole::getUserId, userId));
        engineerStatusService.remove(Wrappers.<EngineerStatus>lambdaQuery().eq(EngineerStatus::getUserId, userId));
        return appUserMapper.deleteById(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int resetPassword(String username, String newPassword) {
        return appUserMapper.update(null,
                Wrappers.<AppUser>update()
                        .eq("username", username)
                        .set("password", encode(newPassword)));
//                        .set("updated_at", new Date()));
    }

    private String encode(String raw) {
//        if (passwordEncoder != null) return passwordEncoder.encode(raw);
        return raw;
    }

    public Long usernameToUserId(String username){
        AppUser user = appUserMapper.selectOne(
                Wrappers.<AppUser>lambdaQuery()
                        .eq(AppUser::getUsername, username)
        );
        return user != null ? user.getId() : null;
    }
}




