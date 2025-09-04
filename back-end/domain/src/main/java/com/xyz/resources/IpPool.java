package com.xyz.resources;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * IP地址资源池表，用于管理IP地址的分配和使用
 * @TableName IP_POOL
 */
@TableName(value ="IP_POOL")
@Data
public class IpPool {
    /**
     * IP资源ID，主键，自动递增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * IP地址
     */
    private String ip;

    /**
     * IP地址状态，FREE-空闲，ASSIGNED-已分配
     */
    private String status;

    /**
     * 关联的订单ID，外键指向orders.id
     */
    private Long orderId;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    /**
     * ip的带宽，默认值为1000MB，单位是MB
     */
    private Integer ipBandwidth;

    /**
     * 
     */
    private Integer avaliableBandwidth;

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
        IpPool other = (IpPool) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getIp() == null ? other.getIp() == null : this.getIp().equals(other.getIp()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getOrderId() == null ? other.getOrderId() == null : this.getOrderId().equals(other.getOrderId()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()))
            && (this.getIpBandwidth() == null ? other.getIpBandwidth() == null : this.getIpBandwidth().equals(other.getIpBandwidth()))
            && (this.getAvaliableBandwidth() == null ? other.getAvaliableBandwidth() == null : this.getAvaliableBandwidth().equals(other.getAvaliableBandwidth()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getIp() == null) ? 0 : getIp().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getOrderId() == null) ? 0 : getOrderId().hashCode());
        result = prime * result + ((getCreatedAt() == null) ? 0 : getCreatedAt().hashCode());
        result = prime * result + ((getUpdatedAt() == null) ? 0 : getUpdatedAt().hashCode());
        result = prime * result + ((getIpBandwidth() == null) ? 0 : getIpBandwidth().hashCode());
        result = prime * result + ((getAvaliableBandwidth() == null) ? 0 : getAvaliableBandwidth().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", ip=").append(ip);
        sb.append(", status=").append(status);
        sb.append(", orderId=").append(orderId);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", ipBandwidth=").append(ipBandwidth);
        sb.append(", avaliableBandwidth=").append(avaliableBandwidth);
        sb.append("]");
        return sb.toString();
    }
}