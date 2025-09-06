package com.xyz.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xyz.dto.DeviceDTO;
import com.xyz.resources.ResourceDevice;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xyz.vo.resources.DeviceAvaliableVO;

import java.util.List;

/**
* @author X
* @description 针对表【RESOURCE_DEVICE(网络设备资源表，用于管理网络设备的库存和分配状态)】的数据库操作Service
* @createDate 2025-09-04 14:44:00
*/
public interface ResourceDeviceService extends IService<ResourceDevice> {
    public IPage<DeviceAvaliableVO> getAvaliableDevice(int current,int size);

    public Integer addDevices(List<DeviceDTO.DeviceAvaliableDTO> devices);
}
