package com.xyz.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xyz.mapper.AppUserMapper;
import com.xyz.mapper.RoleMapper;
import com.xyz.service.TicketEventService;
import com.xyz.service.TicketFlowService;
import com.xyz.ticket.Ticket;
import com.xyz.service.TicketService;
import com.xyz.mapper.TicketMapper;
import com.xyz.vo.TicketFlowPageVo;
import com.xyz.vo.TicketPageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
* @author X
* @description 针对表【TICKET(工单表（派单后生成）)】的数据库操作Service实现
* @createDate 2025-09-08 20:35:59
*/
@Service
public class TicketServiceImpl extends ServiceImpl<TicketMapper, Ticket>
    implements TicketService{

    @Autowired
    TicketMapper ticketMapper;
    @Autowired
    TicketEventService ticketEventService;
    @Autowired
    RoleMapper roleMapper;
    @Autowired
    AppUserMapper appUserMapper;

    @Override
    public IPage<TicketFlowPageVo> pageTicketVO(int current, int size,
                                                String status,
                                                String engineer,
                                                String addressKw) {
        IPage<TicketPageVO> page = new Page<>(Math.max(1, current), Math.max(1, size));
        if(Objects.equals(status, "all")){
            status = "";
        }
        String roleName = roleMapper.selectRoleNameByUserId(appUserMapper.selectIdByUsername(engineer));
        if(Objects.equals(roleName, "平台管理员")){
            engineer = "";
        }
        IPage<TicketPageVO> ticketPageVOIPage = ticketMapper.selectTicketPage(page, status, engineer, addressKw);
        IPage<TicketFlowPageVo> convert = ticketPageVOIPage.convert(ticketPageVO ->
                TicketFlowPageVo.toFlowPageVo(ticketPageVO, ticketEventService.queryEventDetail(ticketPageVO.id())
                )
        );
        return convert;
    }
}




