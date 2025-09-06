package com.xyz.control;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xyz.advice.SuccessAdvice;
import com.xyz.common.PageResult;
import com.xyz.common.ResponseResult;
import com.xyz.dto.IpPoolDTO;
import com.xyz.resources.IpPool;
import com.xyz.service.IpPoolService;
import com.xyz.vo.resources.IpPoolAvaliableVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseResult<PageResult<IpPoolAvaliableVO>> getAvaliableIpPool(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size) {

        IPage<IpPool> pageResult = ipPoolService.getAvaliableIp(current, size);

        // 转换为 VO 对象的分页结果
        IPage<IpPoolAvaliableVO> voPage = pageResult.convert(ipPool ->
                IpPoolAvaliableVO.builder()
                        .ip(ipPool.getIp())
                        .status(ipPool.getStatus())
                        .ipBandwidth(ipPool.getIpBandwidth())
                        .avaliableBandwidth(ipPool.getAvaliableBandwidth())
                        .build()
        );

        return ResponseResult.success(PageResult.of(voPage));
    }


    @PostMapping("/add")
    public ResponseResult addIp(@RequestBody @Valid List< IpPoolDTO.@Valid AddIpPool> ips){
        Integer i = ipPoolService.addIps(ips);
        return ResponseResult.success(SuccessAdvice.insertSuccessMessage(i));
    }
}
