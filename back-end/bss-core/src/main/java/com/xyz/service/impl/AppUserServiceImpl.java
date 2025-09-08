package com.xyz.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xyz.user.AppUser;
import com.xyz.service.AppUserService;
import com.xyz.mapper.AppUserMapper;
import org.springframework.stereotype.Service;

/**
* @author X
* @description 针对表【APP_USER】的数据库操作Service实现
* @createDate 2025-09-07 22:56:35
*/
@Service
public class AppUserServiceImpl extends ServiceImpl<AppUserMapper, AppUser>
    implements AppUserService{

}




