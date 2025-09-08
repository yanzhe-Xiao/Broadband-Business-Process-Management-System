package com.xyz.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xyz.constraints.IpConstraint;
import com.xyz.constraints.OrderConstarint;
import com.xyz.dto.OrderDTO;
import com.xyz.mapper.*;
import com.xyz.orders.OrderItem;
import com.xyz.orders.Orders;
import com.xyz.orders.TariffPlan;
import com.xyz.resources.IpPool;
import com.xyz.resources.ResourceDevice;
import com.xyz.service.OrdersService;
import com.xyz.user.AppUser;
import com.xyz.vo.orders.OrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author X
* @description 针对表【ORDERS(订单表，存储宽带新装/变更/销户等主订单)】的数据库操作Service实现
* @createDate 2025-09-04 14:45:21
*/
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders>
    implements OrdersService{
    @Autowired
    OrderItemMapper orderItemMapper;
    @Autowired
    OrdersMapper ordersMapper;
    @Autowired
    AppUserMapper appUserMapper;
    @Autowired
    TariffPlanMapper tariffPlanMapper;
    @Autowired
    IpPoolMapper ipPoolMapper;
    @Autowired
    ResourceDeviceMapper resourceDeviceMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int commitOrder(OrderDTO.OrderAvaliableDTO dto) {
        //TODO 判断oderItemId里的user是不是这里提供的user √
        //TODO 在创建订单的时候就应先不分配ip和但扣除库存了 扣除套餐的数量 扣除前应先校验数量 付款后再进行ip的分配 √
        //TODO 需要删掉dto里的status，应默认直接设置为待支付 √
        //TODO 如果一个user已有一个待支付的订单则不能进行新的下单 √
        //TODO 下订单的时候检查OrderItem的planCode是否为在购物车中 √
        //TODO 在增加Traffiplan时应该注意根据当前的device和ip判断最大库存是多少 √
        Long userId = appUserMapper.selectIdByUsername(dto.username());
        List<Orders> orders = ordersMapper.selectAllByUserIdAndStatus(userId, OrderConstarint.ORDER_STATUS_PENDING_PAYMENT);
        if(!orders.isEmpty()){
            throw new IllegalArgumentException("存在有未支付的订单");
        }
        // 验证订单项是否属于指定用户
        List<Long> orderItemIds = dto.orderItemId();
        if (orderItemIds != null && !orderItemIds.isEmpty()) {
            List<String> itemUsernames = orderItemMapper.selectUsernamesByIds(orderItemIds);
            // 检查所有订单项是否属于同一个用户，且与当前用户一致
            boolean allBelongToUser = itemUsernames.stream()
                    .allMatch(username -> username != null && username.equals(dto.username()));

            if (!allBelongToUser) {
                throw new IllegalArgumentException("订单项与用户不匹配");
            }

            // TODO: 检查订单项是否为购物车状态
            List<String> itemStatusList = orderItemMapper.selectStatusByIds(orderItemIds);
            boolean allInCart = itemStatusList.stream()
                    .allMatch(status -> status != null && status.equals(OrderConstarint.ORDER_ITEM_STATUS_IN_CART)); // 假设购物车状态为"购物车"

            if (!allInCart) {
                throw new IllegalArgumentException("存在非购物车状态的订单项，无法提交订单");
            }
        }

        // 1. 创建订单实体
        Orders entity = OrderDTO.OrderAvaliableDTO.toEntity(dto,userId);
        int insert = ordersMapper.insert(entity);

        if (insert > 0) {
            // 2. 批量更新订单项的订单ID和状态
//            List<Long> orderItemIds = dto.orderItemId();
            if (orderItemIds != null && !orderItemIds.isEmpty()) {
                orderItemMapper.batchUpdateOrderIdById(entity.getId(), orderItemIds);
                orderItemMapper.batchUpdateStatusAndExpireById(OrderConstarint.ORDER_ITEM_STATUS_PENDING_PAYMENT, orderItemIds);
            }
        } else {
            throw new RuntimeException("提交订单失败，未知原因，请联系客服");
        }
        return insert;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int payOrder(OrderDTO.OrderPaymentDTO paymentDTO) {
        // 1) 根据用户名查 userId
        Long userId = appUserMapper.selectIdByUsername(paymentDTO.username());
        if (userId == null) {
            throw new IllegalArgumentException("用户名不存在: " + paymentDTO.username());
        }

        // 2) 查订单并校验归属 & 状态
        Orders order = ordersMapper.selectById(paymentDTO.orderId());
        if (order == null) {
            throw new IllegalArgumentException("订单不存在: " + paymentDTO.orderId());
        }
        if (!Objects.equals(order.getUserId(), userId)) {
            throw new SecurityException("无权操作该订单");
        }
        if (!OrderConstarint.ORDER_STATUS_PENDING_PAYMENT.equals(order.getStatus())) {
            throw new IllegalStateException("当前订单状态不允许支付: " + order.getStatus());
        }

        // 3) 更新订单状态为“已支付”
        int rows = ordersMapper.update(
                null,
                Wrappers.<Orders>update()
                        .eq("id", paymentDTO.orderId())
                        .eq("user_id", userId)
                        .set("status", OrderConstarint.ORDER_STATUS_PAID)
        );
        if (rows <= 0) {
            throw new RuntimeException("订单支付更新失败，请稍后重试");
        }

        // 4) 批量更新订单项状态：待支付 -> 已支付
        orderItemMapper.update(
                null,
                Wrappers.<OrderItem>update()
                        .eq("username", paymentDTO.username())
                        .eq("order_id", paymentDTO.orderId())
                        .eq("status", OrderConstarint.ORDER_ITEM_STATUS_PENDING_PAYMENT)
                        .set("status", OrderConstarint.ORDER_ITEM_STATUS_PAID)
        );

        // 5) 查询本订单下的订单项
        List<OrderItem> orderItems = orderItemMapper.selectList(
                Wrappers.<OrderItem>lambdaQuery()
                        .eq(OrderItem::getOrderId, paymentDTO.orderId())
                        .eq(OrderItem::getUsername, paymentDTO.username())
        );
        if (orderItems.isEmpty()) {
            return rows;
        }

        // 5.1) 历史已支付的订单项（带 IP）
        List<OrderItem> paidSameUserItems = orderItemMapper.selectList(
                Wrappers.<OrderItem>lambdaQuery()
                        .eq(OrderItem::getUsername, paymentDTO.username())
                        .eq(OrderItem::getStatus, OrderConstarint.ORDER_ITEM_STATUS_PAID)
                        .isNotNull(OrderItem::getIp)
        );

        // 拿到这些订单的 endTime
        Set<Long> paidOrderIds = paidSameUserItems.stream()
                .map(OrderItem::getOrderId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Orders> paidOrderMap = paidOrderIds.isEmpty()
                ? Collections.emptyMap()
                : ordersMapper.selectBatchIds(paidOrderIds).stream()
                .collect(Collectors.toMap(Orders::getId, o -> o));

        // 可复用 IP 表（历史 + 本次）
        Map<String, String> planIpReuseMap = new HashMap<>();
        Date now = new Date();
        for (OrderItem it : paidSameUserItems) {
            Date itemEndTime = it.getEndTime();   // 直接从订单项里拿 endTime
            if (itemEndTime != null && itemEndTime.after(now)) {
                planIpReuseMap.putIfAbsent(it.getPlanCode(), it.getIp());
            }
        }

        // 5.2) 遍历订单项
        for (OrderItem item : orderItems) {
            TariffPlan plan = tariffPlanMapper.selectById(item.getPlanCode());
            if (plan == null) continue;

            // ===== 分配 IP =====
            if (plan.getIsIp() != null && plan.getIsIp() == 1) {
                int requiredBw = (plan.getBandwith() != null && plan.getBandwith() > 0) ? plan.getBandwith() : 0;

                // 先尝试复用
                String reusableIp = planIpReuseMap.get(item.getPlanCode());
                if (reusableIp != null) {
                    orderItemMapper.update(
                            null,
                            Wrappers.<OrderItem>update()
                                    .eq("id", item.getId())
                                    .set("ip", reusableIp)
                    );
                } else {
                    // 无可复用 → 从池里挑一个 best-fit IP
                    IpPool ip = ipPoolMapper.selectOne(
                            Wrappers.<IpPool>lambdaQuery()
                                    .eq(IpPool::getStatus, "FREE")
                                    .apply("NVL(avaliable_bandwidth, ip_bandwidth) >= {0}", requiredBw)
                                    .last("ORDER BY NVL(avaliable_bandwidth, ip_bandwidth) ASC, updated_at ASC FETCH FIRST 1 ROWS ONLY")
                    );
                    if (ip == null) {
                        throw new RuntimeException("无可用 IP 资源（满足带宽 " + requiredBw + " 的 FREE IP 不存在）");
                    }

                    int affected = ipPoolMapper.update(
                            null,
                            Wrappers.<IpPool>update()
                                    .eq("id", ip.getId())
                                    .eq("status", "FREE")
                                    .apply("NVL(avaliable_bandwidth, ip_bandwidth) >= {0}", requiredBw)
                                    .setSql("avaliable_bandwidth = NVL(avaliable_bandwidth, ip_bandwidth) - " + requiredBw)
                                    .setSql("status = CASE WHEN NVL(avaliable_bandwidth, ip_bandwidth) - " + requiredBw + " <= 0 "
                                            + "THEN '" + IpConstraint.IP_STATUS_NOAVALIABLE + "' ELSE 'FREE' END")
                    );
                    if (affected == 0) {
                        throw new RuntimeException("IP 资源被抢占，请重试");
                    }

                    orderItemMapper.update(
                            null,
                            Wrappers.<OrderItem>update()
                                    .eq("id", item.getId())
                                    .set("ip", ip.getIp())
                    );

                    // 本次支付后续相同 planCode 直接复用
                    planIpReuseMap.put(item.getPlanCode(), ip.getIp());
                }
            }

            // ===== 分配设备 =====
            if (plan.getDeviceQty() != null && plan.getDeviceQty() > 0) {
                int needDevices = plan.getDeviceQty() * Optional.ofNullable(item.getQty()).orElse(1);

                ResourceDevice device = resourceDeviceMapper.selectOne(
                        Wrappers.<ResourceDevice>lambdaQuery()
                                .eq(ResourceDevice::getStatus, "STOCK")
                                .ge(ResourceDevice::getQty, needDevices)
                                .eq(ResourceDevice::getSn, plan.getDeviceSn())
                );
                if (device == null) {
                    throw new RuntimeException("设备库存不足或设备SN不匹配，请联系客服");
                }

                int devAffected = resourceDeviceMapper.update(
                        null,
                        Wrappers.<ResourceDevice>update()
                                .eq("id", device.getId())
                                .eq("status", "STOCK")
                                .ge("qty", needDevices)
                                .setSql("qty = qty - " + needDevices)
                                .set("status", (device.getQty() - needDevices) > 0 ? "STOCK" : "ASSIGNED")
                );
                if (devAffected == 0) {
                    throw new RuntimeException("设备库存并发更新冲突，请重试");
                }

                orderItemMapper.update(
                        null,
                        Wrappers.<OrderItem>update()
                                .eq("id", item.getId())
                                .set("device_sn", device.getSn())
                );
            }
        }

        return rows;
    }

    @Override
    @Transactional(readOnly = true)
    public IPage<OrderVO.OrderLookVO> getOrder(int current, int size, String username) {
        int pageNo = Math.max(1, current);
        int pageSize = size <= 0 ? 10 : size;

        // 0) username -> userId
        Long userId = appUserMapper.selectIdByUsername(username);
        if (userId == null) {
            // 返回空页
            return new Page<>(pageNo, pageSize, 0);
        }

        // 1) 按订单维度分页
        Page<Orders> orderPage = new Page<>(pageNo, pageSize);
        IPage<Orders> oPage = ordersMapper.selectPage(
                orderPage,
                Wrappers.<Orders>lambdaQuery()
                        .eq(Orders::getUserId, userId)
                        .orderByDesc(Orders::getId)
        );

        List<Orders> orders = oPage.getRecords();
        if (orders == null || orders.isEmpty()) {
            return new Page<>(pageNo, pageSize, oPage.getTotal());
        }

        // 2) 批量查订单项
        List<Long> orderIds = orders.stream().map(Orders::getId).toList();
        List<OrderItem> allItems = orderItemMapper.selectList(
                Wrappers.<OrderItem>lambdaQuery()
                        .in(OrderItem::getOrderId, orderIds)
                        .eq(OrderItem::getUsername, username)
        );
        Map<Long, List<OrderItem>> itemsByOrder = allItems.stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId));

        // 3) 批量查套餐
        Set<String> planCodes = allItems.stream()
                .map(OrderItem::getPlanCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<String, TariffPlan> planMap = planCodes.isEmpty()
                ? Collections.emptyMap()
                : tariffPlanMapper.selectBatchIds(planCodes).stream()
                .collect(Collectors.toMap(TariffPlan::getPlanCode, p -> p));

        // 4) 组装 VO（builder）
        List<OrderVO.OrderLookVO> voList = new ArrayList<>(orders.size());
        for (Orders o : orders) {
            List<OrderItem> group = itemsByOrder.getOrDefault(o.getId(), Collections.emptyList());

            BigDecimal totalPrice = BigDecimal.ZERO;
            List<OrderVO.OrderItemDetailVO> itemVOs = new ArrayList<>(group.size());

            for (OrderItem item : group) {
                TariffPlan p = planMap.get(item.getPlanCode());

                // 计算单价：按 planType 选择套餐价格
                BigDecimal unit = BigDecimal.ZERO;
                if (p != null && item.getPlanType() != null) {
                    switch (item.getPlanType()) {
                        case "month"   -> unit = p.getMonthlyFee();
                        case "year"    -> unit = p.getYearlyFee();
                        case "forever" -> unit = p.getForeverFee();
                        default        -> unit = BigDecimal.ZERO;
                    }
                }
                if (unit == null) unit = BigDecimal.ZERO;

                int qty = Optional.ofNullable(item.getQty()).orElse(1);
                BigDecimal discount = Optional.ofNullable(p != null ? p.getDiscount() : null)
                        .orElse(BigDecimal.valueOf(100));

                BigDecimal itemPrice = unit
                        .multiply(BigDecimal.valueOf(qty))
                        .multiply(discount)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

                totalPrice = totalPrice.add(itemPrice);

                itemVOs.add(OrderVO.OrderItemDetailVO.builder()
                        .planCode(item.getPlanCode())
                        .planName(p != null ? p.getName() : null)
                        .planType(item.getPlanType())
                        .qty(qty)
                        .unitPrice(unit)
                        .discount(discount)
                        .itemPrice(itemPrice)
                        .description(p != null ? p.getDescription() : null)
                        .ip(item.getIp())
                        .endTime(item.getEndTime())
                        .build());
            }

            voList.add(OrderVO.OrderLookVO.builder()
                    .id(o.getId())
                    .status(o.getStatus())
                    .createdAt(o.getCreatedAt())
                    .installAddress(o.getInstallAddress())
//                    .endTime(o.getEndTime())
                    .price(totalPrice)
                    .items(itemVOs)
                    .build());
        }

        // 5) 返回分页结果（与订单分页对齐）
        Page<OrderVO.OrderLookVO> voPage = new Page<>(pageNo, pageSize, oPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteOrder(OrderDTO.OrderDeleteDTO deleteDTO) {
        if (deleteDTO == null || deleteDTO.orderId() == null || deleteDTO.orderId().isEmpty()) {
            return 0;
        }

        // 1) 根据用户名查 userId
        Long userId = appUserMapper.selectIdByUsername(deleteDTO.username());
        if (userId == null) {
            throw new IllegalArgumentException("用户名不存在: " + deleteDTO.username());
        }

        // 2) 批量查订单，校验归属
        List<Orders> orders = ordersMapper.selectBatchIds(deleteDTO.orderId());
        if (orders.isEmpty()) {
            throw new IllegalArgumentException("所选订单不存在");
        }

        for (Orders o : orders) {
            if (!Objects.equals(o.getUserId(), userId)) {
                throw new SecurityException("订单 " + o.getId() + " 不属于用户 " + deleteDTO.username());
            }
            if(o.getStatus().equals(OrderConstarint.ORDER_STATUS_WORK_ORDER_COMPLETED)
                    || o.getStatus().equals(OrderConstarint.ORDER_STATUS_PENDING_REVIEW)
                    ||o.getStatus().equals(OrderConstarint.ORDER_STATUS_COMPLETED)){
                throw new IllegalArgumentException("此时订单的状态不可被删除");
            }
        }

        // 3) 删除订单（逻辑删 or 物理删）
        int affected = ordersMapper.deleteBatchIds(deleteDTO.orderId());
        if (affected <= 0) {
            throw new RuntimeException("删除订单失败");
        }

        // 如果需要级联删除订单项，可在这里加：
        // orderItemMapper.delete(Wrappers.<OrderItem>lambdaQuery()
        //        .in(OrderItem::getOrderId, deleteDTO.orderId())
        //        .eq(OrderItem::getUsername, deleteDTO.username()));

        return affected;
    }
}




