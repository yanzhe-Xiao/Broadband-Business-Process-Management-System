package com.xyz.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xyz.dto.IpPoolDTO;
import com.xyz.resources.IpPool;

import java.util.List;

/**
* @author X
* @description 针对表【IP_POOL(IP地址资源池表，用于管理IP地址的分配和使用)】的数据库操作Service
* @createDate 2025-09-04 16:12:06
*/
public interface IpPoolService extends IService<IpPool> {

    public IPage<IpPool> getAvaliableIp(int current, int size);
    public Integer addIps(List<IpPoolDTO.AddIpPool> addIpPools);

}
