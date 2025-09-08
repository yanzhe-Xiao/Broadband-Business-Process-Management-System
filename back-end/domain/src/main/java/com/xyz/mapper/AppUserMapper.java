package com.xyz.mapper;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import com.xyz.user.AppUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
* @author X
* @description 针对表【APP_USER】的数据库操作Mapper
* @createDate 2025-09-07 22:56:35
* @Entity com.xyz.user.AppUser
*/
@Mapper
public interface AppUserMapper extends BaseMapper<AppUser> {
    Long selectIdByUsername(@Param("username") String username);

    /**
     * 选择负载最小的空闲工程师ID
     *
     * @return 工程师用户ID，如果没有符合条件的工程师则返回null
     */
    @Select("""
        SELECT u.id
        FROM app_user u
        JOIN user_role ur ON ur.user_id = u.id
        JOIN role r ON r.id = ur.role_id
        JOIN engineer_status es ON es.user_id = u.id
        WHERE r.role_name = 'ENGINEER'
          AND u.status = 'ACTIVE'
          AND es.is_idle = 1
        ORDER BY es.active_ticket_cnt ASC, es.updated_at ASC
        FETCH FIRST 1 ROWS ONLY
        """)
    Long selectBalancedIdleEngineerId();

    /**
     * 选择活跃工单数最少的工程师ID（兜底方法）
     *
     * @return 工程师用户ID，如果没有符合条件的工程师则返回null
     */
    @Select("""
        SELECT u.id
        FROM app_user u
        JOIN user_role ur ON ur.user_id = u.id
        JOIN role r ON r.id = ur.role_id
        JOIN engineer_status es ON es.user_id = u.id
        WHERE r.role_name = 'ENGINEER'
          AND u.status = 'ACTIVE'
        ORDER BY es.active_ticket_cnt ASC, es.updated_at ASC
        FETCH FIRST 1 ROWS ONLY
        """)
    Long selectBalancedEngineerId();
}




