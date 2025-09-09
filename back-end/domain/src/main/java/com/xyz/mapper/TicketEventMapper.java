package com.xyz.mapper;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import com.xyz.ticket.TicketEvent;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author X
* @description 针对表【TICKET_EVENT(工单流程表（记录关键节点：已到达/已完成），可扩展更多code)】的数据库操作Mapper
* @createDate 2025-09-08 20:35:32
* @Entity com.xyz.ticket.TicketEvent
*/
@Mapper
public interface TicketEventMapper extends BaseMapper<TicketEvent> {
    List<TicketEvent> selectAllByTicketId(@Param("ticketId") Long ticketId);
}




