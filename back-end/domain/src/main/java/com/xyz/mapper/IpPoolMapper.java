package com.xyz.mapper;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import com.xyz.resources.IpPoolresources;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author X
* @description 针对表【IP_POOL(IP地址资源池表，用于管理IP地址的分配和使用)】的数据库操作Mapper
* @createDate 2025-09-04 14:42:15
* @Entity com.xyz.domain.IpPoolresources
*/
public interface IpPoolMapper extends BaseMapper<IpPoolresources> {

    List<IpPoolresources> selectByIp(@Param("ip") String ip);

}




