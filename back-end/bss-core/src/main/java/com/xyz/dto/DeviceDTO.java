package com.xyz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;

/**
 * <p>Package Name: com.xyz.dto </p>
 * <p>Description:  </p>
 * <p>Create Time: 2025/9/5 </p>
 *
 * @author <a href="https://github.com/yanzhe-xiao">YANZHE XIAO</a>
 * @version 1.0
 * @since
 */
public class DeviceDTO {
    @Schema(name = "DeviceAvaliableDTO", description = "可用设备信息")
    public record DeviceAvaliableDTO(

            @Schema(description = "设备序列号")
            String sn,

            @Schema(description = "设备型号")
            String model,

            @Schema(description = "设备数量")
            Integer qty,

            @Schema(description = "设备价格")
            Double price,

            @Schema(description = "设备状态", example = "STOCK/ASSIGNED/RETIRED")
            @Pattern(
                    regexp = "^(STOCK|ASSIGNED|RETIRED)$",
                    message = "设备状态只能是 STOCK, ASSIGNED, RETIRED 中的一个"
            )
            String status
    ) {}
}
