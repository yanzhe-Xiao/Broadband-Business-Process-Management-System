package com.xyz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xyz.advice.ImageUrlSplicing;
import com.xyz.constraints.OrderConstarint;
import com.xyz.dto.TariffPlanDTO;
import com.xyz.mapper.IpPoolMapper;
import com.xyz.mapper.ResourceDeviceMapper;
import com.xyz.orders.TariffPlan;
import com.xyz.resources.ResourceDevice;
import com.xyz.service.ImageStorageService;
import com.xyz.service.TariffPlanService;
import com.xyz.mapper.TariffPlanMapper;
import com.xyz.vo.orders.TariffPlanVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

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
    @Autowired
    ImageStorageService imageStorageService;


    @Override
    @Transactional(readOnly = true)
    public IPage<TariffPlanVO.TariffPlanDetail> getTariffPlanDetail(TariffPlanDTO.TariffPlanSearchCriteria criteria) {
        int current = Math.max(1, criteria.current());
        int size = (criteria.size() == null || criteria.size() <= 0) ? 10 : criteria.size();

        String keyword = trimToNull(criteria.keyword());
        BigDecimal minPrice = criteria.minPrice();
        BigDecimal maxPrice = criteria.maxPrice();
        Boolean onlyInStock = criteria.onlyInStock();
        String sort = trimToNull(criteria.sort());          // priceUp | priceDown | rating
        String priceSort = trimToNull(criteria.priceSort()); // month | year | forever

        // 默认 priceSort=month
        if (!"year".equals(priceSort) && !"forever".equals(priceSort)) {
            priceSort = "month";
        }

        // 选择价格字段引用
        SFunction<TariffPlan, BigDecimal> priceGetter = switch (priceSort) {
            case "year" -> TariffPlan::getYearlyFee;
            case "forever" -> TariffPlan::getForeverFee;
            default -> TariffPlan::getMonthlyFee; // month
        };

        LambdaQueryWrapper<TariffPlan> qw = Wrappers.lambdaQuery();

        // 关键字匹配：编码或名称
        if (keyword != null) {
            qw.and(w -> w.like(TariffPlan::getPlanCode, keyword)
                    .or()
                    .like(TariffPlan::getName, keyword));
        }

        // 价格区间按 priceSort 指定字段过滤
        if (minPrice != null) qw.ge(priceGetter, minPrice);
        if (maxPrice != null) qw.le(priceGetter, maxPrice);

        // 仅显示有库存
        if (Boolean.TRUE.equals(onlyInStock)) {
            qw.gt(TariffPlan::getQty, 0);
            qw.eq(TariffPlan::getStatus,"ACTIVE");
        }

        // 排序
        if ("priceUp".equals(sort)) {
            qw.orderByAsc(priceGetter);
        } else if ("priceDown".equals(sort)) {
            qw.orderByDesc(priceGetter);
        } else if ("rating".equals(sort)) {
            qw.orderByDesc(TariffPlan::getRating); // 确认表里有 rating
        } else {
            qw.orderByDesc(TariffPlan::getCreatedAt);
        }

        // 分页查询
        IPage<TariffPlan> page = new Page<>(current, size);
        IPage<TariffPlan> tariffPage = tariffPlanMapper.selectPage(page, qw);

        // 批量查设备，避免 N+1
        List<TariffPlan> plans = tariffPage.getRecords();
        Map<String, ResourceDevice> deviceMap;
        if (plans != null && !plans.isEmpty()) {
            Set<String> sns = plans.stream()
                    .map(TariffPlan::getDeviceSn)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            if (!sns.isEmpty()) {
                // 如果没有 selectBatchBySn，可以用 in 查询
                List<ResourceDevice> devices = resourceDeviceMapper.selectList(
                        Wrappers.<ResourceDevice>lambdaQuery().in(ResourceDevice::getSn, sns)
                );

                deviceMap = devices.stream().collect(
                        Collectors.toMap(
                                ResourceDevice::getSn,
                                d -> d,
                                (a, b) -> a,                // 遇到重复 SN，保留第一个（理论上不会发生）
                                LinkedHashMap::new
                        )
                );
            } else {
                deviceMap = Collections.emptyMap();
            }
        } else {
            deviceMap = Collections.emptyMap();
        }

        // 转 VO
        return tariffPage.convert(plan -> {
            ResourceDevice device = null;
            String sn = plan.getDeviceSn();
            if (sn != null) device = deviceMap.get(sn);
            String fullImageUrl = ImageUrlSplicing.splicingURL(plan.getImageUrl());
            return TariffPlanVO.TariffPlanDetail.builder()
                    .planCode(plan.getPlanCode())
                    .name(plan.getName())
                    .description(plan.getDescription())
                    .monthlyFee(plan.getMonthlyFee())
                    .yearlyFee(plan.getYearlyFee())
                    .foreverFee(plan.getForeverFee())
                    .installationFee(OrderConstarint.INSTALLATION_FEE)
                    .contractPeriod(plan.getPlanPeriod())
                    .status(plan.getStatus())
                    .isIp(plan.getIsIp())
                    .bandwidth(plan.getBandwith())
                    .qty(plan.getQty())
                    .requireDeviceSn(device != null ? device.getSn() : null)
                    .requiredDeviceModel(device != null ? device.getModel() : null)
                    .requiredDeviceQty(plan.getDeviceQty())
                    .devicePrice(device != null ? device.getPrice() : null)
                    .imageUrl(fullImageUrl)
                    .rating(plan.getRating())
                    .build();
        });
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
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
                            return TariffPlanDTO.TariffPlanAvaliable.toEntity(tariffPlanAvaliable,imageStorageService::saveBase64Image);
                        }
                ).toList();
        boolean result = this.saveBatch(plans);
        return result ? plans.size() : 0;
    }
}




