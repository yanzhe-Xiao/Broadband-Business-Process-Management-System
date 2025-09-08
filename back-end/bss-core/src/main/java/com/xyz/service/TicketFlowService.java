package com.xyz.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xyz.constraints.OrderStatuses;
import com.xyz.constraints.TicketEventCodes;
import com.xyz.constraints.TicketStatuses;
import com.xyz.mapper.OrdersMapper;
import com.xyz.mapper.TicketMapper;
import com.xyz.orders.Orders;
import com.xyz.ticket.Ticket;
import com.xyz.ticket.TicketEvent;
import com.xyz.user.EngineerStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class TicketFlowService {

    private final TicketMapper ticketMapper;
    private final TicketEventService ticketEventService;
    private final OrdersMapper ordersMapper;
    private final EngineerStatusService engineerStatusService;

    @Transactional(rollbackFor = Exception.class)
    public void markArrived(Long ticketId, String note) {
        Ticket t = ticketMapper.selectById(ticketId);
        if (t == null) return;

        // 事件
        ticketEventService.save(TicketEvent.builder()
                        .ticketId(ticketId)
                        .eventCode(TicketEventCodes.ARRIVED)
                        .note(note)
                        .happenedAt(new Date())
                .build()
        );

        // 状态->进行中
        ticketMapper.update(null, Wrappers.<Ticket>update()
                .eq("id", ticketId)
                .set("status", TicketStatuses.IN_PROGRESS)
                .set("updated_at", new Date()));
    }

    @Transactional(rollbackFor = Exception.class)
    public void markCompleted(Long ticketId, String note) {
        Ticket t = ticketMapper.selectById(ticketId);
        if (t == null) return;

        // 事件
        ticketEventService.save(TicketEvent.builder()
                .ticketId(ticketId)
                .eventCode(TicketEventCodes.COMPLETED)
                .note(note)
                .happenedAt(new Date())
                .build()
        );

        // 工单->已完成
        ticketMapper.update(null, Wrappers.<Ticket>update()
                .eq("id", ticketId)
                .set("status", TicketStatuses.DONE)
                .set("completed_at", new Date())
                .set("updated_at", new Date()));

        // 订单->工单已完成（后续你可在评价完成后置为“已完成”）
        ordersMapper.update(null, Wrappers.<Orders>update()
                .eq("id", t.getOrderId())
                .set("status", OrderStatuses.WORK_DONE)
                .set("updated_at", new Date()));

        // 工程师活跃工单数 -1（>=0 防御）
        if (t.getAssigneeId() != null) {
            engineerStatusService.update(
                    Wrappers.<EngineerStatus>update()
                            .eq("user_id", t.getAssigneeId())
                            .setSql("active_ticket_cnt = CASE WHEN active_ticket_cnt > 0 THEN active_ticket_cnt - 1 ELSE 0 END")
                            .set("updated_at", new Date()));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelTicket(Long ticketId, String reason) {
        Ticket t = ticketMapper.selectById(ticketId);
        if (t == null) return;

        // 工单->已取消
        ticketMapper.update(null, Wrappers.<Ticket>update()
                .eq("id", ticketId)
                .set("status", TicketStatuses.CANCELED)
                .set("updated_at", new Date()));

        // 订单（看你策略，可回退到 “待派单” / “已取消”，此处不强制）
        // ordersMapper.update(...)

        // 工程师活跃工单数 -1
        if (t.getAssigneeId() != null) {
            engineerStatusService.update(
                    Wrappers.<EngineerStatus>update()
                            .eq("user_id", t.getAssigneeId())
                            .setSql("active_ticket_cnt = CASE WHEN active_ticket_cnt > 0 THEN active_ticket_cnt - 1 ELSE 0 END")
                            .set("updated_at", new Date()));
        }
    }
}
