package com.xyz.valiadator;

import com.xyz.annotation.ValidOrderItemStatus;
import com.xyz.constraints.OrderConstarint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class OrderItemStatusValidator implements ConstraintValidator<ValidOrderItemStatus, String> {

    private Set<String> validStatuses;

    @Override
    public void initialize(ValidOrderItemStatus constraintAnnotation) {
        validStatuses = new HashSet<>();
        try {
            Field[] fields = OrderConstarint.class.getFields();
            for (Field field : fields) {
                if (field.getName().startsWith("ORDER_ITEM_STATUS_")
                        && Modifier.isStatic(field.getModifiers())
                        && Modifier.isFinal(field.getModifiers())) {
                    Object value = field.get(null);
                    if (value instanceof String str) {
                        validStatuses.add(str);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("初始化订单状态校验器失败", e);
        }
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || validStatuses.contains(value);
    }
}
