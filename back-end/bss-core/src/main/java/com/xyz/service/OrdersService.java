package com.xyz.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xyz.dto.OrderDTO;
import com.xyz.orders.Orders;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xyz.vo.orders.OrderVO;

import java.util.List;

/**
* @author X
* @description 针对表【ORDERS(订单表，存储宽带新装/变更/销户等主订单)】的数据库操作Service
* @createDate 2025-09-04 14:45:21
*/
public interface OrdersService extends IService<Orders> {
    public int commitOrder(OrderDTO.OrderAvaliableDTO dto);

    public int payOrder(OrderDTO.OrderPaymentDTO paymentDTO);

    public IPage<OrderVO.OrderLookVO> getOrder(int current,int size,String username);

    public int deleteOrder(OrderDTO.OrderDeleteDTO deleteDTOS);
}
