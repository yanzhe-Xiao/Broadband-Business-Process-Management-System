package com.xyz.mapper;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.xyz.orders.OrderItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author X
* @description 针对表【ORDER_ITEM(订单明细表，存储订单中的具体套餐信息)】的数据库操作Mapper
* @createDate 2025-09-06 16:24:44
* @Entity com.xyz.orders.OrderItem
*/
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
    List<OrderItem> selectAllByUsername(@Param("username") String username);

    List<OrderItem> selectAllByStatus(@Param("status") String status);

    List<OrderItem> selectAllByPlanCode(@Param("planCode") String planCode);

    OrderItem selectOneByPlanCodeAndPlanTypeAndUsernameAndStatus(@Param("planCode") String planCode, @Param("planType") String planType, @Param("username") String username, @Param("status") String status);


    int updateStatusByUsernameAndOrderId(@Param("status") String status, @Param("username") String username, @Param("orderId") Long orderId);
}




