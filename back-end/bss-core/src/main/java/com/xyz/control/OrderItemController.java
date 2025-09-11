package com.xyz.control;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xyz.advice.SuccessAdvice;
import com.xyz.common.PageResult;
import com.xyz.common.ResponseResult;
import com.xyz.dto.OrderItemDTO;
import com.xyz.service.OrderItemService;
import com.xyz.utils.UserAuth;
import com.xyz.vo.orders.OrderItemVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    public ResponseResult addOrderItem(@RequestBody OrderItemDTO.@Valid OrderItemController orderItemController1){
        ArrayList<OrderItemDTO.OrderItemController> orderItemControllers = new ArrayList<>();
        orderItemControllers.add(orderItemController1);
        List<OrderItemDTO.OrderItemAvaliable> orderItemAvaliables = orderItemControllers.stream().map(orderItemController -> {
            return OrderItemDTO.OrderItemAvaliable.withUsername(orderItemController, UserAuth.getCurrentUsername());
        }).collect(Collectors.toList());
        int i = orderItemService.addOrderItem(orderItemAvaliables);
        return ResponseResult.success(SuccessAdvice.insertSuccessMessage(i));
    }

    @PostMapping("/update")
    public ResponseResult updateOrderItem(@RequestBody OrderItemDTO.OrderItemUpdate update){
        int i = orderItemService.updateTypeAndQty(update);
        return ResponseResult.success(SuccessAdvice.updateSuccessMessage(i));
    }

//     record DeteleDto(List<Long> ids){}
    @PostMapping("/delete")
    public ResponseResult deleteOrderItem(@RequestBody List<Long> ids ){
        int i = orderItemService.deleteOrderItem( ids);
        return ResponseResult.success(SuccessAdvice.deleteSuccessMessage(i));
    }

    @GetMapping("/list")
    public PageResult<OrderItemVO.Shopping> getOrerItemFull(int current,int size){
        String username = UserAuth.getCurrentUsername();
        IPage<OrderItemVO.Shopping> orderItemFull = orderItemService.getOrderItemFull(current, size, username);
        return PageResult.of(orderItemFull);
    }
}
