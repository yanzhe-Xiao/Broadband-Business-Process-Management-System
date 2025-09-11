package com.xyz.control;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xyz.advice.SuccessAdvice;
import com.xyz.common.PageResult;
import com.xyz.common.ResponseResult;
import com.xyz.dto.OrderDTO;
import com.xyz.service.OrderOrchestrationService;
import com.xyz.service.OrdersService;
import com.xyz.utils.UserAuth;
import com.xyz.vo.orders.OrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
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
    public ResponseResult<Long> commit(@RequestBody OrderDTO.OrderControllerDTO dto){
        OrderDTO.OrderAvaliableDTO newDto = OrderDTO.OrderAvaliableDTO.withUsername(dto,UserAuth
                .getCurrentUsername());
        Long orderId = ordersService.commitOrder(newDto);
        return ResponseResult.success(orderId);
    }

    @PostMapping("/pay")
    public ResponseResult pay(@RequestBody OrderDTO.OrderPaymentDTO dto){
        OrderDTO.OrderPaymentDTO newDto = OrderDTO.OrderPaymentDTO.withUsername(dto,UserAuth.getCurrentUsername());
        int i = ordersService.payOrder(newDto);
        if(i >= 1){
            orderOrchestrationService.onOrderPaid(dto.orderId());
        }
        return ResponseResult.success(SuccessAdvice.updateSuccessMessage(i));
    }

    public record GetOrder(int current,int size){}
    @PostMapping("/get")
    public ResponseResult<PageResult<OrderVO.OrderLookVO>> get(@RequestBody GetOrder getOrder){
        String username = UserAuth.getCurrentUsername();
        IPage<OrderVO.OrderLookVO> order = ordersService.getOrder(getOrder.current, getOrder.size, username);
        return ResponseResult.success(PageResult.of(order)) ;
    }

    @DeleteMapping("/delete")
    public ResponseResult delete(@RequestBody OrderDTO.OrderDeleteDTO deleteDTO){
        OrderDTO.OrderDeleteDTO updatedDto = OrderDTO.OrderDeleteDTO.withUsername(deleteDTO, UserAuth.getCurrentUsername());
        int i = ordersService.deleteOrder(updatedDto);
        return ResponseResult.success(SuccessAdvice.deleteSuccessMessage(i));
    }

    // 你可以调这个方法来拿到一个订单里的所有的planCodes
    @GetMapping("/{orderId}/plan-codes")
    public ResponseResult<List<String>> getPlanCodes(@PathVariable Long orderId) {
        List<String> planCodes = ordersService.getPlanCodesByOrderId(orderId);
        return ResponseResult.success(planCodes);
    }

    @GetMapping("/cancle")
    public ResponseResult cancle(Long id){
        int i = ordersService.cancleOrder(id);
        return ResponseResult.success(SuccessAdvice.updateSuccessMessage(i));
    }
}
