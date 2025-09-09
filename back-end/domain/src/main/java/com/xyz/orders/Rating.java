package com.xyz.orders;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 评价表（每个订单项一条评价，便于逐套餐评分）
 * @TableName RATING
 */
@TableName(value ="RATING")
@Data
public class Rating {
    /**
     * 评价唯一标识
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 套餐code
     */
    private String planCode;

    /**
     * 评价用户ID
     */
    private Long userId;

    /**
     * 评分: 1~5分
     */
    private Integer score;

    /**
     * 评价内容
     */
    private String ratComment;

    /**
     * 评价创建时间
     */
    private Date createdAt;

    /**
     * 
     */
    private Long orderId;

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
        Rating other = (Rating) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getPlanCode() == null ? other.getPlanCode() == null : this.getPlanCode().equals(other.getPlanCode()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getScore() == null ? other.getScore() == null : this.getScore().equals(other.getScore()))
            && (this.getRatComment() == null ? other.getRatComment() == null : this.getRatComment().equals(other.getRatComment()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getOrderId() == null ? other.getOrderId() == null : this.getOrderId().equals(other.getOrderId()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getPlanCode() == null) ? 0 : getPlanCode().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getScore() == null) ? 0 : getScore().hashCode());
        result = prime * result + ((getRatComment() == null) ? 0 : getRatComment().hashCode());
        result = prime * result + ((getCreatedAt() == null) ? 0 : getCreatedAt().hashCode());
        result = prime * result + ((getOrderId() == null) ? 0 : getOrderId().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", planCode=").append(planCode);
        sb.append(", userId=").append(userId);
        sb.append(", score=").append(score);
        sb.append(", ratComment=").append(ratComment);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", orderId=").append(orderId);
        sb.append("]");
        return sb.toString();
    }
}