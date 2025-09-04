package com.xyz.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xyz.resources.IpPoolresources;
import com.xyz.service.IpPoolService;
import com.xyz.mapper.IpPoolMapper;
import org.springframework.stereotype.Service;

/**
* @author X
* @description 针对表【IP_POOL(IP地址资源池表，用于管理IP地址的分配和使用)】的数据库操作Service实现
* @createDate 2025-09-04 14:42:15
*/
@Service
public class IpPoolServiceImpl extends ServiceImpl<IpPoolMapper, IpPoolresources>
    implements IpPoolService{

}




