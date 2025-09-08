package com.xyz.valiadator;

import com.xyz.annotation.ValidByPrefix;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ByPrefixValidator implements ConstraintValidator<ValidByPrefix, String> {

    private static final Map<String, Set<String>> CACHE = new ConcurrentHashMap<>();

    private String prefix;
    private Class<?>[] sources;
    private boolean required;
    private boolean caseSensitive;
    private boolean trimmed;

    private Set<String> allowed;       // 实际用于匹配（根据大小写/trim 处理后的集合）
    private List<String> displayList;  // 用于提示展示的原始列表（未大小写变换，便于报错提示）

    @Override
    public void initialize(ValidByPrefix anno) {
        this.prefix = anno.prefix();
        this.sources = anno.sources();
        this.required = anno.required();
        this.caseSensitive = anno.caseSensitive();
        this.trimmed = anno.trimmed();

        // 缓存键：sources 类名 + prefix + caseSensitive（大小写敏感会影响缓存 key）
        String cacheKey = buildCacheKey(sources, prefix, caseSensitive);

        // 从缓存拿（值集合用于匹配的版本）
        this.allowed = CACHE.computeIfAbsent(cacheKey, k -> {
            Set<String> set = new LinkedHashSet<>();
            for (Class<?> src : sources) {
                // 只取 public 字段；按需要你也可以改用 getDeclaredFields() 后逐一 setAccessible(true)
                for (Field f : src.getFields()) {
                    if (isWantedConstant(f, prefix)) {
                        String v = getStringValue(f);
                        if (v != null) {
                            set.add(caseSensitive ? v : v.toLowerCase(Locale.ROOT));
                        }
                    }
                }
            }
            return Collections.unmodifiableSet(set);
        });

        // 展示列表（用于错误信息），给用户看原值
        this.displayList = new ArrayList<>();
        for (Class<?> src : sources) {
            for (Field f : src.getFields()) {
                if (isWantedConstant(f, prefix)) {
                    String v = getStringValue(f);
                    if (v != null) displayList.add(v);
                }
            }
        }
        // 去重并保持插入顺序
        LinkedHashSet<String> dedup = new LinkedHashSet<>(displayList);
        this.displayList = new ArrayList<>(dedup);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || (trimmed && value.trim().isEmpty()) || (!trimmed && value.isEmpty())) {
            return !required;
        }
        if (allowed == null || allowed.isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "校验器配置错误：前缀 " + prefix + " 未匹配到任何常量（请检查常量名或排除规则）"
            ).addConstraintViolation();
            return false;
        }
        String candidate = trimmed ? value.trim() : value;
        if (!caseSensitive) candidate = candidate.toLowerCase(Locale.ROOT);
        boolean ok = allowed.contains(candidate);
        if (!ok) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "值必须为以下之一: " + String.join(" / ", displayList)
            ).addConstraintViolation();
        }
        return ok;
    }


    private static final Set<String> EXCLUDE_NAME_KEYWORDS = Set.of(
            "EXAMPLE", "EXAMLE", "ALL", "LIST", "ARRAY", "REGEX", "REGAX"
    );

    private static boolean isWantedConstant(Field f, String prefix) {
        int m = f.getModifiers();
        String name = f.getName();
        if (!(f.getType() == String.class
                && Modifier.isPublic(m)
                && Modifier.isStatic(m)
                && Modifier.isFinal(m)
                && name.startsWith(prefix))) {
            return false;
        }
        // 过滤掉示例/集合/正则等“非值”字段
        String upper = name.toUpperCase(Locale.ROOT);
        for (String kw : EXCLUDE_NAME_KEYWORDS) {
            if (upper.contains(kw)) return false;
        }
        return true;
    }

    private static String getStringValue(Field f) {
        try {
            Object o = f.get(null);
            if (!(o instanceof String v)) return null;
            // 过滤明显是“拼接串/示例串”的值（避免把 "a / b" 或 "a,b" 收进来）
            String s = v.trim();
            if (s.isEmpty()) return null;
            if (s.contains("/") || s.contains(",") || s.contains("|")) return null;
            return s;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    private static String buildCacheKey(Class<?>[] sources, String prefix, boolean caseSensitive) {
        StringBuilder sb = new StringBuilder(prefix)
                .append("|case:").append(caseSensitive).append("|src:");
        for (Class<?> c : sources) {
            sb.append(c.getName()).append(";");
        }
        return sb.toString();
    }
}
