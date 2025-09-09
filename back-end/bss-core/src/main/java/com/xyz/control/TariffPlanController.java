package com.xyz.control;

import com.xyz.advice.SuccessAdvice;
import com.xyz.common.PageResult;
import com.xyz.common.ResponseResult;
import com.xyz.dto.TariffPlanDTO;
import com.xyz.service.TariffPlanService;
import com.xyz.utils.UserAuth;
import com.xyz.vo.orders.TariffPlanVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>Package Name: com.xyz.control </p>
 * <p>Description:  </p>
 * <p>Create Time: 2025/9/5 </p>
 *
 * @author <a href="https://github.com/yanzhe-xiao">YANZHE XIAO</a>
 * @version 1.0
 * @since
 */
@RestController
@RequestMapping("/api")
public class TariffPlanController {
    @Autowired
    TariffPlanService tariffPlanService;

    @PostMapping("/customer/menu")
    public PageResult<TariffPlanVO.TariffPlanDetail> getTariffPlanService(@RequestBody TariffPlanDTO.TariffPlanSearchCriteriaController searchCriteria){
        TariffPlanDTO.TariffPlanSearchCriteria newDto =
                TariffPlanDTO.TariffPlanSearchCriteria.withUsername(searchCriteria, UserAuth.getCurrentUsername());
        return PageResult.of(tariffPlanService.getTariffPlanDetail(newDto));
    }

    @PostMapping("/tariffplan/add")
    public ResponseResult addTariffPlans(@RequestBody List<TariffPlanDTO.TariffPlanAvaliable>  plans){
        Integer i = tariffPlanService.addTariffPlanService(plans);
        return ResponseResult.success(SuccessAdvice.insertSuccessMessage(i));
    }
}
