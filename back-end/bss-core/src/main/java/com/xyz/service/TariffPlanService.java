package com.xyz.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xyz.dto.TariffPlanDTO;
import com.xyz.orders.TariffPlan;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xyz.vo.orders.TariffPlanVO;

import java.util.List;

/**
* @author X
* @description 针对表【TARIFF_PLAN(套餐资费表，存储宽带套餐信息)】的数据库操作Service
* @createDate 2025-09-04 14:45:04
*/
public interface TariffPlanService extends IService<TariffPlan> {

    public IPage<TariffPlanVO.TariffPlanDetail> getTariffPlanDetail(TariffPlanDTO.TariffPlanSearchCriteria criteria);

    public Integer addTariffPlanService(List<TariffPlanDTO.TariffPlanAvaliable> tariffPlanAvaliables);

    public IPage<TariffPlanVO.TariffPlanDetail> getTariffPlanDetailAdmin(TariffPlanDTO.TariffPlanSearchCriteria criteria,String status);

}
