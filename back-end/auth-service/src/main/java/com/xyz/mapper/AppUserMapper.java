package com.xyz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xyz.user.AppUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AppUserMapper extends BaseMapper<AppUser> {}