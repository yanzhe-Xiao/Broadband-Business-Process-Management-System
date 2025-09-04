package com.xyz.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xyz.workflow.WorkflowInstance;
import com.xyz.service.WorkflowInstanceService;
import com.xyz.mapper.WorkflowInstanceMapper;
import org.springframework.stereotype.Service;

/**
* @author X
* @description 针对表【WORKFLOW_INSTANCE(工作流实例表，用于跟踪订单相关的业务流程实例)】的数据库操作Service实现
* @createDate 2025-09-04 14:45:52
*/
@Service
public class WorkflowInstanceServiceImpl extends ServiceImpl<WorkflowInstanceMapper, WorkflowInstance>
    implements WorkflowInstanceService{

}




