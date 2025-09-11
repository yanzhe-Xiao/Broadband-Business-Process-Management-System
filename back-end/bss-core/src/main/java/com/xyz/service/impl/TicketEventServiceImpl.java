package com.xyz.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xyz.advice.ImageUrlSplicing;
import com.xyz.mapper.TicketEventImageUrlMapper;
import com.xyz.mapper.TicketMapper;
import com.xyz.ticket.Ticket;
import com.xyz.ticket.TicketEvent;
import com.xyz.service.TicketEventService;
import com.xyz.mapper.TicketEventMapper;
import com.xyz.ticket.TicketEventImageUrl;
import com.xyz.vo.TicketEventDetailFlowVO;
import com.xyz.vo.TicketEventDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author X
* @description 针对表【TICKET_EVENT(工单流程表（记录关键节点：已到达/已完成），可扩展更多code)】的数据库操作Service实现
* @createDate 2025-09-08 20:35:32
*/
@Service
public class TicketEventServiceImpl extends ServiceImpl<TicketEventMapper, TicketEvent>
    implements TicketEventService{

    @Autowired
    TicketEventMapper ticketEventMapper;
    @Autowired
    TicketMapper ticketMapper;
    @Autowired
    TicketEventImageUrlMapper ticketEventImageUrlMapper;


    @Override
    public String getByNewestStatus(Long id) {
        List<TicketEvent> ticketEvents = ticketEventMapper.selectAllByTicketId(id);
        if(!ticketEvents.isEmpty()){
            TicketEvent ticketEvent = ticketEvents.get(0);
            return ticketEvent.getEventCode();
        }else{
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TicketEventDetailVO queryEventDetail(Long ticketId, String eventCode) {
        if (ticketId == null) {
            throw new IllegalArgumentException("ticketId 不能为空");
        }
        if (eventCode == null || eventCode.isBlank()) {
            throw new IllegalArgumentException("eventCode 不能为空");
        }

        // 1) 唯一事件（ticketId + eventCode 唯一）
        TicketEvent ev = ticketEventMapper.selectOne(
                Wrappers.<TicketEvent>lambdaQuery()
                        .eq(TicketEvent::getTicketId, ticketId)
                        .eq(TicketEvent::getEventCode, eventCode)
        );
        if (ev == null) {
            // 找不到就返回空对象或抛异常，按你的风格二选一：
            // throw new IllegalArgumentException("事件不存在: ticketId=" + ticketId + ", eventCode=" + eventCode);
            return TicketEventDetailVO.builder()
                    .ticketId(ticketId)
                    .eventCode(eventCode)
                    .note(null)
                    .happenedAt(null)
                    .imageUrls(java.util.List.of())
                    .build();
        }

        // 2) 该事件的图片 URL 列表，并用 ImageUrlSplicing 包装
        java.util.List<TicketEventImageUrl> urls = ticketEventImageUrlMapper.selectList(
                Wrappers.<TicketEventImageUrl>lambdaQuery()
                        .eq(TicketEventImageUrl::getTicketEventId, ev.getId())
        );
        java.util.List<String> imageUrls = urls.stream()
                .map(u -> ImageUrlSplicing.splicingURL(u.getImageUrl()))
                .toList();

        // 3) 组装返回
        return TicketEventDetailVO.builder()
                .ticketId(ticketId)
                .eventCode(ev.getEventCode())
                .note(ev.getNote())
                .happenedAt(ev.getHappenedAt())
                .imageUrls(imageUrls)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketEventDetailFlowVO> queryEventDetail(Long ticketId) {
        if (ticketId == null) {
            throw new IllegalArgumentException("ticketId 不能为空");
        }

        // 1) 查该工单的所有事件（按时间升序）
        List<TicketEvent> events = ticketEventMapper.selectList(
                Wrappers.<TicketEvent>lambdaQuery()
                        .eq(TicketEvent::getTicketId, ticketId)
                        .orderByAsc(TicketEvent::getHappenedAt)
        );
        if (events == null || events.isEmpty()) {
            return List.of();
        }

        // 2) 批量查图片URL，并按 eventId 分组；取出时顺便做 URL 拼接
        List<Long> eventIds = events.stream().map(TicketEvent::getId).toList();

        List<TicketEventImageUrl> imgs = eventIds.isEmpty()
                ? List.of()
                : ticketEventImageUrlMapper.selectList(
                Wrappers.<TicketEventImageUrl>lambdaQuery()
                        .in(TicketEventImageUrl::getTicketEventId, eventIds)
        );

        Map<Long, List<String>> imageUrlMap = imgs.stream()
                .collect(Collectors.groupingBy(
                        TicketEventImageUrl::getTicketEventId,
                        Collectors.mapping(
                                u -> ImageUrlSplicing.splicingURL(u.getImageUrl()),
                                Collectors.toList()
                        )
                ));

        // 3) 组装 VO 列表
        return events.stream()
                .map(e -> TicketEventDetailFlowVO.builder()
                        .eventCode(e.getEventCode())
                        .note(e.getNote())
                        .happenedAt(e.getHappenedAt())
                        .imageUrls(imageUrlMap.getOrDefault(e.getId(), List.of()))
                        .build())
                .toList();
    }



}




