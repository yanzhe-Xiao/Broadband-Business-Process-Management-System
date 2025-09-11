package com.xyz.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xyz.ticket.Ticket;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xyz.vo.TicketPageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author X
* @description 针对表【TICKET(工单表（派单后生成）)】的数据库操作Mapper
* @createDate 2025-09-08 20:35:59
* @Entity com.xyz.ticket.Ticket
*/
@Mapper
public interface TicketMapper extends BaseMapper<Ticket> {
    IPage<TicketPageVO> selectTicketPage(
            IPage<?> page,
            @Param("status") String status,          // 可选：按状态筛选
            @Param("engineer") String engineer,      // 可选：工程师用户名/姓名模糊
            @Param("addressKw") String addressKw     // 可选：安装地址关键字
    );

    List<TicketPageVO> exportTicketList( // 需要导出时可复用同一SQL
                                         @Param("status") String status,
                                         @Param("engineer") String engineer,
                                         @Param("addressKw") String addressKw
    );

    Ticket selectOneByOrderId(@Param("orderId") Long orderId);
}




