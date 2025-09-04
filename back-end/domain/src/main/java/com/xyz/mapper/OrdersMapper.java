package com.xyz.mapper;

import com.xyz.orders.Orders;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author X
* @description 针对表【ORDERS(订单表，存储宽带新装/变更/销户等主订单)】的数据库操作Mapper
* @createDate 2025-09-04 14:45:21
* @Entity com.xyz.orders.Orders
*/
@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {

}




