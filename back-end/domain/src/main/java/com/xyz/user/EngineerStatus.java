package com.xyz.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName ENGINEER_STATUS
 */
@TableName(value ="ENGINEER_STATUS")
@Data
public class EngineerStatus {
    /**
     * 
     */
    @TableId
    private Long userId;

    /**
     * 
     */
    private Integer isIdle;

    /**
     * 
     */
    private Integer activeTicketCnt;

    /**
     * 
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
        EngineerStatus other = (EngineerStatus) that;
        return (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getIsIdle() == null ? other.getIsIdle() == null : this.getIsIdle().equals(other.getIsIdle()))
            && (this.getActiveTicketCnt() == null ? other.getActiveTicketCnt() == null : this.getActiveTicketCnt().equals(other.getActiveTicketCnt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getIsIdle() == null) ? 0 : getIsIdle().hashCode());
        result = prime * result + ((getActiveTicketCnt() == null) ? 0 : getActiveTicketCnt().hashCode());
        result = prime * result + ((getUpdatedAt() == null) ? 0 : getUpdatedAt().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", userId=").append(userId);
        sb.append(", isIdle=").append(isIdle);
        sb.append(", activeTicketCnt=").append(activeTicketCnt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append("]");
        return sb.toString();
    }
}