package com.xyz.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xyz.user.Role;
import com.xyz.service.RoleService;
import com.xyz.mapper.RoleMapper;
import org.springframework.stereotype.Service;

/**
* @author X
* @description 针对表【ROLE】的数据库操作Service实现
* @createDate 2025-09-09 00:15:56
*/
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role>
    implements RoleService{

}




