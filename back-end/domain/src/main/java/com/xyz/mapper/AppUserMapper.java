package com.xyz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xyz.user.AppUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>Package Name: com.xyz.mapper </p>
 * <p>Description: 用户实体（AppUser）的数据访问层接口。该接口继承自MyBatis-Plus的BaseMapper，自动获得了对AppUser表的CRUD（增删改查）等基本数据库操作能力，无需编写对应的XML文件。</p>
 * <p>Create Time: 2025/9/2 </p>
 *
 * @author <a href="https://github.com/yanzhe-xiao">YANZHE XIAO</a>
 * @version 1.0
 * @since
 */
@Mapper
public interface AppUserMapper extends BaseMapper<AppUser> {
}