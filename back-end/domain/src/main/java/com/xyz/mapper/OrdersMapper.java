package com.xyz.mapper;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import com.xyz.orders.Orders;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author X
* @description 针对表【ORDERS(订单表，存储宽带新装/变更/销户等主订单)】的数据库操作Mapper
* @createDate 2025-09-07 20:48:08
* @Entity com.xyz.orders.Orders
*/
@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {
    List<Orders> selectAllByUserId(@Param("userId") Long userId);

    List<Orders> selectAllByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);
}




