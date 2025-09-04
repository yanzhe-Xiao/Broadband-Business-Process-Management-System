package com.xyz.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xyz.orders.Orders;
import com.xyz.service.OrdersService;
import com.xyz.mapper.OrdersMapper;
import org.springframework.stereotype.Service;

/**
* @author X
* @description 针对表【ORDERS(订单表，存储宽带新装/变更/销户等主订单)】的数据库操作Service实现
* @createDate 2025-09-04 14:45:21
*/
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders>
    implements OrdersService{

}




