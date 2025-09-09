package com.xyz.mapper;

import com.xyz.user.Role;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author X
* @description 针对表【ROLE】的数据库操作Mapper
* @createDate 2025-09-09 00:15:56
* @Entity com.xyz.user.Role
*/
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

}




