package com.xyz.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xyz.constraints.IpConstraint;
import com.xyz.dto.IpPoolDTO;
import com.xyz.mapper.IpPoolMapper;
import com.xyz.resources.IpPool;
import com.xyz.service.IpPoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
    public IPage<IpPool> getAvaliableIp(int current, int size) {
        IPage<IpPool> ipPools = new Page<>(current, size);
        return ipPoolMapper.selectAllByStatus(ipPools,IpConstraint.IP_STATUS_AVALIABLE);
    }


    @Override
    @Transactional
    public Integer addIps(List<IpPoolDTO.AddIpPool> addIpPools) {
        // 将 AddIpPool DTO 转换为 IpPool 实体
        List<IpPool> ipPools = addIpPools.stream()
                .map(dto -> {
                    IpPool ipPool = new IpPool();
                    ipPool.setIp(dto.ip());
                    ipPool.setStatus(dto.status());
                    ipPool.setIpBandwidth(dto.ipBandwidth());
                    ipPool.setAvaliableBandwidth(dto.avaliableBandwidth());
                    return ipPool;
                })
                .collect(Collectors.toList());

        // 使用 MyBatis-Plus 的 saveBatch 方法
        boolean result = this.saveBatch(ipPools);

        // 返回插入成功的记录数
        return result ? ipPools.size() : 0;
    }


}




