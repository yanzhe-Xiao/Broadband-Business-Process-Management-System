package com.xyz.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xyz.workflow.Ticket;
import com.xyz.service.TicketService;
import com.xyz.mapper.TicketMapper;
import org.springframework.stereotype.Service;

/**
* @author X
* @description 针对表【TICKET(工单表，用于管理订单相关的具体工作任务)】的数据库操作Service实现
* @createDate 2025-09-04 14:46:09
*/
@Service
public class TicketServiceImpl extends ServiceImpl<TicketMapper, Ticket>
    implements TicketService{

}




