package com.xyz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xyz.constraints.RoleNames;
import com.xyz.constraints.UserStatus;
import com.xyz.dto.PageReq;
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
import com.xyz.vo.Profile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.util.StringUtils;

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

    @Autowired
    PasswordEncoder encoder;
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
        String username = dto.username();
        if (!org.springframework.util.StringUtils.hasText(username)) {
            throw new IllegalArgumentException("username 不能为空");
        }

        // 1) 定位用户
        AppUser user = appUserMapper.selectOne(
                Wrappers.<AppUser>lambdaQuery().eq(AppUser::getUsername, username));
        if (user == null) {
            throw new IllegalArgumentException("用户不存在: " + username);
        }

        // 2) 按字段是否为空决定是否更新
        UpdateWrapper<AppUser> uw = Wrappers.<AppUser>update().eq("id", user.getId());
        boolean hasSet = false;

        if (dto.fullName() != null) {
            uw.set("full_name", dto.fullName());
            hasSet = true;
        }
        if (dto.phone() != null) {
            uw.set("phone", dto.phone());
            hasSet = true;
        }
        if (dto.email() != null) {
            uw.set("email", dto.email());
            hasSet = true;
        }
        if (dto.status() != null) {
            uw.set("status", dto.status());
            hasSet = true;
        }
        int affected = 0;
        if (hasSet) {
            affected = appUserMapper.update(null, uw);
        }

        // 3) 角色变更（仅当传入了 roleName 时处理；不传则不改）
        if (org.springframework.util.StringUtils.hasText(dto.roleName())) {
            // 3.1 查到目标角色ID（不存在则创建）
            Role role = roleMapper.selectOne(
                    Wrappers.<Role>lambdaQuery().eq(Role::getRoleName, dto.roleName()));
            if (role == null) {
                role = new Role();
                role.setRoleName(dto.roleName());
                roleMapper.insert(role);
            }
            Long targetRoleId = role.getId();

            // 3.2 查询当前用户的角色ID；若已相同则不动
            UserRole current = userRoleMapper.selectOne(
                    Wrappers.<UserRole>lambdaQuery().eq(UserRole::getUserId, user.getId()));
            Long currentRoleId = current != null ? current.getRoleId() : null;

            if (!Objects.equals(currentRoleId, targetRoleId)) {
                // 先删再插，保持单角色
                userRoleMapper.delete(Wrappers.<UserRole>lambdaQuery().eq(UserRole::getUserId, user.getId()));
                UserRole ur = new UserRole();
                ur.setUserId(user.getId());
                ur.setRoleId(targetRoleId);
                userRoleMapper.insert(ur);

                // 3.3 同步工程师状态表：只有当新角色是“装维工程师”时确保存在；否则删除
                if (RoleNames.ROLE_NAME_ENGINEER.equals(dto.roleName())) {
                    EngineerStatus es = engineerStatusService.getOne(
                            Wrappers.<EngineerStatus>lambdaQuery().eq(EngineerStatus::getUserId, user.getId()));
                    if (es == null) {
                        es = new EngineerStatus();
                        es.setUserId(user.getId());
                        es.setIsIdle(1);
                        es.setActiveTicketCnt(0);
                        engineerStatusService.save(es);
                    }
                } else {
                    engineerStatusService.remove(
                            Wrappers.<EngineerStatus>lambdaQuery().eq(EngineerStatus::getUserId, user.getId()));
                }
            }
        }

        return hasSet ? affected : 1; // 没有基本信息变更，但角色可能变更，此时返回 1 表示成功
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
        String encode = encoder.encode(newPassword);
        return appUserMapper.update(null,
                Wrappers.<AppUser>update()
                        .eq("username", username)
                        .set("password", encode));
//                        .set("updated_at", new Date()));
    }

    private String encode(String raw) {
        if (encoder != null) return encoder.encode(raw);
        return raw;
    }

    public Long usernameToUserId(String username){
        AppUser user = appUserMapper.selectOne(
                Wrappers.<AppUser>lambdaQuery()
                        .eq(AppUser::getUsername, username)
        );
        return user != null ? user.getId() : null;
    }

    @Override
    @Transactional(readOnly = true)
    public IPage<Profile> queryUserPage(PageReq req) {
        int pageNo = req.getCurrent() == null || req.getCurrent() < 1 ? 1 : req.getCurrent();
        int pageSize = req.getSize() == null || req.getSize() <= 0 ? 10 : req.getSize();

        // 1) 基础查询
        IPage<AppUser> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<AppUser> qw = Wrappers.<AppUser>lambdaQuery();

        if (StringUtils.hasText(req.getKeyword())) {
            qw.and(q -> q.like(AppUser::getUsername, req.getKeyword())
                    .or().like(AppUser::getFullName, req.getKeyword())
                    .or().like(AppUser::getEmail, req.getKeyword())
                    .or().like(AppUser::getPhone, req.getKeyword()));
        }
        if (!Objects.equals(req.getStatus(), "all") && StringUtils.hasText(req.getStatus())) {
            qw.eq(AppUser::getStatus, req.getStatus());
        }

        IPage<AppUser> userPage = this.page(page, qw);

        if (userPage.getRecords().isEmpty()) {
            return new Page<>(pageNo, pageSize, 0);
        }

        // 2) 查角色（一个用户一个角色）
        List<Long> userIds = userPage.getRecords().stream()
                .map(AppUser::getId)
                .toList();

        List<RoleMapper.UserRoleNameDTO> rows = roleMapper.selectRoleNameListByUserIds(userIds);
        Map<Long, String> roleMap = rows.stream()
                .collect(Collectors.toMap(RoleMapper.UserRoleNameDTO::userId, RoleMapper.UserRoleNameDTO::roleName, (a, b) -> a));

        // 3) 组装 Profile
        List<Profile> records = userPage.getRecords().stream()
                .map(u -> Profile.builder()
                        .username(u.getUsername())
                        .fullName(u.getFullName())
                        .roleName(roleMap.get(u.getId()))
                        .status(u.getStatus())
                        .email(u.getEmail())
                        .phone(u.getPhone())
                        .build())
                .toList();

        Page<Profile> result = new Page<>(pageNo, pageSize, userPage.getTotal());
        result.setRecords(records);
        return result;
    }
}




