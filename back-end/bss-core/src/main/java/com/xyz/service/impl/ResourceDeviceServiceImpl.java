package com.xyz.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xyz.resources.ResourceDevice;
import com.xyz.service.ResourceDeviceService;
import com.xyz.mapper.ResourceDeviceMapper;
import org.springframework.stereotype.Service;

/**
* @author X
* @description 针对表【RESOURCE_DEVICE(网络设备资源表，用于管理网络设备的库存和分配状态)】的数据库操作Service实现
* @createDate 2025-09-04 14:44:00
*/
@Service
public class ResourceDeviceServiceImpl extends ServiceImpl<ResourceDeviceMapper, ResourceDevice>
    implements ResourceDeviceService{

}




