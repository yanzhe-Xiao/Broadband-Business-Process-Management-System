package com.xyz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xyz.resources.IpPool;

/**
* @author X
* @description 针对表【IP_POOL(IP地址资源池表，用于管理IP地址的分配和使用)】的数据库操作Service
* @createDate 2025-09-04 16:12:06
*/
public interface IpPoolService extends IService<IpPool> {

    public void getAvaliableIp();

}
