package com.xyz.annotation;

import java.lang.annotation.*;

@Documented
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface OpenApiEnumByPrefix {
    String prefix();                           // 常量名前缀，如 "ORDER_ITEM_PLAN_TYPE_"
    Class<?>[] sources() default {};           // 常量所在类（可以多个）
    String exampleDelimiter() default " / ";   // example 的连接符
    boolean caseSensitive() default true;      // 文档层通常无所谓大小写，这里保留选项
}
