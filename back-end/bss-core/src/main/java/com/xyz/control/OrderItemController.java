package com.xyz.control;

import com.xyz.advice.SuccessAdvice;
import com.xyz.common.ResponseResult;
import com.xyz.dto.OrderItemDTO;
import com.xyz.service.OrderItemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>Package Name: com.xyz.control </p>
 * <p>Description:  </p>
 * <p>Create Time: 2025/9/6 </p>
 *
 * @author <a href="https://github.com/yanzhe-xiao">YANZHE XIAO</a>
 * @version 1.0
 * @since
 */
@RestController
@RequestMapping("/api/order-item")
public class OrderItemController {
    @Autowired
    OrderItemService orderItemService;

    @PostMapping("/add")
    public ResponseResult addOrderItem(@RequestBody @Valid List<OrderItemDTO.@Valid OrderItemAvaliable> orderItemAvailable){
        int i = orderItemService.addOrderItem(orderItemAvailable);
        return ResponseResult.success(SuccessAdvice.insertSuccessMessage(i));
    }

    @PostMapping("/update")
    public ResponseResult updateOrderItem(@RequestBody OrderItemDTO.OrderItemUpdate update){
        int i = orderItemService.updateTypeAndQty(update);
        return ResponseResult.success(SuccessAdvice.updateSuccessMessage(i));
    }

    @DeleteMapping("/delete")
    public ResponseResult deleteOrderItem(@RequestBody List<Long> ids){
        int i = orderItemService.deleteOrderItem(ids);
        return ResponseResult.success(SuccessAdvice.deleteSuccessMessage(i));
    }

    //todo 查找
}
