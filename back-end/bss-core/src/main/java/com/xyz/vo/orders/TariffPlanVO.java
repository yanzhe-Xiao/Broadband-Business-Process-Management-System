package com.xyz.vo.orders;

import lombok.Builder;

import java.math.BigDecimal;

/**
 * <p>Package Name: com.xyz.vo.ip_pool.vo </p>
 * <p>Description:  </p>
 * <p>Create Time: 2025/9/5 </p>
 *
 * @author <a href="https://github.com/yanzhe-xiao">YANZHE XIAO</a>
 * @version 1.0
 * @since
 */
public class TariffPlanVO {

    @Builder
    public record TariffPlanDetail(
            // 套餐基本信息
            String planCode,
            String name,
            String description,
            BigDecimal monthlyFee, // 月费
            BigDecimal yearlyFee,
            BigDecimal foreverFee,
            Double installationFee, // 安装费
            Integer contractPeriod, // 合约期，单位月
            String status, // 套餐状态
            // IP资源信息
            Integer isIp, // 是否需要IP
            Integer bandwidth, // 带宽，单位MB
            Integer qty,
            BigDecimal rating,
            String imageUrl,
            // 设备资源信息
            String requireDeviceSn,
            String requiredDeviceModel, // 所需设备型号
            Integer requiredDeviceQty, // 所需设备数量
            Double devicePrice // 设备单价
    ){}
}
