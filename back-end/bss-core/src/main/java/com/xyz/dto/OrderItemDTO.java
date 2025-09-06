package com.xyz.dto;

import com.xyz.constraints.OrderConstarint;
import com.xyz.mapper.TariffPlanMapper;
import com.xyz.orders.OrderItem;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

/**
 * <p>Package Name: com.xyz.dto </p>
 * <p>Description: orderItem的dto </p>
 * <p>Create Time: 2025/9/6 </p>
 *
 * @author <a href="https://github.com/yanzhe-xiao">YANZHE XIAO</a>
 * @version 1.0
 * @since 21
 */
public class OrderItemDTO {

    @Schema(name = "OrderItemAvaliable", description = "订单明细 DTO")
    public record OrderItemAvaliable(

            @Schema(description = "套餐编码", example = "123")
            @NotBlank(message = "套餐编码不能为空")
            String planCode,

            @Schema(description = "数量", example = "1")
            @NotNull(message = "数量不能为空")
            Integer qty,

            @Schema(description = "订单状态", example = "在购物车中 / 待支付 / 已支付")
            @Pattern(
                    regexp = "^(在购物车中|待支付|已支付)$",
                    message = "订单状态只能是 在购物车中 / 待支付 / 已支付"
            )
            String status,

            @Schema(description = "用户名", example = "admin")
            @NotBlank(message = "用户名不能为空")
            String username,

            @Schema(description = "套餐类型", example = "month / year / forever")
            @Pattern(
                    regexp = "^(month|year|forever)$",
                    message = "套餐类型只能是 month / year / forever"
            )
            String planType
    ) {
        /**
         * 转换成 OrderItem 实体
         */
        public OrderItem toEntity(BigDecimal price) {
            OrderItem entity = new OrderItem();
//            entity.setOrderId(orderId);
            entity.setPlanCode(this.planCode);
            entity.setQty(this.qty);
            entity.setStatus(this.status);
            entity.setUsername(this.username);
            entity.setPlanType(this.planType);

            // period 计算
            long period;
            switch (this.planType) {
                case "month" -> period = 30L * this.qty;
                case "year" -> period = 365L * this.qty;
                case "forever" -> period = Long.MAX_VALUE;
                default -> throw new IllegalArgumentException("非法套餐类型: " + this.planType);
            }
            entity.setPeriod(period);
            // 价格先空
            entity.setPrice(price);

            return entity;
        }
    }


    @Schema(name = "OrderItemUpdate", description = "订单项更新 DTO")
    public record OrderItemUpdate(
            @Schema(description = "订单项ID", example = "1")
            @NotNull(message = "订单项ID不能为空")
            Long id,

            @Schema(description = "数量", example = "1")
            @NotNull(message = "数量不能为空")
            Integer qty,

            @Schema(description = "套餐类型", example = "month")
            @Pattern(
                    regexp = "^(month|year|forever)$",
                    message = "套餐类型只能是 month / year / forever"
            )
            String planType
    ) { }


}
