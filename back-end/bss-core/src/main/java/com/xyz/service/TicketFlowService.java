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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TicketFlowService {
    @Autowired
    TicketMapper ticketMapper;
    @Autowired
    TicketEventService ticketEventService;
    @Autowired
    OrdersMapper ordersMapper;
    @Autowired
    EngineerStatusService engineerStatusService;
    @Autowired
    private TaskScheduler taskScheduler;
    @Transactional(rollbackFor = Exception.class)
    public void markArrived(Long ticketId, String note, Long userId) {
        Ticket t = ticketMapper.selectById(ticketId);
        assertIsTicketValiad(t,userId);
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
                .set("status", TicketStatuses.IN_PROGRESS));
    }

    @Transactional(rollbackFor = Exception.class)
    public void markCompleted(Long ticketId, String note,Long userId) {
        Ticket t = ticketMapper.selectById(ticketId);
        assertIsTicketValiad(t,userId);

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
                .set("completed_at", new Date()));
//                .set("updated_at", new Date()));

        // 订单->工单已完成（后续你可在评价完成后置为“已完成”）
        ordersMapper.update(null, Wrappers.<Orders>update()
                .eq("id", t.getOrderId())
                .set("status", OrderStatuses.WORK_DONE));
//                .set("updated_at", new Date()));

        // 工程师活跃工单数 -1（>=0 防御）
        if (t.getAssigneeId() != null) {
            engineerStatusService.update(
                    Wrappers.<EngineerStatus>update()
                            .eq("user_id", t.getAssigneeId())
                            .setSql("active_ticket_cnt = CASE WHEN active_ticket_cnt > 0 THEN active_ticket_cnt - 1 " +
                                    "ELSE 0 END"));
//                            .set("updated_at", new Date()));
        }

        // ===== 事务提交后 1 分钟把订单置为 待评价 =====
        final Long orderId = t.getOrderId();
        org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                new org.springframework.transaction.support.TransactionSynchronization() {
                    @Override public void afterCommit() {
                        taskScheduler.schedule(
                                () -> ordersMapper.update(null, Wrappers.<Orders>update()
                                        .eq("id", orderId)
                                        .eq("status", OrderStatuses.WORK_DONE) // 守护条件：只在仍为“工单已完成”时转“待评价”
                                        .set("status", OrderStatuses.TO_REVIEW)),
                                java.util.Date.from(java.time.Instant.now().plus(1, java.time.temporal.ChronoUnit.MINUTES))
                        );
                    }
                }
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelTicket(Long ticketId, String reason,Long userId) {
        Ticket t = ticketMapper.selectById(ticketId);
        assertIsTicketValiad(t,userId);


        // 工单->已取消
        ticketMapper.update(null, Wrappers.<Ticket>update()
                .eq("id", ticketId)
                .set("status", TicketStatuses.CANCELED));
//                .set("updated_at", new Date()));
        // ordersMapper.update(...)

        // 工程师活跃工单数 -1
        if (t.getAssigneeId() != null) {
            engineerStatusService.update(
                    Wrappers.<EngineerStatus>update()
                            .eq("user_id", t.getAssigneeId())
                            .setSql("active_ticket_cnt = CASE WHEN active_ticket_cnt > 0 THEN active_ticket_cnt - 1 " +
                                    "ELSE 0 END"));
//                            .set("updated_at", new Date()));
        }
    }

    private static void assertIsTicketValiad(Ticket t, Long userId){
        if (t == null) throw new IllegalArgumentException("操作失败，为查询到相应的工单");
        if(!Objects.equals(t.getAssigneeId(), userId)){
            throw new SecurityException("无权限操作此工单");
        }
    }
}
