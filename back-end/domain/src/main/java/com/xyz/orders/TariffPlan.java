package com.xyz.orders;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 套餐资费表，存储宽带套餐信息
 * @TableName TARIFF_PLAN
 */
@TableName(value ="TARIFF_PLAN")
@Data
public class TariffPlan {
    /**
     * 套餐编码，主键
     */
    @TableId
    private String planCode;

    /**
     * 套餐名称
     */
    private String name;

    /**
     * 月费
     */
    private BigDecimal monthlyFee;

    /**
     * 年费
     */
    private BigDecimal yearlyFee;

    /**
     * 永久费
     */
    private BigDecimal foreverFee;

    /**
     * 套餐有效期
     */
    private Integer planPeriod;

    /**
     * 折扣率，100为无折扣
     */
    private BigDecimal discount;

    /**
     * 状态：ACTIVE-启用，INACTIVE-停用
     */
    private String status;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    /**
     * 
     */
    private Integer isIp;

    /**
     * 
     */
    private Long ipId;

    /**
     * 
     */
    private String deviceSn;

    /**
     * 套餐的带宽
     */
    private Integer bandwith;

    /**
     * 
     */
    private String description;

    /**
     * 
     */
    private Integer deviceQty;

    /**
     * 数量
     */
    private Integer qty;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        TariffPlan other = (TariffPlan) that;
        return (this.getPlanCode() == null ? other.getPlanCode() == null : this.getPlanCode().equals(other.getPlanCode()))
            && (this.getName() == null ? other.getName() == null : this.getName().equals(other.getName()))
            && (this.getMonthlyFee() == null ? other.getMonthlyFee() == null : this.getMonthlyFee().equals(other.getMonthlyFee()))
            && (this.getYearlyFee() == null ? other.getYearlyFee() == null : this.getYearlyFee().equals(other.getYearlyFee()))
            && (this.getForeverFee() == null ? other.getForeverFee() == null : this.getForeverFee().equals(other.getForeverFee()))
            && (this.getPlanPeriod() == null ? other.getPlanPeriod() == null : this.getPlanPeriod().equals(other.getPlanPeriod()))
            && (this.getDiscount() == null ? other.getDiscount() == null : this.getDiscount().equals(other.getDiscount()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()))
            && (this.getIsIp() == null ? other.getIsIp() == null : this.getIsIp().equals(other.getIsIp()))
            && (this.getIpId() == null ? other.getIpId() == null : this.getIpId().equals(other.getIpId()))
            && (this.getDeviceSn() == null ? other.getDeviceSn() == null : this.getDeviceSn().equals(other.getDeviceSn()))
            && (this.getBandwith() == null ? other.getBandwith() == null : this.getBandwith().equals(other.getBandwith()))
            && (this.getDescription() == null ? other.getDescription() == null : this.getDescription().equals(other.getDescription()))
            && (this.getDeviceQty() == null ? other.getDeviceQty() == null : this.getDeviceQty().equals(other.getDeviceQty()))
            && (this.getQty() == null ? other.getQty() == null : this.getQty().equals(other.getQty()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getPlanCode() == null) ? 0 : getPlanCode().hashCode());
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((getMonthlyFee() == null) ? 0 : getMonthlyFee().hashCode());
        result = prime * result + ((getYearlyFee() == null) ? 0 : getYearlyFee().hashCode());
        result = prime * result + ((getForeverFee() == null) ? 0 : getForeverFee().hashCode());
        result = prime * result + ((getPlanPeriod() == null) ? 0 : getPlanPeriod().hashCode());
        result = prime * result + ((getDiscount() == null) ? 0 : getDiscount().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getCreatedAt() == null) ? 0 : getCreatedAt().hashCode());
        result = prime * result + ((getUpdatedAt() == null) ? 0 : getUpdatedAt().hashCode());
        result = prime * result + ((getIsIp() == null) ? 0 : getIsIp().hashCode());
        result = prime * result + ((getIpId() == null) ? 0 : getIpId().hashCode());
        result = prime * result + ((getDeviceSn() == null) ? 0 : getDeviceSn().hashCode());
        result = prime * result + ((getBandwith() == null) ? 0 : getBandwith().hashCode());
        result = prime * result + ((getDescription() == null) ? 0 : getDescription().hashCode());
        result = prime * result + ((getDeviceQty() == null) ? 0 : getDeviceQty().hashCode());
        result = prime * result + ((getQty() == null) ? 0 : getQty().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", planCode=").append(planCode);
        sb.append(", name=").append(name);
        sb.append(", monthlyFee=").append(monthlyFee);
        sb.append(", yearlyFee=").append(yearlyFee);
        sb.append(", foreverFee=").append(foreverFee);
        sb.append(", planPeriod=").append(planPeriod);
        sb.append(", discount=").append(discount);
        sb.append(", status=").append(status);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", isIp=").append(isIp);
        sb.append(", ipId=").append(ipId);
        sb.append(", deviceSn=").append(deviceSn);
        sb.append(", bandwith=").append(bandwith);
        sb.append(", description=").append(description);
        sb.append(", deviceQty=").append(deviceQty);
        sb.append(", qty=").append(qty);
        sb.append("]");
        return sb.toString();
    }
}