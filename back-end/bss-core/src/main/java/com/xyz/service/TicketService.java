package com.xyz.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xyz.ticket.Ticket;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xyz.vo.TicketFlowPageVo;
import com.xyz.vo.TicketPageVO;

/**
* @author X
* @description 针对表【TICKET(工单表（派单后生成）)】的数据库操作Service
* @createDate 2025-09-08 20:35:59
*/
public interface TicketService extends IService<Ticket> {
    IPage<TicketFlowPageVo> pageTicketVO(int current, int size,
                                         String status,
                                         String engineer,
                                         String addressKw);
}
