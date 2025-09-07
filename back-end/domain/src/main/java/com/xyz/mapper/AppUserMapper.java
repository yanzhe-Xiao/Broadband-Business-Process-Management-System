package com.xyz.mapper;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import com.xyz.user.AppUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author X
* @description 针对表【APP_USER】的数据库操作Mapper
* @createDate 2025-09-07 22:56:35
* @Entity com.xyz.user.AppUser
*/
@Mapper
public interface AppUserMapper extends BaseMapper<AppUser> {
    Long selectIdByUsername(@Param("username") String username);
}




