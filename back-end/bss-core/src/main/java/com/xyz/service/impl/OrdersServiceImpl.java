package com.xyz.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xyz.dto.OrderDTO;
import com.xyz.mapper.AppUserMapper;
import com.xyz.mapper.OrderItemMapper;
import com.xyz.mapper.OrdersMapper;
import com.xyz.orders.OrderItem;
import com.xyz.orders.Orders;
import com.xyz.service.OrdersService;
import com.xyz.user.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
* @author X
* @description 针对表【ORDERS(订单表，存储宽带新装/变更/销户等主订单)】的数据库操作Service实现
* @createDate 2025-09-04 14:45:21
*/
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders>
    implements OrdersService{
    @Autowired
    OrderItemMapper orderItemMapper;
    @Autowired
    OrdersMapper ordersMapper;
    @Autowired
    AppUserMapper appUserMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int commitOrder(OrderDTO.OrderAvaliableDTO dto) {
        //TODO 判断oderItemId里的user是不是这里提供的user
        //TODO 在创建订单的时候就应该进行分配ip和扣除库存了 扣除套餐的数量 扣除前应先校验数量
        //TODO 需要删掉dto里的status，应默认直接设置为待支付
        //TODO 如果一个user已有一个待支付的订单则不能进行新的下单
        Long id = appUserMapper.selectIdByUsername(dto.username());
        // 1. 创建订单实体
        Orders entity = OrderDTO.OrderAvaliableDTO.toEntity(dto,id);
        int insert = ordersMapper.insert(entity);

        if (insert > 0) {
            // 2. 批量更新订单项的订单ID和状态
            List<Long> orderItemIds = dto.orderItemId();
            if (orderItemIds != null && !orderItemIds.isEmpty()) {
                orderItemMapper.batchUpdateOrderIdById(entity.getId(), orderItemIds);
                orderItemMapper.batchUpdateStatusAndExpireById("待支付", orderItemIds);
            }
        } else {
            throw new RuntimeException("提交订单失败，未知原因，请联系客服");
        }
        return insert;
    }
}




