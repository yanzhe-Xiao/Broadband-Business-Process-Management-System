package com.xyz.control;

import com.xyz.common.ResponseResult;
import com.xyz.dto.IpPoolDTO;
import com.xyz.resources.IpPool;
import com.xyz.service.IpPoolService;
import com.xyz.vo.ip_pool.IpPoolAvaliableVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>Package Name: com.xyz.control </p>
 * <p>Description:  </p>
 * <p>Create Time: 2025/9/4 </p>
 *
 * @author <a href="https://github.com/yanzhe-xiao">YANZHE XIAO</a>
 * @version 1.0
 * @since
 */
@RestController
@RequestMapping("/api/ip")
public class IpPoolController {

    @Autowired
    IpPoolService ipPoolService;

    @GetMapping("/avaliable")
    public ResponseResult<List<IpPoolAvaliableVO>> getAvaliableIpPool(){
        List<IpPool> avaliableIps = ipPoolService.getAvaliableIp();
        return ResponseResult.success(avaliableIps.stream()
                .map(ipPool -> IpPoolAvaliableVO.builder()
                        .ip(ipPool.getIp())
                        .status(ipPool.getStatus())
                        .ipBandwidth(ipPool.getIpBandwidth())
                        .avaliableBandwidth(ipPool.getAvaliableBandwidth()) // 注意字段名拼写
                        .build())
                .collect(Collectors.toList()));
    }



    @PostMapping("/add")
    public ResponseResult addIp(@RequestBody @Valid List< IpPoolDTO.@Valid AddIpPool> ips){
        Integer i = ipPoolService.addIps(ips);
        return ResponseResult.success("成功插入"+i+"行数据");
    }
}
