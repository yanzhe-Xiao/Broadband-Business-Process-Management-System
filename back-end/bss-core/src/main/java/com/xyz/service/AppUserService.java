package com.xyz.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xyz.dto.PageReq;
import com.xyz.dto.UserDTO;
import com.xyz.user.AppUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xyz.vo.Profile;

/**
* @author X
* @description 针对表【APP_USER】的数据库操作Service
* @createDate 2025-09-07 22:56:35
*/
public interface AppUserService extends IService<AppUser> {
    int createUser(UserDTO.UpsertUserDTO dto);
    int updateUser(UserDTO.UpsertUserDTO dto);
    int deleteUser(String username);
    int resetPassword(String username, String newPassword);
    public Long usernameToUserId(String username);
    /**
     * 分页查询用户
     *
     * @param req 查询参数
     * @return 分页结果（Profile）
     */
    IPage<Profile> queryUserPage(PageReq req);
}
