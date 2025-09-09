package com.xyz.mapper;

import com.xyz.user.Role;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
* @author X
* @description 针对表【ROLE】的数据库操作Mapper
* @createDate 2025-09-09 00:15:56
* @Entity com.xyz.user.Role
*/
@Mapper
public interface RoleMapper extends BaseMapper<Role> {
    /**
     * 根据用户ID查询角色名称
     * @param userId 用户ID
     * @return 角色名称（一个用户一个角色）
     */
    @Select("""
        SELECT r.role_name
        FROM role r
        JOIN user_role ur ON ur.role_id = r.id
        WHERE ur.user_id = #{userId}
        """)
    String selectRoleNameByUserId(@Param("userId") Long userId);

    /**
     * 插入 user_role 关联（如果不存在）
     */
    @Insert("""
        INSERT INTO user_role(user_id, role_id)
        SELECT #{userId}, r.id
        FROM role r
        WHERE r.role_name = #{roleName}
          AND NOT EXISTS (
              SELECT 1 FROM user_role ur
              WHERE ur.user_id = #{userId}
              AND ur.role_id = r.id
          )
        """)
    int insertUserAndRoleByUserIdAndRoleName(@Param("userId") Long userId,
                                             @Param("roleName") String roleName);
}




