package com.xyz.vo.ip_pool;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>Package Name: com.xyz.vo.ip_pool </p>
 * <p>Description: 可用IP池信息视图对象(VO)，用于返回IP地址、状态、总带宽和可用带宽等信息。</p>
 * <p>Create Time: 2025/9/4 </p>
 *
 * @author <a href="https://github.com/yanzhe-xiao">YANZHE XIAO</a>
 * @version 1.0
 * @since 21
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "IpPoolAvaliableVO", description = "可用IP池信息")
public class IpPoolAvaliableVO {

    @Schema(description = "IP地址", example = "192.168.1.100")
    private String ip;

    @Schema(description = "IP状态", example = "AVAILABLE")
    private String status;

    @Schema(description = "该IP的总带宽（单位：Mbps）", example = "1000")
    private Integer ipBandwidth;

    @Schema(description = "当前可用带宽（单位：Mbps）", example = "800")
    private Integer avaliableBandwidth;
}