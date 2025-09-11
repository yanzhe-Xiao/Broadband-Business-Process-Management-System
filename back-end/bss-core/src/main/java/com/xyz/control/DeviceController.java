package com.xyz.control;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xyz.advice.SuccessAdvice;
import com.xyz.common.PageResult;
import com.xyz.common.ResponseResult;
import com.xyz.dto.DeviceDTO;
import com.xyz.resources.ResourceDevice;
import com.xyz.service.ResourceDeviceService;
import com.xyz.vo.resources.DeviceAvaliableVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>Package Name: com.xyz.control </p>
 * <p>Description:  </p>
 * <p>Create Time: 2025/9/5 </p>
 *
 * @author <a href="https://github.com/yanzhe-xiao">YANZHE XIAO</a>
 * @version 1.0
 * @since 21
 */
@RestController
@RequestMapping("/api/device")
public class DeviceController {
    @Autowired
    ResourceDeviceService resourceDeviceService;

    @GetMapping("avaliable")
    public ResponseResult< PageResult<DeviceAvaliableVO>> getAvaliableDevice(int current,int size){
        IPage<DeviceAvaliableVO> avaliableDevice = resourceDeviceService.getAvaliableDevice(current, size);
        return ResponseResult.success(PageResult.of(avaliableDevice));
    }


    @PostMapping("add")
    public ResponseResult addDevice(@RequestBody @Valid List<DeviceDTO.@Valid DeviceAvaliableDTO> devices){
        Integer nums = resourceDeviceService.addDevices(devices);
        return ResponseResult.success(SuccessAdvice.insertSuccessMessage(nums));
    }

    @GetMapping("/all")
    public ResponseResult<List<ResourceDevice>> getAllDevice(){
        List<ResourceDevice> allDevices = resourceDeviceService.getAllDevices();
        return ResponseResult.success(allDevices);
    }
}
