package com.xyz.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xyz.workflow.WorkflowStep;
import com.xyz.service.WorkflowStepService;
import com.xyz.mapper.WorkflowStepMapper;
import org.springframework.stereotype.Service;

/**
* @author X
* @description 针对表【WORKFLOW_STEP(工作流步骤表，记录工作流中每个具体步骤的执行情况)】的数据库操作Service实现
* @createDate 2025-09-04 14:45:56
*/
@Service
public class WorkflowStepServiceImpl extends ServiceImpl<WorkflowStepMapper, WorkflowStep>
    implements WorkflowStepService{

}




