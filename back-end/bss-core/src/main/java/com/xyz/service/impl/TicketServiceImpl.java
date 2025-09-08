package com.xyz.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xyz.ticket.Ticket;
import com.xyz.service.TicketService;
import com.xyz.mapper.TicketMapper;
import org.springframework.stereotype.Service;

/**
* @author X
* @description 针对表【TICKET(工单表（派单后生成）)】的数据库操作Service实现
* @createDate 2025-09-08 20:35:59
*/
@Service
public class TicketServiceImpl extends ServiceImpl<TicketMapper, Ticket>
    implements TicketService{

}




