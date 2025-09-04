package com.xyz.workflow;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 工作流步骤表，记录工作流中每个具体步骤的执行情况
 * @TableName WORKFLOW_STEP
 */
@TableName(value ="WORKFLOW_STEP")
@Data
public class WorkflowStep {
    /**
     * 工作流步骤ID，主键，自动递增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联的工作流实例ID，外键指向 workflow_instance.id
     */
    private Long instanceId;

    /**
     * 步骤代码，如reserve(资源预留)、dispatch(派单)、open(开通)、bill(计费)等
     */
    private String stepCode;

    /**
     * 步骤执行状态，PENDING-待处理，SUCCEEDED-成功，FAILED-失败，COMPENSATED-已补偿
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
     * 步骤执行的输入数据，JSON或其他格式
     */
    private String dataIn;

    /**
     * 步骤执行的输出数据，JSON或其他格式
     */
    private String dataOut;

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
        WorkflowStep other = (WorkflowStep) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getInstanceId() == null ? other.getInstanceId() == null : this.getInstanceId().equals(other.getInstanceId()))
            && (this.getStepCode() == null ? other.getStepCode() == null : this.getStepCode().equals(other.getStepCode()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getInstanceId() == null) ? 0 : getInstanceId().hashCode());
        result = prime * result + ((getStepCode() == null) ? 0 : getStepCode().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
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
        sb.append(", instanceId=").append(instanceId);
        sb.append(", stepCode=").append(stepCode);
        sb.append(", status=").append(status);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", dataIn=").append(dataIn);
        sb.append(", dataOut=").append(dataOut);
        sb.append("]");
        return sb.toString();
    }
}