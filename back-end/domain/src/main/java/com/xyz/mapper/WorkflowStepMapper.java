package com.xyz.mapper;

import com.xyz.workflow.WorkflowStep;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author X
* @description 针对表【WORKFLOW_STEP(工作流步骤表，记录工作流中每个具体步骤的执行情况)】的数据库操作Mapper
* @createDate 2025-09-04 14:45:56
* @Entity com.xyz.workflow.WorkflowStep
*/
@Mapper
public interface WorkflowStepMapper extends BaseMapper<WorkflowStep> {

}




