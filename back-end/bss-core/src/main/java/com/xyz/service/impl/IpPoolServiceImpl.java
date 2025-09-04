package com.xyz.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xyz.constraints.IpConstraint;
import com.xyz.mapper.IpPoolMapper;
import com.xyz.resources.IpPool;
import com.xyz.service.IpPoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
* @author X
* @description 针对表【IP_POOL(IP地址资源池表，用于管理IP地址的分配和使用)】的数据库操作Service实现
* @createDate 2025-09-04 16:12:06
*/
@Service
public class IpPoolServiceImpl extends ServiceImpl<IpPoolMapper, IpPool>
    implements IpPoolService{

    @Autowired
    private IpPoolMapper ipPoolMapper;
    @Override
    public void getAvaliableIp() {
        ipPoolMapper.selectByStatusAndIpBandwidth(IpConstraint.IP_AVALIABLE_STATUS,null);
    }
}




