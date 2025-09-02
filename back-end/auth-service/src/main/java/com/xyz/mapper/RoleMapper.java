package com.xyz.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RoleMapper {
    @Select("""
        SELECT r.role_code
          FROM user_role ur
          JOIN role r ON r.id = ur.role_id
         WHERE ur.user_id = #{userId}
    """)
    List<String> selectRoleCodesByUserId(Long userId);
}