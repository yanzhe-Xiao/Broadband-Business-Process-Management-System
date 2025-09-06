package com.xyz.vo.resources;

import io.swagger.v3.oas.annotations.media.Schema;
//import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>Package Name: com.xyz.vo.resources </p>
 * <p>Description:  </p>
 * <p>Create Time: 2025/9/5 </p>
 *
 * @author <a href="https://github.com/yanzhe-xiao">YANZHE XIAO</a>
 * @version 1.0
 * @since
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "DeviceAvaliableVO", description = "可用设备信息")
public class DeviceAvaliableVO {

    @Schema(description = "设备序列号")
    private String sn;

    @Schema(description = "设备型号")
    private String model;

    @Schema(description = "设备数量")
    private Integer qty;

    @Schema(description = "设备价格")
    private Double price;

    @Schema(description = "设备状态", example = "STOCK/ASSIGNED/RETIRED")
//    @Pattern(regexp = "^(STOCK|ASSIGNED|RETIRED)$",
//            message = "设备状态只能是 STOCK, ASSIGNED, RETIRED 中的一个")
    private String status;
}
