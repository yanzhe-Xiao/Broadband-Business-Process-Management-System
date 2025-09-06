package com.xyz.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xyz.constraints.OrderConstarint;
import com.xyz.dto.OrderItemDTO;
import com.xyz.mapper.OrderItemMapper;
import com.xyz.mapper.TariffPlanMapper;
import com.xyz.orders.OrderItem;
import com.xyz.orders.TariffPlan;
import com.xyz.service.OrderItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItem>
        implements OrderItemService {

    @Resource
    TariffPlanMapper tariffPlanMapper;
    @Resource
    OrderItemMapper orderItemMapper;

    @Override
    @Transactional
    public int addOrderItem(List<OrderItemDTO.OrderItemAvaliable> dtos) {
        if (dtos == null || dtos.isEmpty()) return 0;

        // 1) 收集 planCode，批量查套餐，避免 N+1
        Set<String> planCodes = dtos.stream()
                .map(OrderItemDTO.OrderItemAvaliable::planCode)
                .collect(Collectors.toSet());
        if (planCodes.isEmpty()) return 0;

        List<TariffPlan> plans = tariffPlanMapper.selectList(
                Wrappers.<TariffPlan>lambdaQuery().in(TariffPlan::getPlanCode, planCodes)
        );
        Map<String, TariffPlan> planMap = plans.stream()
                .collect(Collectors.toMap(TariffPlan::getPlanCode, p -> p));

        // 2) 先做“forever”存在性校验：只要 DB 已有 username+planCode + forever，则禁止新增
        //    需要知道所有涉及到的 username+planCode 组合
        record Key(String username, String planCode) {}
        Set<Key> keys = dtos.stream()
                .map(d -> new Key(d.username(), d.planCode()))
                .collect(Collectors.toSet());

        // 查询库里是否存在任一 key 的 forever
        List<OrderItem> existedForever = orderItemMapper.selectList(
                Wrappers.<OrderItem>lambdaQuery()
                        .in(OrderItem::getUsername, keys.stream().map(Key::username).collect(Collectors.toSet()))
                        .in(OrderItem::getPlanCode, keys.stream().map(Key::planCode).collect(Collectors.toSet()))
                        .eq(OrderItem::getPlanType, "forever")
        );

        if (!existedForever.isEmpty()) {
            // 冲突 key 集合（用于提示）
            Set<Key> conflict = existedForever.stream()
                    .map(e -> new Key(e.getUsername(), e.getPlanCode()))
                    .collect(Collectors.toSet());
            // 任一 dto 命中冲突就失败
            for (OrderItemDTO.OrderItemAvaliable dto : dtos) {
                if (conflict.contains(new Key(dto.username(), dto.planCode()))) {
                    throw new IllegalStateException("已存在 forever 套餐，禁止对 username=" +
                            dto.username() + ", planCode=" + dto.planCode() + " 继续添加。");
                }
            }
        }

        int affected = 0;

        // 3) 按 DTO 逐一处理（计算价格、合并/插入）
        for (OrderItemDTO.OrderItemAvaliable dto : dtos) {
            String planCode = dto.planCode();
            TariffPlan plan = planMap.get(planCode);
            if (plan == null) {
                throw new IllegalArgumentException("未找到套餐 planCode=" + planCode);
            }

            // 计算单价与总价
            BigDecimal unitPrice = computeUnitPriceByType(plan, dto.planType());
            BigDecimal totalPrice;
            if ("forever".equals(dto.planType())) {
                // forever 价格与数量无关（你前面规则里购物车阶段把 qty 固定为 1）
                totalPrice = unitPrice;
            } else {
                totalPrice = unitPrice.multiply(BigDecimal.valueOf(dto.qty()))
                        .setScale(2, RoundingMode.HALF_UP);
            }

            String username = dto.username();
            String status = dto.status(); // "在购物车中" / "待支付" / "已支付"
            String incomingType = dto.planType(); // "month" / "year" / "forever"

            // 购物车：允许合并
            if ("在购物车中".equals(status)) {
                // 找到同 username+planCode+status 的现有项
                List<OrderItem> same3 = orderItemMapper.selectList(
                        Wrappers.<OrderItem>lambdaQuery()
                                .eq(OrderItem::getUsername, username)
                                .eq(OrderItem::getPlanCode, planCode)
                                .eq(OrderItem::getStatus, "在购物车中")
                );

                if ("forever".equals(incomingType)) {
                    // 规则：三键相同 + 本次是 forever => 合并为一条 forever
                    // 处理方式：若已有记录，统一“收敛”为一条 forever；保留第一条，删除其余
                    if (same3.isEmpty()) {
                        // 直接插入一条 forever（qty 固定 1，period=MAX，price=unitPrice）
                        OrderItem one = dto.toEntity(unitPrice.setScale(2, RoundingMode.HALF_UP)); // 价格不乘 qty
                        one.setQty(1);
                        one.setPeriod(Long.MAX_VALUE);
                        one.setPlanType("forever");
                        // 建议：购物车阶段 orderId 可以为空
                        one.setOrderId(null);
                        affected += orderItemMapper.insert(one);
                    } else {
                        // 先删多余，再把第一条更新成 forever 的规范
                        OrderItem keep = same3.get(0);
                        List<Long> toDel = same3.stream().skip(1).map(OrderItem::getId).toList();
                        if (!toDel.isEmpty()) {
                            affected += orderItemMapper.deleteBatchIds(toDel);
                        }
                        keep.setPlanType("forever");
                        keep.setQty(1);
                        keep.setPeriod(Long.MAX_VALUE);
                        keep.setPrice(unitPrice.setScale(2, RoundingMode.HALF_UP)); // forever 单价作为总价
                        keep.setUpdatedAt(new Date());
                        affected += orderItemMapper.updateById(keep);
                    }
                    continue;
                }

                // month/year 的情况
                if (same3.isEmpty()) {
                    // 无同三键，直接插入
                    OrderItem entity = dto.toEntity(totalPrice);
                    fillPeriod(entity, incomingType, dto.qty());
                    entity.setOrderId(null);
                    affected += orderItemMapper.insert(entity);
                } else {
                    // 有同三键
                    // 先找是否有同类型
                    List<OrderItem> sameType = same3.stream()
                            .filter(o -> incomingType.equals(o.getPlanType()))
                            .collect(Collectors.toList());
                    if (!sameType.isEmpty()) {
                        // 合并为一条：qty 累加，price 重算（单价*总 qty），period 重算
                        int existedQty = sameType.stream().map(OrderItem::getQty).filter(Objects::nonNull)
                                .mapToInt(Integer::intValue).sum();
                        int newQty = existedQty + dto.qty();

                        // 收敛为一条
                        OrderItem keep = sameType.get(0);
                        List<Long> delIds = sameType.stream()
                                .skip(1)
                                .map(OrderItem::getId)
                                .toList(); // 仅同类型重复项
                        if (!delIds.isEmpty()) affected += orderItemMapper.deleteBatchIds(delIds);

                        keep.setQty(newQty);
                        keep.setPrice(unitPrice.multiply(BigDecimal.valueOf(newQty)).setScale(2, RoundingMode.HALF_UP));
                        fillPeriod(keep, incomingType, newQty);
                        keep.setUpdatedAt(new Date());
                        affected += orderItemMapper.updateById(keep);
                    } else {
                        // 没有同类型，且不是 forever（已在上面分支处理），插入新条目
                        OrderItem entity = dto.toEntity(totalPrice);
                        fillPeriod(entity, incomingType, dto.qty());
                        entity.setOrderId(null);
                        affected += orderItemMapper.insert(entity);
                    }
                }

            } else {
                // 非购物车（待支付/已支付）：两键相同也不合并，必须新插入
                OrderItem entity = dto.toEntity(totalPrice);
                fillPeriod(entity, incomingType, dto.qty());
                // 如果是“待支付/已支付”，通常应当已经有 orderId（下单后生成），
                // 这里若 DTO 没带，可以按你的业务在外层补齐。
                affected += orderItemMapper.insert(entity);
            }
        }

        return affected;
    }

    @Override
    public int updateTypeAndQty(OrderItemDTO.OrderItemUpdate update) {
        Objects.requireNonNull(update, "update 不能为空");

        // 1) 查当前订单项
        OrderItem item = orderItemMapper.selectById(update.id());
        if (item == null) {
            throw new IllegalArgumentException("订单项不存在 id=" + update.id());
        }

        // 2) 目标类型、数量
        String targetType = update.planType();
        Integer targetQty = update.qty();
        if (targetType == null || targetQty == null) {
            throw new IllegalArgumentException("planType 与 qty 均不能为空");
        }
        if (targetQty <= 0 && !"forever".equals(targetType)) {
            throw new IllegalArgumentException("qty 必须为正整数（forever 不使用 qty）");
        }

        // 3) 一致性校验：确保 username + planCode + forever 的唯一性
        //    a) 若将当前条改为 month/year，则库中不能存在其它 forever（同 user+planCode）
        //    b) 若将当前条改为 forever，则库中不能存在其它 forever（同 user+planCode）
        boolean toForever = "forever".equals(targetType);
        boolean fromForever = "forever".equals(item.getPlanType());

        List<OrderItem> foreverSiblings = orderItemMapper.selectList(
                Wrappers.<OrderItem>lambdaQuery()
                        .eq(OrderItem::getUsername, item.getUsername())
                        .eq(OrderItem::getPlanCode, item.getPlanCode())
                        .eq(OrderItem::getPlanType, "forever")
                        .ne(OrderItem::getId, item.getId())
        );
        if (toForever || !toForever) { // 任何目标都需要保证唯一 forever 的约束
            if (!foreverSiblings.isEmpty() && !toForever) {
                // 目标为非 forever，但兄弟里已有 forever
                throw new IllegalStateException("已存在 forever 套餐，禁止将该条改为 " + targetType);
            }
            if (!foreverSiblings.isEmpty() && toForever) {
                // 目标为 forever，但兄弟里已有 forever
                throw new IllegalStateException("已存在 forever 套餐，禁止重复设置 forever");
            }
        }

        // 4) 查套餐，计算价格/周期
        TariffPlan plan = tariffPlanMapper.selectOne(
                Wrappers.<TariffPlan>lambdaQuery().eq(TariffPlan::getPlanCode, item.getPlanCode())
        );
        if (plan == null) {
            throw new IllegalStateException("未找到套餐 planCode=" + item.getPlanCode());
        }

        // 5) 根据目标类型做规格化 & 价格/周期计算
        BigDecimal unit = computeUnitPriceByType(plan, targetType); // 未乘 qty 的“单价”
        if ("forever".equals(targetType)) {
            // forever：qty=1、period=MAX、price=unit
            item.setPlanType("forever");
            item.setQty(1);
            item.setPeriod(Long.MAX_VALUE);
            item.setPrice(unit.setScale(2, RoundingMode.HALF_UP));
        } else {
            // month/year：period=30/365 * qty、price=unit * qty
            item.setPlanType(targetType);
            item.setQty(targetQty);
            long periodDays = "month".equals(targetType) ? 30L * targetQty : 365L * targetQty;
            item.setPeriod(periodDays);
            item.setPrice(unit.multiply(BigDecimal.valueOf(targetQty)).setScale(2, RoundingMode.HALF_UP));
        }


        return orderItemMapper.updateById(item);
    }

    @Override
    @Transactional
    public int deleteOrderItem(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        // 批量查询订单项
        List<OrderItem> items = orderItemMapper.selectBatchIds(ids);

        // 检查是否存在不存在的订单项
        if (items.size() != ids.size()) {
            Set<Long> existingIds = items.stream().map(OrderItem::getId).collect(Collectors.toSet());
            Set<Long> missingIds = ids.stream()
                    .filter(id -> !existingIds.contains(id))
                    .collect(Collectors.toSet());
            if (!missingIds.isEmpty()) {
                throw new IllegalArgumentException("订单项不存在，id=" + missingIds);
            }
        }

        // 检查是否包含已支付|待支付的订单项
        List<Long> paidIds = items.stream()
                .filter(item -> "已支付".equals(item.getStatus()) || "待支付".equals(item.getStatus()))
                .map(OrderItem::getId)
                .toList();

        if (!paidIds.isEmpty()) {
            throw new IllegalStateException("已支付/待支付的订单项不可删除，id=" + paidIds);
        }

        // 批量删除
        return orderItemMapper.delete(Wrappers.<OrderItem>lambdaQuery().in(OrderItem::getId, ids));    }





    /* ======= 辅助方法 ======= */

    /**
     * 按 planType 计算“单价”（未乘 qty）：
     * - month    => monthlyFee
     * - year     => yearlyFee
     * - forever  => foreverFee
     * 折扣规则：discount 为“百分数”，100 表示无折扣；最终价 = 基础价 * (discount / 100)
     * 结果保留两位小数（HALF_UP），最低不小于 0。
     */
    private BigDecimal computeUnitPriceByType(TariffPlan plan, String planType) {
        BigDecimal baseFee;
        switch (planType) {
            case "month" -> baseFee = plan.getMonthlyFee();
            case "year" -> baseFee = plan.getYearlyFee();
            case "forever" -> baseFee = plan.getForeverFee();
            default -> throw new IllegalArgumentException("非法套餐类型: " + planType);
        }
        if (baseFee == null) {
            throw new IllegalStateException("套餐基础价格为空，planCode=" + plan.getPlanCode() + ", type=" + planType);
        }

        // 折扣率：100=不打折；为 null 时按 100 处理
        BigDecimal discountPercent = plan.getDiscount() == null ? BigDecimal.valueOf(100) : plan.getDiscount();

        // 折扣系数 = discount / 100
        BigDecimal factor = discountPercent
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

        BigDecimal unit = baseFee.multiply(factor);
        if (unit.signum() < 0) unit = BigDecimal.ZERO;

        return unit.setScale(2, RoundingMode.HALF_UP);
    }

    /** 按类型设置 period（单位：天） */
    private void fillPeriod(OrderItem item, String planType, int qty) {
        switch (planType) {
            case "month" -> item.setPeriod(30L * qty);
            case "year" -> item.setPeriod(365L * qty);
            case "forever" -> item.setPeriod(Long.MAX_VALUE);
            default -> throw new IllegalArgumentException("非法套餐类型: " + planType);
        }
    }


}
