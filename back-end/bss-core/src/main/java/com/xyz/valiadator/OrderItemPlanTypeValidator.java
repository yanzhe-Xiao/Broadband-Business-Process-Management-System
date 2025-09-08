package com.xyz.valiadator;

import com.xyz.annotation.ValidOrderItemPlanType;
import com.xyz.constraints.OrderConstarint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class OrderItemPlanTypeValidator implements ConstraintValidator<ValidOrderItemPlanType, String> {

    private Set<String> validPlanTypes;

    @Override
    public void initialize(ValidOrderItemPlanType constraintAnnotation) {
        validPlanTypes = new HashSet<>();
        try {
            Field[] fields = OrderConstarint.class.getFields();
            for (Field field : fields) {
                if (field.getName().startsWith("ORDER_ITEM_PLAN_TYPE_")
                        && Modifier.isStatic(field.getModifiers())
                        && Modifier.isFinal(field.getModifiers())) {
                    Object value = field.get(null);
                    if (value instanceof String str) {
                        validPlanTypes.add(str);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("初始化套餐类型校验器失败", e);
        }
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || validPlanTypes.contains(value);
    }
}
