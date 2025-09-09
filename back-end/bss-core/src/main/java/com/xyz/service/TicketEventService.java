package com.xyz.service;

import com.xyz.ticket.TicketEvent;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xyz.vo.TicketEventDetailVO;

import java.util.List;

/**
* @author X
* @description 针对表【TICKET_EVENT(工单流程表（记录关键节点：已到达/已完成），可扩展更多code)】的数据库操作Service
* @createDate 2025-09-08 20:35:32
*/
public interface TicketEventService extends IService<TicketEvent> {
    public String getByNewestStatus(Long id);

    /**
     * 按 ticketId + eventCode 查询唯一事件，返回 note + imageUrls + happenedAt
     */
    TicketEventDetailVO queryEventDetail(Long ticketId, String eventCode);
}
