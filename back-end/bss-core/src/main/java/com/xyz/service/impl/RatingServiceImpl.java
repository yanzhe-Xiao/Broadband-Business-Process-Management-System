package com.xyz.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xyz.constraints.OrderStatuses;
import com.xyz.mapper.OrderItemMapper;
import com.xyz.mapper.OrdersMapper;
import com.xyz.mapper.TariffPlanMapper;
import com.xyz.orders.OrderItem;
import com.xyz.orders.Orders;
import com.xyz.orders.Rating;
import com.xyz.orders.TariffPlan;
import com.xyz.service.RatingService;
import com.xyz.mapper.RatingMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

/**
* @author X
* @description 针对表【RATING(评价表（每个订单项一条评价，便于逐套餐评分）)】的数据库操作Service实现
* @createDate 2025-09-08 20:36:17
*/
@Service
@RequiredArgsConstructor
public class RatingServiceImpl extends ServiceImpl<RatingMapper, Rating>
    implements RatingService{
    @Autowired
    RatingMapper ratingMapper;
    @Autowired
    OrderItemMapper orderItemMapper;
    @Autowired
    TariffPlanMapper tariffPlanMapper;
    @Autowired
    OrdersMapper ordersMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void submitRating(Long orderItemId, Long userId, int score, String comment) {
        // 写入评分
        Rating r = new Rating();
        r.setOrderItemId(orderItemId);
        r.setUserId(userId);
        r.setScore(score);
        r.setRatComment(comment);
        this.save(r);

        // 找到 planCode
        OrderItem oi = orderItemMapper.selectById(orderItemId);
        if (oi == null) return;
        String planCode = oi.getPlanCode();
        if (planCode == null) return;

        // 即时重算平均分（SQL 聚合一次）
        BigDecimal newAvg = ratingMapper.selectAvgScoreByPlanCode(planCode);
        tariffPlanMapper.update(null, Wrappers.<TariffPlan>update()
                .eq("plan_code", planCode)
                .set("rating", newAvg)
                );
        ordersMapper.update(null, Wrappers.<Orders>lambdaUpdate()
                .eq(Orders::getId, oi.getOrderId())
                .set(Orders::getStatus, OrderStatuses.COMPLETED));

    }
}




