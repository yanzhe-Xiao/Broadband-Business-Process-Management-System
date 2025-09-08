package com.xyz.mapper;

import com.xyz.orders.Rating;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
* @author X
* @description 针对表【RATING(评价表（每个订单项一条评价，便于逐套餐评分）)】的数据库操作Mapper
* @createDate 2025-09-08 20:36:17
* @Entity com.xyz.orders.Rating
*/
@Mapper
public interface RatingMapper extends BaseMapper<Rating> {
    BigDecimal selectAvgScoreByPlanCode(@Param("planCode") String planCode);
}




