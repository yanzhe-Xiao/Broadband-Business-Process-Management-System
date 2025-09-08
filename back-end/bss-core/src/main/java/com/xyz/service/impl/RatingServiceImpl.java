package com.xyz.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xyz.mapper.OrderItemMapper;
import com.xyz.mapper.TariffPlanMapper;
import com.xyz.orders.OrderItem;
import com.xyz.orders.Rating;
import com.xyz.orders.TariffPlan;
import com.xyz.service.RatingService;
import com.xyz.mapper.RatingMapper;
import lombok.RequiredArgsConstructor;
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
    private final RatingMapper ratingMapper;
    private final OrderItemMapper orderItemMapper;
    private final TariffPlanMapper tariffPlanMapper;

    @Transactional(rollbackFor = Exception.class)
    public void submitRating(Long orderItemId, Long userId, int score, String comment) {
        // 写入评分
        Rating r = new Rating();
        r.setOrderItemId(orderItemId);
        r.setUserId(userId);
        r.setScore(score);
        r.setRatComment(comment);
        r.setCreatedAt(new Date());
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
                .set("updated_at", new Date()));
    }
}




