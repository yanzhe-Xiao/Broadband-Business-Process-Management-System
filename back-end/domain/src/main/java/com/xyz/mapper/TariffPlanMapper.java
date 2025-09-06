package com.xyz.mapper;
import org.apache.ibatis.annotations.Param;

import com.xyz.orders.TariffPlan;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author X
* @description 针对表【TARIFF_PLAN(套餐资费表，存储宽带套餐信息)】的数据库操作Mapper
* @createDate 2025-09-06 09:35:14
* @Entity com.xyz.orders.TariffPlan
*/
@Mapper
public interface TariffPlanMapper extends BaseMapper<TariffPlan> {
    TariffPlan selectOneByPlanCode(@Param("planCode") String planCode);
}




