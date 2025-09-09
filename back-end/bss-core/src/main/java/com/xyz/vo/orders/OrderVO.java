package com.xyz.vo.orders;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * <p>Package Name: com.xyz.vo.orders </p>
 * <p>Description:  </p>
 * <p>Create Time: 2025/9/8 </p>
 *
 * @author <a href="https://github.com/yanzhe-xiao">YANZHE XIAO</a>
 * @version 1.0
 * @since
 */
public class OrderVO {

    @Schema(name = "OrderLookVO", description = "订单查看详情（聚合订单项）")
    @Builder
    public record OrderLookVO(

            @Schema(description = "订单ID")
            Long id,

            @Schema(description = "订单状态")
            String status,

            @Schema(description = "订单创建时间")
            Date createdAt,

            @Schema(description = "安装地址")
            String installAddress,

            @Schema(description = "订单总价")
            BigDecimal price,

            @Schema(description = "订单项明细")
            List<OrderItemDetailVO> items,

            // 工程师信息
        @Schema(description = "工程师姓名")
                    String engineerFullName,

            @Schema(description = "工程师电话")
            String engineerPhone,

            @Schema(description = "工程师邮箱")
            String engineerEmail

    ) {}

    @Schema(name = "OrderItemDetailVO", description = "订单项明细")
    @Builder
    public record OrderItemDetailVO(

            @Schema(description = "套餐编码")
            String planCode,

            @Schema(description = "套餐名称")
            String planName,

            @Schema(description = "套餐类型", example = "month / year / forever")
            String planType,

            @Schema(description = "数量")
            Integer qty,

            @Schema(description = "单价（按套餐类型选择）")
            BigDecimal unitPrice,

            @Schema(description = "折扣（100为无折扣）")
            BigDecimal discount,

            @Schema(description = "该订单项小计")
            BigDecimal itemPrice,

            @Schema(description = "套餐描述")
            String description,

            @Schema(description = "分配的IP")
            String ip,

            @Schema(description = "这个结束的时间")
            Date endTime
    ) {}
}
