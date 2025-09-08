package com.xyz.annotation;

import com.xyz.valiadator.OrderItemStatusValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = OrderItemStatusValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidOrderItemStatus {
    String message() default "订单状态非法";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
