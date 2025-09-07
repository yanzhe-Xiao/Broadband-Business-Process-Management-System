package com.xyz.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xyz.dto.OrderItemDTO;
import com.xyz.orders.OrderItem;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xyz.vo.orders.OrderItemVO;

import java.util.List;

/**
* @author X
* @description 针对表【ORDER_ITEM(订单明细表，存储订单中的具体套餐信息)】的数据库操作Service
* @createDate 2025-09-04 14:45:17
*/
public interface OrderItemService extends IService<OrderItem> {

    public int addOrderItem(List<OrderItemDTO.OrderItemAvaliable> orderItemAvailable);

    public int updateTypeAndQty(OrderItemDTO.OrderItemUpdate update);

    public int deleteOrderItem(List<Long> ids);

    public IPage<OrderItemVO.Shopping> getOrderItemFull(int current,int size,String username);

}
