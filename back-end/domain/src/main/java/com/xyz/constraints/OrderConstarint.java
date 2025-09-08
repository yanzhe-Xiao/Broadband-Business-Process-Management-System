package com.xyz.constraints;

import java.util.Calendar;
import java.util.Date;

/**
 * <p>Package Name: com.xyz.constraints </p>
 * <p>Description:  </p>
 * <p>Create Time: 2025/9/5 </p>
 *
 * @author <a href="https://github.com/yanzhe-xiao">YANZHE XIAO</a>
 * @version 1.0
 * @since
 */
public class OrderConstarint {
    public static final Double INSTALLATION_FEE = 100.00;

    public static final Date FOREVER_DATE = createFarFutureDate();

    /** 待支付 */
    public static final String ORDER_STATUS_PENDING_PAYMENT = "待支付";

    /** 已支付 */
    public static final String ORDER_STATUS_PAID = "已支付";

    /** 待派单 */
    public static final String ORDER_STATUS_PENDING_ASSIGNMENT = "待派单";

    /** 已分配工单 */
    public static final String ORDER_STATUS_ASSIGNED_WORK_ORDER = "已分配工单";

    /** 工单已完成 */
    public static final String ORDER_STATUS_WORK_ORDER_COMPLETED = "工单已完成";

    /** 待评价 */
    public static final String ORDER_STATUS_PENDING_REVIEW = "待评价";

    /** 已完成 */
    public static final String ORDER_STATUS_COMPLETED = "已完成";

    /** 已取消 */
    public static final String ORDER_STATUS_CANCELED = "已取消";

    /** orderStatus的示例汇总 */
    public static final String ORDER_STATUS_EXAMPLE = generateOrderExample("ORDER_STATUS_");

    //——————————————-------------------------
    // ORDER_ITEM
    /** 在购物车中 */
    public static final String ORDER_ITEM_STATUS_IN_CART = "在购物车中";

    /** 待支付 */
    public static final String ORDER_ITEM_STATUS_PENDING_PAYMENT = "待支付";

    /** 已支付 */
    public static final String ORDER_ITEM_STATUS_PAID = "已支付";

    /** 已取消 */
    public static final String ORDER_ITEM_STATUS_CANCELED = "已取消";

    /** orderItemStatus的示例汇总 */
    public static final String ORDER_ITEM_STATUS_EXAMLE = generateOrderExample("ORDER_ITEM_STATUS");

    /** 月套餐 */
    public static final String ORDER_ITEM_PLAN_TYPE_MONTH = "month";

    /** 年套餐 */
    public static final String ORDER_ITEM_PLAN_TYPE_YEAR = "year";

    /** 永久套餐 */
    public static final String ORDER_ITEM_PLAN_TYPE_FOREVER = "forever";

    /** orderItemPlanType的示例汇总 */
    public static final String ORDER_ITEM_PLAN_TYPE_EXAMLE = generateOrderExample("ORDER_ITEM_PLAN_TYPE_");


    private static Date createFarFutureDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(2199, Calendar.JANUARY, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * 自动生成示例
     * 会自动收集 OrderConstarint 类中所有以 prefix 开头的常量值
     * @return 字符串
     */
    private static String generateOrderExample(String prefix) {
        java.lang.reflect.Field[] fields = OrderConstarint.class.getFields();

        StringBuilder regexBuilder = new StringBuilder();
        boolean first = true;

        for (java.lang.reflect.Field field : fields) {
            if (field.getName().startsWith(prefix)) {
                try {
                    if (!first) {
                        regexBuilder.append(",");
                    }
                    regexBuilder.append(field.get(null));
                    first = false;
                } catch (IllegalAccessException e) {
                    // 忽略无法访问的字段
                }
            }
        }

        return regexBuilder.toString();
    }


}
