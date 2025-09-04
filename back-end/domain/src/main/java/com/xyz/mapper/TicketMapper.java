package com.xyz.mapper;

import com.xyz.workflow.Ticket;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author X
* @description 针对表【TICKET(工单表，用于管理订单相关的具体工作任务)】的数据库操作Mapper
* @createDate 2025-09-04 14:46:09
* @Entity com.xyz.workflow.Ticket
*/
@Mapper
public interface TicketMapper extends BaseMapper<Ticket> {

}




