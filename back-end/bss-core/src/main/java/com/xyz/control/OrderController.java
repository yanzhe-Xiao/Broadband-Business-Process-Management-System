package com.xyz.control;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xyz.advice.SuccessAdvice;
import com.xyz.common.PageResult;
import com.xyz.common.ResponseResult;
import com.xyz.dto.OrderDTO;
import com.xyz.service.OrderOrchestrationService;
import com.xyz.service.OrdersService;
import com.xyz.vo.orders.OrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>Package Name: com.xyz.control </p>
 * <p>Description:  </p>
 * <p>Create Time: 2025/9/7 </p>
 *
 * @author <a href="https://github.com/yanzhe-xiao">YANZHE XIAO</a>
 * @version 1.0
 * @since
 */
@RestController
@RequestMapping("/api/order")
public class OrderController {
    @Autowired
    OrdersService ordersService;
    @Autowired
    OrderOrchestrationService orderOrchestrationService;

    @PostMapping("/commit")
    public ResponseResult commit(@RequestBody OrderDTO.OrderAvaliableDTO dto){
        int i = ordersService.commitOrder(dto);
        return ResponseResult.success(SuccessAdvice.insertSuccessMessage(i));
    }

    @PostMapping("/pay")
    public ResponseResult pay(@RequestBody OrderDTO.OrderPaymentDTO dto){
        int i = ordersService.payOrder(dto);
        if(i >= 1){
            orderOrchestrationService.onOrderPaid(dto.orderId());
        }
        return ResponseResult.success(SuccessAdvice.updateSuccessMessage(i));
    }

    @PostMapping("/get")
    public PageResult<OrderVO.OrderLookVO> get(int current, int size, String username){
        IPage<OrderVO.OrderLookVO> order = ordersService.getOrder(current, size, username);
        return PageResult.of(order);
    }

    @DeleteMapping("/delete")
    public ResponseResult delete(@RequestBody OrderDTO.OrderDeleteDTO deleteDTO){
        int i = ordersService.deleteOrder(deleteDTO);
        return ResponseResult.success(SuccessAdvice.deleteSuccessMessage(i));
    }
}
