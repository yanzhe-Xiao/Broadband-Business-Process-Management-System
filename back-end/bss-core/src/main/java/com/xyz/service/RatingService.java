package com.xyz.service;

import com.xyz.orders.Rating;
import com.baomidou.mybatisplus.extension.service.IService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
* @author X
* @description 针对表【RATING(评价表（每个订单项一条评价，便于逐套餐评分）)】的数据库操作Service
* @createDate 2025-09-08 20:36:17
*/
public interface RatingService extends IService<Rating> {
    public void submitRating(Long orderItemId, Long userId, int score, String comment);
}
