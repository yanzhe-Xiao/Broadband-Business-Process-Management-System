package com.xyz.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xyz.constraints.OrderStatuses;
import com.xyz.constraints.TicketStatuses;
import com.xyz.mapper.*;
import com.xyz.orders.Orders;
import com.xyz.ticket.Ticket;
import com.xyz.user.EngineerStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderOrchestrationService {

    private final TaskScheduler taskScheduler;
    private final OrdersMapper ordersMapper;
    private final TicketMapper ticketMapper;
    private final TicketService ticketService; // 可直接用 IService
    private final EngineerStatusService engineerStatusService;
    private final AppUserMapper appUserMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;

    // === 在 payOrder() 成功后调用这个方法 ===

    public void onOrderPaid(Long orderId) {
        log.info("订单支付完成，开始调度任务，订单ID: {}", orderId);

        // T+1分钟：置为"待派单"
        taskScheduler.schedule(() -> safeSetOrderPendingDispatch(orderId),
                Date.from(Instant.now().plus(1, ChronoUnit.MINUTES)));
        log.info("已调度订单状态更新任务，订单ID: {}，预计执行时间: {}", orderId,
                Date.from(Instant.now().plus(1, ChronoUnit.MINUTES)));

        // 随机 10~60 分钟后：进行派单
//        int delay = 10 + new Random().nextInt(51); // [10,60]
        int delay = 2 ;
        taskScheduler.schedule(() -> safeDispatch(orderId),
                Date.from(Instant.now().plus(delay, ChronoUnit.MINUTES)));
        log.info("已调度订单派发任务，订单ID: {}，预计执行时间: {}", orderId,
                Date.from(Instant.now().plus(delay, ChronoUnit.MINUTES)));
    }

    private void safeSetOrderPendingDispatch(Long orderId) {
        try {
            ordersMapper.update(null, Wrappers.<Orders>update()
                    .eq("id", orderId)
                    .eq("status", OrderStatuses.PAID)
                    .set("status", OrderStatuses.PENDING_DISPATCH));
        } catch (Exception e) {
            // 可打日志并重试/入队
        }
    }

    private void safeDispatch(Long orderId) {
        try {
            log.info("开始执行派单");
            dispatchOneOrder(orderId);
        } catch (Exception e) {
            throw new IllegalArgumentException("更新失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void dispatchOneOrder(Long orderId) {
        // 只对“待派单”的订单做派单
        Orders order = ordersMapper.selectById(orderId);
        if (order == null || !OrderStatuses.PENDING_DISPATCH.equals(order.getStatus())) {
            return;
        }

        // 选择工程师（必须是 ENGINEER 且 ACTIVE 且 is_idle=1，active_ticket_cnt 最小）
        Long engineerId = pickBalancedIdleEngineer();
        if (engineerId == null) {
            // 没有闲时工程师：可以选择重新排队，或派给最空闲的一位（忽略 is_idle）
            engineerId = pickBalancedEngineerFallback();
            if (engineerId == null) {
                // 放弃或重试
                return;
            }
        }

        // 创建工单：状态“已分配”
        Ticket ticket = new Ticket();
        ticket.setOrderId(orderId);
        ticket.setStatus(TicketStatuses.ASSIGNED);
        ticket.setAssigneeId(engineerId);
        ticket.setDispatchedAt(new Date());
        ticketMapper.insert(ticket);

        // 更新订单为“已分配工单”
        ordersMapper.update(null, Wrappers.<Orders>update()
                .eq("id", orderId)
                .set("status", OrderStatuses.DISPATCHED)
                );

        // 工程师活跃工单数+1（并保持 is_idle=1 或自行策略改为忙碌）
        engineerStatusService.update(
                Wrappers.<EngineerStatus>update()
                        .eq("user_id", engineerId)
                        .setSql("active_ticket_cnt = active_ticket_cnt + 1")
                        );
    }

    // 精确选择“闲时工程师”
    private Long pickBalancedIdleEngineer() {
        // 你可以写一个专门的 Mapper SQL：按 ENGINEER + ACTIVE + is_idle=1 + active_ticket_cnt 升序取 1
        return appUserMapper.selectBalancedIdleEngineerId();
    }

    // 兜底：没有闲时，则取 active_ticket_cnt 最小的工程师
    private Long pickBalancedEngineerFallback() {
        return appUserMapper.selectBalancedEngineerId();
    }
}
