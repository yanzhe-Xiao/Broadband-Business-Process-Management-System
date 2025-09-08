package com.xyz.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xyz.orders.TariffPlan;
import com.xyz.service.TariffPlanService;
import com.xyz.mapper.TariffPlanMapper;
import org.springframework.stereotype.Service;

/**
* @author X
* @description 针对表【TARIFF_PLAN(套餐资费表，存储宽带套餐信息)】的数据库操作Service实现
* @createDate 2025-09-08 11:57:39
*/
@Service
public class TariffPlanServiceImpl extends ServiceImpl<TariffPlanMapper, TariffPlan>
    implements TariffPlanService{

}




