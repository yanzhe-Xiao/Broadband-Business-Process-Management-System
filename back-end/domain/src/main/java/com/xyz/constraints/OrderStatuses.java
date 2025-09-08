package com.xyz.constraints;

public final class OrderStatuses {
    public static final String PENDING_PAYMENT  = "待支付";
    public static final String PAID             = "已支付";
    public static final String PENDING_DISPATCH = "待派单";
    public static final String DISPATCHED       = "已分配工单";
    public static final String WORK_DONE        = "工单已完成";
    public static final String TO_REVIEW        = "待评价";
    public static final String COMPLETED        = "已完成";
    public static final String CANCELED         = "已取消";
    private OrderStatuses() {}
}
