package com.xyz.control;

import com.xyz.advice.SuccessAdvice;
import com.xyz.common.ResponseResult;
import com.xyz.dto.OrderItemDTO;
import com.xyz.service.OrderItemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseResult addOrderItem(@RequestBody @Valid List<OrderItemDTO.@Valid OrderItemAvailable> orderItemAvailable){
        int i = orderItemService.addOrderItem(orderItemAvailable);
        return ResponseResult.success(SuccessAdvice.insertSuccessMessage(i));
    }
}
