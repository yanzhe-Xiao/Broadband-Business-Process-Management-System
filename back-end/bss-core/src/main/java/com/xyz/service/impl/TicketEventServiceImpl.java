package com.xyz.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xyz.ticket.TicketEvent;
import com.xyz.service.TicketEventService;
import com.xyz.mapper.TicketEventMapper;
import org.springframework.stereotype.Service;

/**
* @author X
* @description 针对表【TICKET_EVENT(工单流程表（记录关键节点：已到达/已完成），可扩展更多code)】的数据库操作Service实现
* @createDate 2025-09-08 20:35:32
*/
@Service
public class TicketEventServiceImpl extends ServiceImpl<TicketEventMapper, TicketEvent>
    implements TicketEventService{

}




