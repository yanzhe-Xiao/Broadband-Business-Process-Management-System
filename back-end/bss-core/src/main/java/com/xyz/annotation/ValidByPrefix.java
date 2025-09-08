package com.xyz.annotation;

import com.xyz.valiadator.ByPrefixValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ByPrefixValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidByPrefix {

    /**
     * 要匹配的常量名前缀，例如 "ORDER_ITEM_STATUS_" / "ORDER_ITEM_PLAN_TYPE_"
     */
    String prefix();

    /**
     * 从哪些类上收集常量，必须是 public static final String
     */
    Class<?>[] sources() default { com.xyz.constraints.OrderConstarint.class };

    /**
     * 是否必须有值（null/空串是否允许）
     */
    boolean required() default true;

    /**
     * 比较是否区分大小写
     */
    boolean caseSensitive() default true;

    /**
     * 在比较前是否 trim()
     */
    boolean trimmed() default true;

    String message() default "值不在允许的常量集合内（前缀: {prefix}）";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
