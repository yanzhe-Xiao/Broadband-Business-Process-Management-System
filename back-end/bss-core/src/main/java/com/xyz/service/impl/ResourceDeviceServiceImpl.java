package com.xyz.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xyz.constraints.DeviceConstarint;
import com.xyz.dto.DeviceDTO;
import com.xyz.mapper.ResourceDeviceMapper;
import com.xyz.resources.ResourceDevice;
import com.xyz.service.ResourceDeviceService;
import com.xyz.vo.resources.DeviceAvaliableVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
* @author X
* @description 针对表【RESOURCE_DEVICE(网络设备资源表，用于管理网络设备的库存和分配状态)】的数据库操作Service实现
* @createDate 2025-09-04 14:44:00
*/
@Service
public class ResourceDeviceServiceImpl extends ServiceImpl<ResourceDeviceMapper, ResourceDevice>
    implements ResourceDeviceService{

    @Autowired
    ResourceDeviceMapper resourceDeviceMapper;

    @Override
    @Transactional
    public IPage<DeviceAvaliableVO> getAvaliableDevice(int current, int size) {
        IPage<ResourceDevice> page = new Page<>(current,size);
        IPage<ResourceDevice> resourceDevices =
                resourceDeviceMapper.selectAllByStatus(page,DeviceConstarint.DEVICE_STATUS_STOCK);
        return resourceDevices.convert(resourceDevice -> DeviceAvaliableVO.builder()
                .qty(resourceDevice.getQty())
                .sn(resourceDevice.getSn())
                .price(Double.valueOf(resourceDevice.getPrice()))
                .model(resourceDevice.getModel())
                .status(resourceDevice.getStatus())
                .build());
    }

    @Override
    @Transactional
    public Integer addDevices(List<DeviceDTO.DeviceAvaliableDTO> devices) {
        List<ResourceDevice> resourceDevices = devices.stream()
                .map(deviceAvaliableDTO -> {
                    ResourceDevice resourceDevice = new ResourceDevice();
                    resourceDevice.setStatus(deviceAvaliableDTO.status());
                    resourceDevice.setSn(deviceAvaliableDTO.sn());
                    resourceDevice.setModel(deviceAvaliableDTO.model());
                    resourceDevice.setQty(deviceAvaliableDTO.qty());
                    resourceDevice.setPrice(deviceAvaliableDTO.price());
                    return resourceDevice;
                })
                .toList();
        boolean result = this.saveBatch(resourceDevices);
        return result ? resourceDevices.size() : 0;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceDevice> getAllDevices() {
        return resourceDeviceMapper.selectAll();
    }
}




