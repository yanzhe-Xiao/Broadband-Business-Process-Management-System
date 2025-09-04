package com.xyz.orders;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 订单明细表，存储订单中的具体套餐信息
 * @TableName ORDER_ITEM
 */
@TableName(value ="ORDER_ITEM")
@Data
public class OrderItem {
    /**
     * 订单明细ID，主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID，外键关联orders表
     */
    private Long orderId;

    /**
     * 套餐编码，外键关联tariff_plan表
     */
    private String planCode;

    /**
     * 生效的开始时间
     */
    private Date startBillingAt;

    /**
     * 结束的时间
     */
    private Date endBilling;

    /**
     * 数量
     */
    private Integer qty;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

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
        OrderItem other = (OrderItem) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getOrderId() == null ? other.getOrderId() == null : this.getOrderId().equals(other.getOrderId()))
            && (this.getPlanCode() == null ? other.getPlanCode() == null : this.getPlanCode().equals(other.getPlanCode()))
            && (this.getStartBillingAt() == null ? other.getStartBillingAt() == null : this.getStartBillingAt().equals(other.getStartBillingAt()))
            && (this.getEndBilling() == null ? other.getEndBilling() == null : this.getEndBilling().equals(other.getEndBilling()))
            && (this.getQty() == null ? other.getQty() == null : this.getQty().equals(other.getQty()))
            && (this.getPrice() == null ? other.getPrice() == null : this.getPrice().equals(other.getPrice()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getOrderId() == null) ? 0 : getOrderId().hashCode());
        result = prime * result + ((getPlanCode() == null) ? 0 : getPlanCode().hashCode());
        result = prime * result + ((getStartBillingAt() == null) ? 0 : getStartBillingAt().hashCode());
        result = prime * result + ((getEndBilling() == null) ? 0 : getEndBilling().hashCode());
        result = prime * result + ((getQty() == null) ? 0 : getQty().hashCode());
        result = prime * result + ((getPrice() == null) ? 0 : getPrice().hashCode());
        result = prime * result + ((getCreatedAt() == null) ? 0 : getCreatedAt().hashCode());
        result = prime * result + ((getUpdatedAt() == null) ? 0 : getUpdatedAt().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", orderId=").append(orderId);
        sb.append(", planCode=").append(planCode);
        sb.append(", startBillingAt=").append(startBillingAt);
        sb.append(", endBilling=").append(endBilling);
        sb.append(", qty=").append(qty);
        sb.append(", price=").append(price);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append("]");
        return sb.toString();
    }
}