package com.xyz.vo.orders;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;

/**
 * <p>Package Name: com.xyz.vo.orders </p>
 * <p>Description:  </p>
 * <p>Create Time: 2025/9/7 </p>
 *
 * @author <a href="https://github.com/yanzhe-xiao">YANZHE XIAO</a>
 * @version 1.0
 * @since
 */
public class OrderItemVO {

    @Builder
    @Schema(description = "购物车VO")
    public record Shopping(
            @Schema(description = "订单明细ID，主键")
            Long id,
            @Schema(description = "套餐编码")
            String planCode,
            @Schema(description = "套餐名称")
            String name,
            @Schema(description = "套餐数量")
            Integer qty,
            @Schema(description = "套餐类型，只能是month/year/forever三种类型")
            String planType,
            @Schema(description = "订单项状态，只有三个状态：在购物车中/待支付/已支付")
            String status,
            @Schema(description = "总价")
            BigDecimal price,
            @Schema(description = "套餐所维持的时间，单位天，forever类型为Long.MAX_VALUE")
            Long period,
            @Schema(description = "套餐的描述")
            String description,
            @Schema(description = "套餐的image")
            String imageUrl
    ) {
    }
}
