package com.xyz.dto;

import com.xyz.constraints.OrderConstarint;
import com.xyz.mapper.TariffPlanMapper;
import com.xyz.orders.OrderItem;
import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(name = "OrderItemAvailable", description = "订单明细可用传输对象")
    public record OrderItemAvailable(

//            @Schema(description = "订单ID，外键关联orders表", example = "1001")
//            Long orderId,

            @Schema(description = "套餐编码，外键关联tariff_plan表", example = "PLAN2025A")
            String planCode,

            @Schema(description = "生效的开始时间", example = "2025-09-06T12:00:00Z")
            Date startBillingAt,

            @Schema(description = "数量", example = "2")
            Integer qty,

//            @Schema(description = "价格", example = "99.99")
//            BigDecimal price,

            @Schema(description = "订单状态", example = "在购物车中 / 待支付 / 已支付")
            @Pattern(
                    regexp = "^(在购物车中|待支付|已支付)$",
                    message = "订单状态只能是 在购物车中 / 待支付 / 已支付"
            )
            String status,

            @Schema(description = "用户名", example = "xiaoyanzhe")
            String username,

            @Schema(description = "套餐类型，只能是 month / year / forever", example = "month")
            @Pattern(
                    regexp = "^(month|year|forever)$",
                    message = "套餐类型只能是 month / year / forever"
            )
            String planType
    ) {
        public static OrderItem toEntity(OrderItemAvailable dto,BigDecimal price) {
            OrderItem entity = new OrderItem();
//  entity.setOrderId(dto.orderId());
            entity.setPlanCode(dto.planCode());
            entity.setStartBillingAt(dto.startBillingAt());
            entity.setQty(dto.qty());
            entity.setPrice(price);
            entity.setStatus(dto.status());
            entity.setUsername(dto.username());
            entity.setPlanType(dto.planType());

            //  endBilling
            entity.setEndBilling(calculateEndBilling(dto.startBillingAt(), dto.planType(), dto.qty()));

            return entity;
        }

    }
    public static Date calculateEndBilling(Date startBillingAt, String planType, Integer qty) {
        if (startBillingAt != null && planType != null && qty != null) {
            if ("forever".equals(planType)) {
                return OrderConstarint.FOREVER_DATE;
            }

            Calendar cal = Calendar.getInstance();
            cal.setTime(startBillingAt);
            switch (planType) {
                case "month" -> cal.add(Calendar.MONTH, qty);
                case "year" -> cal.add(Calendar.YEAR, qty);
                default -> throw new IllegalArgumentException("未知的套餐类型: " + planType);
            }
            return cal.getTime();
        }
        return null;
    }
}
