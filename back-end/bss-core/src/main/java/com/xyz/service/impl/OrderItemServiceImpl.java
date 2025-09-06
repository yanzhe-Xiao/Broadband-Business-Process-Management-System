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

@Service
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItem>
        implements OrderItemService {

    @Resource
    TariffPlanMapper tariffPlanMapper;
    @Resource
    OrderItemMapper orderItemMapper;

    @Override
    @Transactional
    public int addOrderItem(List<OrderItemDTO.OrderItemAvailable> dtos) {
        if (dtos == null || dtos.isEmpty()) return 0;

        final Date now = new Date();

        // 1) 收集 planCode 批量查套餐，避免 N+1
        Set<String> planCodes = new LinkedHashSet<>();
        for (var dto : dtos) {
            if (dto != null && dto.planCode() != null) planCodes.add(dto.planCode());
        }
        if (planCodes.isEmpty()) return 0;

        List<TariffPlan> plans = tariffPlanMapper.selectList(
                Wrappers.<TariffPlan>lambdaQuery().in(TariffPlan::getPlanCode, planCodes)
        );
        Map<String, TariffPlan> planMap = new HashMap<>();
        for (TariffPlan p : plans) planMap.put(p.getPlanCode(), p);

        List<OrderItem> toInsert = new ArrayList<>();
        List<OrderItem> toUpdate = new ArrayList<>();
        List<Long> toDeleteIds = new ArrayList<>();

        for (var dto : dtos) {
            assertNoPaidOrPendingForeverTwoKey(dto.username(), dto.planCode());

            if (dto == null) continue;

            TariffPlan plan = planMap.get(dto.planCode());
            if (plan == null) throw new IllegalArgumentException("套餐不存在: planCode=" + dto.planCode());

            // 归一化请求参数
            int reqQty = (dto.qty() == null || dto.qty() <= 0) ? 1 : dto.qty();
            String reqType = dto.planType();
            if (reqType == null) throw new IllegalArgumentException("planType 不能为空（month/year/forever）");

            // 开始时间：空则默认 now；且不允许早于现在（你之前的规则）
            Date reqStart = dto.startBillingAt() != null ? dto.startBillingAt() : now;
            if (reqStart.before(now)) {
                throw new IllegalArgumentException("开始时间不能早于当前时间");
            }
            Date reqEnd = OrderItemDTO.calculateEndBilling(reqStart, reqType, reqQty);

            // ===== 先用四键精确查（planCode, planType, username, status）=====
            OrderItem exact = orderItemMapper.selectOneByPlanCodeAndPlanTypeAndUsernameAndStatus(
                    dto.planCode(), reqType, dto.username(), dto.status()
            );

            if (exact != null) {
                // 四键命中：同类型合并
                // 禁止“新开始时间晚于已有开始时间”
                if (dto.startBillingAt() != null && exact.getStartBillingAt() != null
                        && dto.startBillingAt().after(exact.getStartBillingAt())) {
                    throw new IllegalStateException("开始计费时间不得晚于已存在记录的开始时间");
                }

                int newQty = (exact.getQty() == null ? 0 : exact.getQty()) + reqQty;

                // 库存校验
                if (plan.getQty() != null && newQty > plan.getQty()) {
                    throw new IllegalStateException("库存不足，套餐剩余数量: " + plan.getQty() + ", 需要数量: " + newQty);
                }

                Date newStart = exact.getStartBillingAt() != null ? exact.getStartBillingAt() : now;
                Date newEnd = OrderItemDTO.calculateEndBilling(newStart, exact.getPlanType(), newQty);

                // 同三键（但排除自身）不得重叠
                List<OrderItem> sameTrios = orderItemMapper.selectList(
                        Wrappers.<OrderItem>lambdaQuery()
                                .eq(OrderItem::getPlanCode, dto.planCode())
                                .eq(OrderItem::getUsername, dto.username())
                                .eq(OrderItem::getStatus, dto.status())
                );
                for (OrderItem oi : sameTrios) {
                    if (oi.getId().equals(exact.getId())) continue;
                    if (isOverlap(newStart, newEnd, oi.getStartBillingAt(), oi.getEndBilling())) {
                        throw new IllegalStateException("时间段重叠，无法合并，请检查同组订单的生效区间");
                    }
                }

                exact.setQty(newQty);
                exact.setStartBillingAt(newStart);
                exact.setEndBilling(newEnd);
                exact.setPrice(calcDiscountedTotalPrice(plan, exact.getPlanType(), newQty));
                exact.setUpdatedAt(now);
                toUpdate.add(exact);
                continue;
            }

            // ===== 四键没命中 -> 查三键（planCode, username, status）=====
            List<OrderItem> sameTrios = orderItemMapper.selectList(
                    Wrappers.<OrderItem>lambdaQuery()
                            .eq(OrderItem::getPlanCode, dto.planCode())
                            .eq(OrderItem::getUsername, dto.username())
                            .eq(OrderItem::getStatus, dto.status())
            );

            if (!sameTrios.isEmpty()) {
                boolean existsForever = sameTrios.stream().anyMatch(oi -> "forever".equals(oi.getPlanType()));

                if ("forever".equals(reqType)) {
                    // 三键命中 + 传入 forever
                    if (existsForever) {
                        throw new IllegalStateException("已存在 forever 类型的同组合订单，不能重复申请 forever");
                    }
                    // 升级一条为 forever（选最新创建），删除其他
                    OrderItem target = sameTrios.stream()
                            .max(Comparator.comparing(OrderItem::getCreatedAt, Comparator.nullsFirst(Date::compareTo)))
                            .orElse(sameTrios.get(0));

                    // 库存校验（用本次数量）
                    if (plan.getQty() != null && reqQty > plan.getQty()) {
                        throw new IllegalStateException("库存不足，套餐剩余数量: " + plan.getQty() + ", 需要数量: " + reqQty);
                    }

                    target.setPlanType("forever");
                    target.setStartBillingAt(now);
                    target.setEndBilling(OrderConstarint.FOREVER_DATE);
                    target.setQty(reqQty);
                    target.setPrice(calcDiscountedTotalPrice(plan, "forever", reqQty));
                    target.setUpdatedAt(now);
                    toUpdate.add(target);

                    for (OrderItem oi : sameTrios) {
                        if (!oi.getId().equals(target.getId())) {
                            toDeleteIds.add(oi.getId());
                        }
                    }
                    continue;
                }

                // 三键命中 + 传入 month/year
                if (existsForever) {
                    // 降级 forever -> month/year（修改这条 forever）
                    OrderItem foreverItem = sameTrios.stream()
                            .filter(oi -> "forever".equals(oi.getPlanType()))
                            .findFirst().orElseThrow();

                    // 库存校验（用本次数量）
                    if (plan.getQty() != null && reqQty > plan.getQty()) {
                        throw new IllegalStateException("库存不足，套餐剩余数量: " + plan.getQty() + ", 需要数量: " + reqQty);
                    }

                    Date newStart = reqStart; // 允许为 now（已归一化）
                    Date newEnd = reqEnd;

                    // 与其它三键记录（排除 forever 这条）不得重叠
                    for (OrderItem oi : sameTrios) {
                        if (oi.getId().equals(foreverItem.getId())) continue;
                        if (isOverlap(newStart, newEnd, oi.getStartBillingAt(), oi.getEndBilling())) {
                            throw new IllegalStateException("时间段重叠，无法从 forever 降级为 " + reqType);
                        }
                    }

                    foreverItem.setPlanType(reqType);
                    foreverItem.setStartBillingAt(newStart);
                    foreverItem.setEndBilling(newEnd);
                    foreverItem.setQty(reqQty);
                    foreverItem.setPrice(calcDiscountedTotalPrice(plan, reqType, reqQty));
                    foreverItem.setUpdatedAt(now);
                    toUpdate.add(foreverItem);
                    continue;
                } else {
                    // 三键存在但无 forever：新增 month/year，且要“尽可能早但不早于最早 start 且不重叠”
                    Date earliestStart = sameTrios.stream()
                            .map(OrderItem::getStartBillingAt)
                            .filter(Objects::nonNull)
                            .min(Date::compareTo)
                            .orElse(now); // 兜底 now

                    Date chosenStart = dto.startBillingAt();
                    if (chosenStart == null) {
                        chosenStart = findEarliestStartSlot(sameTrios, earliestStart, reqType, reqQty);
                        if (chosenStart == null) {
                            throw new IllegalStateException("没有可用的非重叠时间段用于插入新订单区间");
                        }
                    }
                    Date chosenEnd = OrderItemDTO.calculateEndBilling(chosenStart, reqType, reqQty);

                    // 最终保险：与三键现有记录再做一次不重叠校验
                    for (OrderItem oi : sameTrios) {
                        if (isOverlap(chosenStart, chosenEnd, oi.getStartBillingAt(), oi.getEndBilling())) {
                            throw new IllegalStateException("时间段重叠（并发冲突），请重试");
                        }
                    }

                    // 库存校验
                    if (plan.getQty() != null && reqQty > plan.getQty()) {
                        throw new IllegalStateException("库存不足，套餐剩余数量: " + plan.getQty() + ", 需要数量: " + reqQty);
                    }

                    BigDecimal price = calcDiscountedTotalPrice(plan, reqType, reqQty);
                    OrderItem entity = OrderItemDTO.OrderItemAvailable.toEntity(
                            new OrderItemDTO.OrderItemAvailable(
                                    dto.planCode(), chosenStart, reqQty, dto.status(), dto.username(), reqType
                            ),
                            price
                    );
                    toInsert.add(entity);
                    continue;
                }
            }

            // ===== 三键没命中 -> 查二键（username, planCode）=====
            // 仅对状态为「待支付/已支付」的历史施加边界：新开始必须晚于它们
            Date chosenStart = reqStart;
            chosenStart = enforceTwoKeyBoundaryOrThrow(dto.username(), dto.planCode(), chosenStart);

            // 二键没有对三键的“不重叠”要求（因为三键已为空），可直接插入
            // 库存校验
            if (plan.getQty() != null && reqQty > plan.getQty()) {
                throw new IllegalStateException("库存不足，套餐剩余数量: " + plan.getQty() + ", 需要数量: " + reqQty);
            }

            BigDecimal price = calcDiscountedTotalPrice(plan, reqType, reqQty);
            OrderItem entity = OrderItemDTO.OrderItemAvailable.toEntity(
                    new OrderItemDTO.OrderItemAvailable(
                            dto.planCode(), chosenStart, reqQty, dto.status(), dto.username(), reqType
                    ),
                    price
            );
            toInsert.add(entity);
        }

        boolean ok = true;
        if (!toInsert.isEmpty()) ok &= this.saveBatch(toInsert);
        if (!toUpdate.isEmpty()) ok &= this.updateBatchById(toUpdate);
        if (!toDeleteIds.isEmpty()) ok &= orderItemMapper.deleteBatchIds(toDeleteIds) == toDeleteIds.size();

        return ok ? (toInsert.size() + toUpdate.size()) : 0;
    }

    /** 二键规则：同 (username, planCode) 下若有 已支付/待支付 订单，新的开始时间必须晚于它们的最大结束边界
     *  - end=null 视为 +∞，存在则直接不允许再插 */
    private Date enforceTwoKeyBoundaryOrThrow(String username, String planCode, Date requestedStartOrNow) {
        List<OrderItem> paidOrPending = orderItemMapper.selectList(
                Wrappers.<OrderItem>lambdaQuery()
                        .eq(OrderItem::getUsername, username)
                        .eq(OrderItem::getPlanCode, planCode)
                        .in(OrderItem::getStatus, Arrays.asList("待支付", "已支付"))
        );
        if (paidOrPending == null || paidOrPending.isEmpty()) return requestedStartOrNow;

        long maxBoundary = paidOrPending.stream()
                .map(oi -> oi.getEndBilling() == null ? Long.MAX_VALUE : oi.getEndBilling().getTime())
                .max(Long::compare).orElse(Long.MIN_VALUE);

        if (maxBoundary == Long.MAX_VALUE) {
            throw new IllegalStateException("该用户此套餐已存在未结束的订单，无法新增");
        }

        Date boundary = new Date(maxBoundary);
        return requestedStartOrNow.before(boundary) ? boundary : requestedStartOrNow;
    }

    /** 判断时间段是否重叠，区间采用 [start, end)；end=null 代表 +∞ */
    private static boolean isOverlap(Date aStart, Date aEnd, Date bStart, Date bEnd) {
        long aS = aStart == null ? Long.MIN_VALUE : aStart.getTime();
        long aE = aEnd   == null ? Long.MAX_VALUE : aEnd.getTime();
        long bS = bStart == null ? Long.MIN_VALUE : bStart.getTime();
        long bE = bEnd   == null ? Long.MAX_VALUE : bEnd.getTime();
        return aS < bE && bS < aE;
    }

    /** 按折扣计算总价：fee(planType) * (discount/100) * qty，含空值校验 */
    private static BigDecimal calcDiscountedTotalPrice(TariffPlan plan, String planType, int qty) {
        if (planType == null) throw new IllegalArgumentException("planType 不能为空（month/year/forever）");
        if (qty <= 0) qty = 1;

        BigDecimal discount = plan.getDiscount() == null ? BigDecimal.valueOf(100) : plan.getDiscount();
        BigDecimal factor = discount.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

        return switch (planType) {
            case "month" -> {
                BigDecimal fee = plan.getMonthlyFee();
                if (fee == null) throw new IllegalStateException("套餐月费为空");
                yield fee.multiply(factor).multiply(BigDecimal.valueOf(qty));
            }
            case "year" -> {
                BigDecimal fee = plan.getYearlyFee();
                if (fee == null) throw new IllegalStateException("套餐年费为空");
                yield fee.multiply(factor).multiply(BigDecimal.valueOf(qty));
            }
            case "forever" -> {
                BigDecimal fee = plan.getForeverFee();
                if (fee == null) throw new IllegalStateException("套餐永久费为空");
                yield fee.multiply(factor).multiply(BigDecimal.valueOf(qty));
            }
            default -> throw new IllegalArgumentException("未知的套餐类型: " + planType);
        };
    }

    /** 找到最早的不重叠开始时间：不早于 earliestAllowed；若无可用位置返回 null */
    private static Date findEarliestStartSlot(List<OrderItem> sameTrios,
                                              Date earliestAllowed,
                                              String planType,
                                              int qty) {
        if (earliestAllowed == null) return null;

        List<OrderItem> intervals = new ArrayList<>(sameTrios);
        intervals.sort(Comparator.comparing(
                OrderItem::getStartBillingAt,
                Comparator.nullsFirst(Date::compareTo)
        ));

        Date cursor = earliestAllowed;

        for (OrderItem it : intervals) {
            Date iStart = it.getStartBillingAt();
            Date iEnd   = it.getEndBilling();

            long aS = cursor.getTime();
            long iS = (iStart == null ? Long.MIN_VALUE : iStart.getTime());
            long iE = (iEnd   == null ? Long.MAX_VALUE : iEnd.getTime());

            Date candEnd = OrderItemDTO.calculateEndBilling(cursor, planType, qty); // forever -> null
            long cE = (candEnd == null ? Long.MAX_VALUE : candEnd.getTime());

            if (cE <= iS) {
                return cursor;
            }

            if (iE == Long.MAX_VALUE) {
                return null; // 无穷大占满
            }
            cursor = new Date(Math.max(aS, iE));
        }
        return cursor; // 放在最后一个区间之后
    }
    /** 二键硬阻断：同 (username, planCode) 下只要存在【待支付/已支付】且 planType=forever，就禁止再添加任何订单 */
    private void assertNoPaidOrPendingForeverTwoKey(String username, String planCode) {
        // 用 count 更高效
        Long cnt = orderItemMapper.selectCount(
                Wrappers.<OrderItem>lambdaQuery()
                        .eq(OrderItem::getUsername, username)
                        .eq(OrderItem::getPlanCode, planCode)
                        .in(OrderItem::getStatus, Arrays.asList("待支付", "已支付"))
                        .eq(OrderItem::getPlanType, "forever")
        );
        if (cnt != null && cnt > 0) {
            throw new IllegalStateException("该用户此套餐已存在【待支付/已支付】的永久套餐，禁止再添加任何订单项");
        }
    }
}
