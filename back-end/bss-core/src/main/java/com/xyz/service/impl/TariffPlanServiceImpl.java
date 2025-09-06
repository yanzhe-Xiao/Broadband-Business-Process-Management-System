package com.xyz.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xyz.constraints.OrderConstarint;
import com.xyz.dto.TariffPlanDTO;
import com.xyz.mapper.IpPoolMapper;
import com.xyz.mapper.ResourceDeviceMapper;
import com.xyz.orders.TariffPlan;
import com.xyz.resources.ResourceDevice;
import com.xyz.service.TariffPlanService;
import com.xyz.mapper.TariffPlanMapper;
import com.xyz.vo.orders.TariffPlanVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
* @author X
* @description 针对表【TARIFF_PLAN(套餐资费表，存储宽带套餐信息)】的数据库操作Service实现
* @createDate 2025-09-04 14:45:04
*/
@Service
public class TariffPlanServiceImpl extends ServiceImpl<TariffPlanMapper, TariffPlan>
    implements TariffPlanService{
    @Autowired
    TariffPlanMapper tariffPlanMapper;
    @Autowired
    IpPoolMapper ipPoolMapper;
    @Autowired
    ResourceDeviceMapper resourceDeviceMapper;



    @Override
    @Transactional
    public IPage<TariffPlanVO.TariffPlanDetail> getTariffPlanDetail(int current, int size) {
        IPage<TariffPlan> page = new Page<>(current, size);
        IPage<TariffPlan> tariffPlanPage = tariffPlanMapper.selectPage(page, null);
        return tariffPlanPage.convert(tariffPlan -> {
            String deviceSn = tariffPlan.getDeviceSn();
            ResourceDevice device = deviceSn != null ? resourceDeviceMapper.selectBySn(deviceSn) : null;

            return TariffPlanVO.TariffPlanDetail.builder()
                    .planCode(tariffPlan.getPlanCode())
                    .name(tariffPlan.getName())
                    .description(tariffPlan.getDescription())
                    .monthlyFee(tariffPlan.getMonthlyFee())
                    .yearlyFee(tariffPlan.getYearlyFee())
                    .foreverFee(tariffPlan.getForeverFee())
                    .installationFee(OrderConstarint.INSTALLATION_FEE)
                    .contractPeriod(tariffPlan.getPlanPeriod())
                    .status(tariffPlan.getStatus())
                    .isIp(tariffPlan.getIsIp())
                    .bandwidth(tariffPlan.getBandwith())
                    .qty(tariffPlan.getQty())
                    .requireDeviceSn(device != null ? device.getSn() : null)
                    .requiredDeviceModel(device != null ? device.getModel() : null)
                    .requiredDeviceQty(tariffPlan.getDeviceQty())
                    .devicePrice(device != null ? device.getPrice() : null)
                    .build();
        });
    }

    @Override
    @Transactional
    public Integer addTariffPlanService(List<TariffPlanDTO.TariffPlanAvaliable> tariffPlanAvaliables) {
        List<TariffPlan> plans = tariffPlanAvaliables.stream()
                .map(
                        tariffPlanAvaliable -> {
                            Integer ip = tariffPlanAvaliable.isIp();
                            if (ip == 1) {
                                try {
                                    Integer bandwidth = ipPoolMapper.selectMaxAvaliableBandwidth();
                                    if (bandwidth < tariffPlanAvaliable.bandwidth() * tariffPlanAvaliable.qty()) {
                                        throw new IllegalArgumentException(
                                                String.format("IP资源池中最大可用带宽(%d MB)小于套餐所需带宽(%d MB)",
                                                        bandwidth, tariffPlanAvaliable.bandwidth())
                                        );
                                    }
                                    // 如果符合要求，继续处理逻辑
                                } catch (DataAccessException e) {
                                    throw new RuntimeException("检查IP带宽资源时发生错误: " + e.getMessage(), e);
                                }
                            }
                            return TariffPlanDTO.TariffPlanAvaliable.toEntity(tariffPlanAvaliable);
                        }
                ).toList();
        boolean result = this.saveBatch(plans);
        return result ? plans.size() : 0;
    }
}




