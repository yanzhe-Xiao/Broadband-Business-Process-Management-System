package com.xyz.mapper;

import com.xyz.orders.OrderItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author X
* @description 针对表【ORDER_ITEM(订单明细表，存储订单中的具体套餐信息)】的数据库操作Mapper
* @createDate 2025-09-06 23:46:58
* @Entity com.xyz.orders.OrderItem
*/
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

}




