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
import com.xyz.service.OrdersService;
import com.xyz.service.RatingService;
import com.xyz.mapper.RatingMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.LinkedHashSet;

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
    @Autowired
    OrdersService ordersService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void submitRating(Long orderId, String planCode, Long userId, int score, String comment) {
        if (orderId == null) throw new IllegalArgumentException("orderId 不能为空");
        if (planCode == null || planCode.isBlank()) throw new IllegalArgumentException("planCode 不能为空");

        // 1) 校验订单存在&归属&状态=待评价
        Orders order = ordersMapper.selectById(orderId);
        if (order == null) throw new IllegalArgumentException("订单不存在: " + orderId);
        if (!Objects.equals(order.getUserId(), userId)) {
            throw new SecurityException("无权为该订单评价");
        }
        if (!OrderStatuses.TO_REVIEW.equals(order.getStatus())) {
            throw new IllegalStateException("订单当前状态不允许评价: " + order.getStatus());
        }

        // 2) 插入/更新评分（同一订单+同一套餐+同一用户，避免重复多条）
        Rating existed = ratingMapper.selectOne(
                Wrappers.<Rating>lambdaQuery()
                        .eq(Rating::getOrderId, orderId)
                        .eq(Rating::getPlanCode, planCode)
                        .eq(Rating::getUserId, userId)
                        .last("FETCH FIRST 1 ROWS ONLY")
        );
        if (existed == null) {
            Rating r = new Rating();
            r.setOrderId(orderId);
            r.setPlanCode(planCode);
            r.setUserId(userId);
            r.setScore(score);
            r.setRatComment(comment);
            ratingMapper.insert(r);
        } else {
            ratingMapper.update(null,
                    Wrappers.<Rating>update()
                            .eq("id", existed.getId())
                            .set("score", score)
                            .set("rat_comment", comment)
            );
        }

        // 3) 重新计算该套餐的平均分（全局/跨订单）
        BigDecimal newAvg = ratingMapper.selectAvgScoreByPlanCode(planCode);
        tariffPlanMapper.update(null, Wrappers.<TariffPlan>update()
                .eq("plan_code", planCode)
                .set("rating", newAvg)
        );

        // 4) 判断该订单下的所有套餐是否都已被评价
        // 4.1 订单所需的套餐集合（去重）
        List<OrderItem> orderItems = orderItemMapper.selectList(
                Wrappers.<OrderItem>lambdaQuery()
                        .eq(OrderItem::getOrderId, orderId)
                        .select(OrderItem::getPlanCode)
        );
        Set<String> needPlans = orderItems.stream()
                .map(OrderItem::getPlanCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (needPlans.isEmpty()) throw new IllegalArgumentException("当前订单无套餐"); // 没有套餐，无需进入完成判定

        // 4.2 已评价的套餐集合（在同一订单下）
        List<Rating> ratingsOfOrder = ratingMapper.selectList(
                Wrappers.<Rating>lambdaQuery()
                        .eq(Rating::getOrderId, orderId)
                        .select(Rating::getPlanCode)
        );
        Set<String> ratedPlans = ratingsOfOrder.stream()
                .map(Rating::getPlanCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 4.3 先比数量，再比集合相等（都评价完）
        if (ratedPlans.size() == needPlans.size() && ratedPlans.containsAll(needPlans)) {
            // 守护条件，避免并发下误改
            ordersMapper.update(null,
                    Wrappers.<Orders>update()
                            .eq("id", orderId)
                            .eq("status", OrderStatuses.TO_REVIEW)
                            .set("status", OrderStatuses.COMPLETED)
            );
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submitRatings(Long orderId, Long userId, int score, String comment) {
        List<String> planCodes = ordersService.getPlanCodesByOrderId(orderId);
        planCodes.forEach(planCode -> submitRating(orderId, planCode,userId,score,comment));
        return planCodes.size();

    }

}




