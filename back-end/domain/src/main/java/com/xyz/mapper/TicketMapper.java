package com.xyz.mapper;

import com.xyz.ticket.Ticket;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author X
* @description 针对表【TICKET(工单表（派单后生成）)】的数据库操作Mapper
* @createDate 2025-09-08 20:35:59
* @Entity com.xyz.ticket.Ticket
*/
@Mapper
public interface TicketMapper extends BaseMapper<Ticket> {

}




