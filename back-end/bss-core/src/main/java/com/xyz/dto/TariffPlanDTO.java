package com.xyz.dto;

import com.xyz.orders.TariffPlan;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

/**
 * <p>Package Name: com.xyz.dto </p>
 * <p>Description:  </p>
 * <p>Create Time: 2025/9/5 </p>
 *
 * @author <a href="https://github.com/yanzhe-xiao">YANZHE XIAO</a>
 * @version 1.0
 * @since
 */
public class TariffPlanDTO {
    /**
     * 套餐计划 DTO
     */
    @Schema(name = "TariffPlanAvaliable", description = "套餐计划传输对象")
    public record TariffPlanAvaliable(

            @Schema(description = "套餐编码")
            String planCode,

            @Schema(description = "套餐名称")
            String name,

            @Schema(description = "月费")
            BigDecimal monthlyFee,

            @Schema(description = "年费")
            BigDecimal yearlyFee,

            @Schema(description = "永久费")
            BigDecimal foreverFee,

            @Schema(description = "套餐有效期（月）")
            Integer planPeriod,

            @Schema(description = "折扣率（100表示无折扣）")
            BigDecimal discount,

            @Schema(description = "套餐的数量")
            Integer qty,

            @Schema(description = "状态", example = "ACTIVE/INACTIVE")
            @Pattern(
                    regexp = "^(ACTIVE/INACTIVE)$",
                    message = "套餐状态只能是ACTIVE/INACTIVE中的一个"
            )
            String status,

            @Schema(description = "是否需要IP", example = "0-否, 1-是")
            @Pattern(
                    regexp = "^(0/1)$",
                    message = "isIp只能是0/1"
            )
            Integer isIp,

            @Schema(description = "设备资源sn")
            String deviceSN,

            @Schema(description = "套餐带宽（MB）")
            Integer bandwidth,

            @Schema(description = "套餐描述")
            String description,

            @Schema(description = "所需设备数量")
            Integer deviceQty


    ) {
        public static TariffPlan toEntity(TariffPlanDTO.TariffPlanAvaliable dto) {
            TariffPlan entity = new TariffPlan();
            entity.setPlanCode(dto.planCode());
            entity.setName(dto.name());
            entity.setMonthlyFee(dto.monthlyFee());
            entity.setYearlyFee(dto.yearlyFee());
            entity.setForeverFee(dto.foreverFee());
            entity.setPlanPeriod(dto.planPeriod());
            entity.setDiscount(dto.discount());
            entity.setStatus(dto.status());
            entity.setIsIp(dto.isIp());
            entity.setDeviceSn(dto.deviceSN());
            entity.setBandwith(dto.bandwidth());
            entity.setDescription(dto.description());
            entity.setDeviceQty(dto.deviceQty());
            return entity;
        }
    }

}
