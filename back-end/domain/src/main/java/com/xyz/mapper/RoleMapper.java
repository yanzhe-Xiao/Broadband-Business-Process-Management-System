package com.xyz.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * <p>Package Name: com.xyz.mapper </p>
 * <p>Description: 角色数据访问层接口，用于处理用户与角色相关的数据库操作。这里使用MyBatis的注解方式来定义SQL语句。</p>
 * <p>Create Time: 2025/9/2 </p>
 *
 * @author <a href="https://github.com/yanzhe-xiao">YANZHE XIAO</a>
 * @version 1.0
 * @since
 */
@Mapper
public interface RoleMapper {

    /**
     * 根据用户ID查询其拥有的所有角色的编码（role_code）。
     * 通过联结用户角色关联表（user_role）和角色表（role）来实现。
     *
     * @param userId 用户的唯一标识ID。
     * @return 包含该用户所有角色编码的字符串列表。如果用户没有任何角色，则返回空列表。
     */
    @Select("""
        SELECT r.role_name
          FROM user_role ur
          JOIN role r ON r.id = ur.role_id
         WHERE ur.user_id = #{userId}
    """)
    String selectRoleNameByUserId(Long userId);

    /**
     * 为指定用户ID关联一个指定的角色名。
     * 此方法会先根据角色名（roleName）在角色表（ROLE）中查找对应的角色ID，
     * 然后将用户ID和查找到的角色ID插入到用户角色关联表（USER_ROLE）中。
     *
     * @param userId   用户的唯一标识ID。
     * @param roleName 要关联的角色的名称。
     * @return 返回插入操作影响的行数，通常为1表示成功，0表示失败（例如角色名不存在）。
     */
    @Insert("""
        INSERT INTO USER_ROLE (USER_ID, ROLE_ID)
        SELECT #{userId}, ID
        FROM ROLE
        WHERE ROLE.ROLE_NAME = #{roleName}
    """)
    Integer insertUserAndRoleByUserIdAndRoleName(Long userId, String roleName);
}