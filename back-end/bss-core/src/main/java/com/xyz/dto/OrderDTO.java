package com.xyz.dto;

import com.xyz.constraints.OrderConstarint;
import com.xyz.orders.Orders;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>Package Name: com.xyz.dto </p>
 * <p>Description:  </p>
 * <p>Create Time: 2025/9/7 </p>
 *
 * @author <a href="https://github.com/yanzhe-xiao">YANZHE XIAO</a>
 * @version 1.0
 * @since
 */
public class OrderDTO {

    @Schema(name = "OrdersAvailableDTO", description = "订单可用信息传输对象")
    public record OrderAvaliableDTO(
            @Schema(description = "用户name")
            String username,

            @Schema(description = "订单项ID")
            List<Long> orderItemId,

//            @Schema(description = "订单状态",
//                    allowableValues = {
//                            "待支付", "已支付", "待派单",
//                            "已分配工单", "工单已完成",
//                            "待评价", "已完成", "已取消"
//                    })
//            @Pattern(regexp = "待支付|已支付|待派单|已分配工单|工单已完成|待评价|已完成|已取消",
//                    message = "订单状态必须是固定值之一")
//            String status,

            @Schema(description = "省/自治区/直辖市")
            String province,

            @Schema(description = "地级市/自治州；直辖市可留空或与省相同")
            String city,

            @Schema(description = "区/县")
            String district,

            @Schema(description = "小区/写字楼/门牌号等")
            String detailAddress
    ) {
        /**
         * DTO 转换为实体
         */
        public static Orders toEntity(OrderAvaliableDTO dto,Long userId) {
            Orders entity = new Orders();
            entity.setUserId(userId);
//            entity.setStatus(dto.status());
            entity.setProvince(dto.province());
            entity.setCity(dto.city());
            entity.setDistrict(dto.district());
            entity.setDetailAddress(dto.detailAddress());
            entity.setInstallationFee(BigDecimal.valueOf(OrderConstarint.INSTALLATION_FEE));
            return entity;
        }

        public static OrderAvaliableDTO withUsername(OrderControllerDTO old, String username) {
            return new OrderAvaliableDTO(
                    username,
                    old.orderItemId(),
                    old.province(),
                    old.city(),
                    old.district(),
                    old.detailAddress()
            );
        }
    }

    @Schema(name = "OrderPaymentDTO", description = "订单支付信息传输对象")
    public record OrderPaymentDTO(
            @Schema(description = "用户名")
            String username,

            @Schema(description = "订单ID")
            Long orderId
    ) {
        public static OrderPaymentDTO withUsername(OrderPaymentDTO old, String username) {
            return new OrderPaymentDTO(
                    username,
                    old.orderId()
            );
        }
    }

    @Schema(name = "OrderDeleteDTO",description = "删除订单的DTO")
    public record OrderDeleteDTO(
            @Schema(description = "用户名")
            String username,

            @Schema(description = "订单ID")
            List<Long> orderId
    ){
        public static OrderDeleteDTO withUsername(OrderDeleteDTO old, String username) {
            return new OrderDeleteDTO(
                    username,
                    old.orderId()
            );
        }
    }

    @Schema(name = "OrderControllerDTO", description = "订单可用信息传输对象")
    public record OrderControllerDTO(
//            @Schema(description = "用户name")
//            String username,

            @Schema(description = "订单项ID")
            List<Long> orderItemId,

//            @Schema(description = "订单状态",
//                    allowableValues = {
//                            "待支付", "已支付", "待派单",
//                            "已分配工单", "工单已完成",
//                            "待评价", "已完成", "已取消"
//                    })
//            @Pattern(regexp = "待支付|已支付|待派单|已分配工单|工单已完成|待评价|已完成|已取消",
//                    message = "订单状态必须是固定值之一")
//            String status,

            @Schema(description = "省/自治区/直辖市")
            String province,

            @Schema(description = "地级市/自治州；直辖市可留空或与省相同")
            String city,

            @Schema(description = "区/县")
            String district,

            @Schema(description = "小区/写字楼/门牌号等")
            String detailAddress
    ){}


}
