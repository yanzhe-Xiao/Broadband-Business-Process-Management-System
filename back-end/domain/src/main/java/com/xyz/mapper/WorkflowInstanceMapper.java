package com.xyz.mapper;

import com.xyz.workflow.WorkflowInstance;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author X
* @description 针对表【WORKFLOW_INSTANCE(工作流实例表，用于跟踪订单相关的业务流程实例)】的数据库操作Mapper
* @createDate 2025-09-04 14:45:52
* @Entity com.xyz.workflow.WorkflowInstance
*/
@Mapper
public interface WorkflowInstanceMapper extends BaseMapper<WorkflowInstance> {

}




