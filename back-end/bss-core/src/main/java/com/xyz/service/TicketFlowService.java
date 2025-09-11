package com.xyz.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xyz.constraints.OrderStatuses;
import com.xyz.constraints.TicketEventCodes;
import com.xyz.constraints.TicketStatuses;
import com.xyz.mapper.OrdersMapper;
import com.xyz.mapper.TicketEventImageUrlMapper;
import com.xyz.mapper.TicketMapper;
import com.xyz.orders.Orders;
import com.xyz.ticket.Ticket;
import com.xyz.ticket.TicketEvent;
import com.xyz.ticket.TicketEventImageUrl;
import com.xyz.user.EngineerStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
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
    TaskScheduler taskScheduler;
    @Autowired
    ImageStorageService imageStorageService;
    @Autowired
    TicketEventImageUrlMapper ticketEventImageUrlMapper;
    @Transactional(rollbackFor = Exception.class)
    public void markArrived(Long ticketId, String note, Long userId,List<String> base64) {
        Ticket t = ticketMapper.selectOneByOrderId(ticketId);
        assertIsTicketValiad(t,userId,null);

        // 事件
        TicketEvent ticketEvent = TicketEvent.builder()
                .ticketId(t.getId())
                .eventCode(TicketEventCodes.SITE_SURVEY.getDescription())
                .note(note)
                .happenedAt(new Date())
                .build();

        ticketEventService.save(ticketEvent);
        Long ticketEventId = ticketEvent.getId(); // 获取保存后的ID

        // 状态->进行中
        ticketMapper.update(null, Wrappers.<Ticket>update()
                .eq("id", ticketId)
                .set("status", TicketStatuses.IN_PROGRESS));
        base64.forEach(base -> insertImageUrl(base,ticketEventId));
//        insertImageUrl(base64,ticketEventId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void markCompleted(Long ticketId, String note,Long userId,List<String> base64) {
        Ticket t = ticketMapper.selectOneByOrderId(ticketId);
        assertIsTicketValiad(t,userId,TicketEventCodes.CUSTOMER_SIGNATURE.getPrevious().getDescription());


        // 事件
        TicketEvent ticketEvent = TicketEvent.builder()
                .ticketId(t.getId())
                .eventCode(TicketEventCodes.CUSTOMER_SIGNATURE.getDescription())
                .note(note)
                .happenedAt(new Date())
                .build();

        ticketEventService.save(ticketEvent);
        Long ticketEventId = ticketEvent.getId(); // 获取保存后的ID

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

        base64.forEach(base -> insertImageUrl(base,ticketEventId));


        // ===== 事务提交后 1 分钟把订单置为 待评价 =====
        final Long orderId = t.getOrderId();
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override public void afterCommit() {
                        taskScheduler.schedule(
                                () -> ordersMapper.update(null, Wrappers.<Orders>update()
                                        .eq("id", orderId)
                                        .eq("status", OrderStatuses.WORK_DONE) // 守护条件：只在仍为“工单已完成”时转“待评价”
                                        .set("status", OrderStatuses.TO_REVIEW)),
                                Date.from(Instant.now().plus(1, ChronoUnit.MINUTES))
                        );
                    }
                }
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public void markOtherStatus(Long ticketId, String note,Long userId,List<String> base64, TicketEventCodes status){
        Ticket t = ticketMapper.selectOneByOrderId(ticketId);
        assertIsTicketValiad(t,userId,status.getPrevious().getDescription());

        // 事件
        TicketEvent ticketEvent = TicketEvent.builder()
                .ticketId(t.getId())
                .eventCode(status.getDescription())
                .note(note)
                .happenedAt(new Date())
                .build();

        ticketEventService.save(ticketEvent);
        Long ticketEventId = ticketEvent.getId(); // 获取保存后的ID
        base64.forEach(base -> insertImageUrl(base,ticketEventId));
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelTicket(Long ticketId, String reason,Long userId) {
        Ticket t = ticketMapper.selectById(ticketId);
        if (t == null) throw new IllegalArgumentException("操作失败，为查询到相应的工单");
        if(!Objects.equals(t.getAssigneeId(), userId)){
            throw new SecurityException("无权限操作此工单");
        }

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

    //TODO 根据Status 和 ticketid查找ticketEvent EVENT_CODE NODE HAPPENED_AT，然后根据ticketEventid去ticketEventImageUrl表里找到url

    private void assertIsTicketValiad(Ticket t, Long userId,String lastStatus){
        if (t == null) throw new IllegalArgumentException("操作失败，未查询到相应的工单");
        if(!Objects.equals(t.getAssigneeId(), userId)){
            throw new SecurityException("无权限操作此工单");
        }
        if(lastStatus == null) lastStatus = TicketStatuses.ASSIGNED;
        String newestStatus = ticketEventService.getByNewestStatus(t.getId());
        if(newestStatus == null) newestStatus = t.getStatus();
        if(!lastStatus.equals(newestStatus)){
            throw new IllegalArgumentException("不能跳状态进行订单流程");
        }
    }

    private void insertImageUrl(String base64,Long ticketEventId){
        String url = imageStorageService.saveBase64Image(base64);
        TicketEventImageUrl ticketEventImageUrl = new TicketEventImageUrl();
//        ticketEventImageUrl.setId(0L);
        ticketEventImageUrl.setTicketEventId(ticketEventId);
        ticketEventImageUrl.setImageUrl(url);

        ticketEventImageUrlMapper.insert(ticketEventImageUrl);
    }
}
