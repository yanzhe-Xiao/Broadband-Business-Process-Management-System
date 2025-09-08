package com.xyz.ticket;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import lombok.Builder;
import lombok.Data;

/**
 * 工单流程表（记录关键节点：已到达/已完成），可扩展更多code
 * @TableName TICKET_EVENT
 */
@TableName(value ="TICKET_EVENT")
@Data
@Builder
public class TicketEvent {
    /**
     * 事件唯一标识
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联的工单ID
     */
    private Long ticketId;

    /**
     * 事件代码: ARRIVED / COMPLETED
     */
    private String eventCode;

    /**
     * 事件备注
     */
    private String note;

    /**
     * 事件发生时间
     */
    private Date happenedAt;

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
        TicketEvent other = (TicketEvent) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getTicketId() == null ? other.getTicketId() == null : this.getTicketId().equals(other.getTicketId()))
            && (this.getEventCode() == null ? other.getEventCode() == null : this.getEventCode().equals(other.getEventCode()))
            && (this.getNote() == null ? other.getNote() == null : this.getNote().equals(other.getNote()))
            && (this.getHappenedAt() == null ? other.getHappenedAt() == null : this.getHappenedAt().equals(other.getHappenedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getTicketId() == null) ? 0 : getTicketId().hashCode());
        result = prime * result + ((getEventCode() == null) ? 0 : getEventCode().hashCode());
        result = prime * result + ((getNote() == null) ? 0 : getNote().hashCode());
        result = prime * result + ((getHappenedAt() == null) ? 0 : getHappenedAt().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", ticketId=").append(ticketId);
        sb.append(", eventCode=").append(eventCode);
        sb.append(", note=").append(note);
        sb.append(", happenedAt=").append(happenedAt);
        sb.append("]");
        return sb.toString();
    }
}