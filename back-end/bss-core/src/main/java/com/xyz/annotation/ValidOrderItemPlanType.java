package com.xyz.annotation;

import com.xyz.valiadator.OrderItemPlanTypeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = OrderItemPlanTypeValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidOrderItemPlanType {
    String message() default "套餐类型非法";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
