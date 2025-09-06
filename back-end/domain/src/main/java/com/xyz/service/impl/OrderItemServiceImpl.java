package com.xyz.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xyz.orders.OrderItem;
import com.xyz.service.OrderItemService;
import com.xyz.mapper.OrderItemMapper;
import org.springframework.stereotype.Service;

/**
* @author X
* @description 针对表【ORDER_ITEM(订单明细表，存储订单中的具体套餐信息)】的数据库操作Service实现
* @createDate 2025-09-06 16:24:44
*/
@Service
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItem>
    implements OrderItemService{

}




