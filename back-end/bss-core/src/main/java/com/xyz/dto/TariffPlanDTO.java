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

            @Schema(description = "折扣率（100表示无折扣）")
            BigDecimal discount,

            @Schema(description = "套餐的数量")
            Integer qty,

            @Schema(description = "状态", example = "ACTIVE/INACTIVE")
            @Pattern(
                    regexp = "^(ACTIVE|INACTIVE)$",
                    message = "套餐状态只能是ACTIVE/INACTIVE中的一个"
            )
            String status,

            @Schema(description = "是否需要IP", example = "0-否, 1-是")
            @Pattern(
                    regexp = "^(0|1)$",
                    message = "isIp只能是0/1"
            )
            Integer isIp,

            @Schema(description = "套餐带宽（MB）")
            Integer bandwidth,

            @Schema(description = "设备资源sn")
            String deviceSN,

            @Schema(description = "套餐描述")
            String description,

            @Schema(description = "所需设备数量")
            Integer deviceQty,

            @Schema(description = "套餐封面图（Base64，支持 data URL）",
                    example = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...")
                    String imageBase64


    ) {
        public static TariffPlan toEntity(TariffPlanDTO.TariffPlanAvaliable dto,
                                          java.util.function.Function<String, String> saveBase64Fn) {
            TariffPlan entity = new TariffPlan();
            entity.setPlanCode(dto.planCode());
            entity.setName(dto.name());
            entity.setMonthlyFee(dto.monthlyFee());
            entity.setYearlyFee(dto.yearlyFee());
            entity.setForeverFee(dto.foreverFee());
            entity.setDiscount(dto.discount());
            entity.setStatus(dto.status());
            entity.setQty(dto.qty());
            entity.setIsIp(dto.isIp());
            entity.setBandwith(dto.bandwidth());
            entity.setDescription(dto.description());
            entity.setDeviceQty(dto.deviceQty());
            // ⭐ 将 base64 存盘并得到可访问 URL，落到 entity.imageUrl
            if (dto.imageBase64() != null && !dto.imageBase64().isBlank()) {
                String url = saveBase64Fn.apply(dto.imageBase64());
                entity.setImageUrl(url);
            }
            return entity;
        }
    }

    @Schema(name = "TariffPlanSearchCriteria", description = "套餐计划搜索条件传输对象")
    public record TariffPlanSearchCriteria(

            @Schema(description = "当前页码")
            int current,

            @Schema(description = "关键字（用于匹配套餐编码或名称）", example = "111123456")
            String keyword,

            @Schema(description = "最大价格", example = "2")
            BigDecimal maxPrice,

            @Schema(description = "最小价格", example = "1")
            BigDecimal minPrice,

            @Schema(description = "是否仅显示有库存的套餐", example = "true")
            Boolean onlyInStock,

            @Schema(description = "用户角色名", example = "客户")
            String roleName,

            @Schema(description = "每页显示条数", example = "8")
            Integer size,

            @Schema(description = "排序方式", example = "priceUp")
            @Pattern(
                    regexp = "^(priceUp|priceDown|rating)$",
                    message = "排序方式只能是priceUp/priceDown/rating"
            )
            String sort,

            @Schema(description = "用户名", example = "username")
            String username,

            @Schema(description = "价格分类", example = "month")
            @Pattern(
                    regexp = "^(month|year|forever)$",
                    message = "排序方式只能是month|year|forever"
            )
            String priceSort

    ) {}


}
