package com.xyz.config;

import com.xyz.annotation.OpenApiEnumByPrefix;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * 按字段注解 @OpenApiEnumByPrefix(prefix=..., sources=...)，
 * 在 OpenAPI 文档中为对应属性自动填充 enum & example。
 *
 * 兼容 springdoc 2.x：
 * - 优先使用 StringSchema#setEnum(List<String>)
 * - 其它类型通过原始类型强转一次性 setEnum，避免通配符捕获报错
 */
@Configuration
public class OpenApiEnumByPrefixAutoFillConfig {

    // 按你的项目实际情况填写：用于把 schemaName（通常是简单类名）映射回 Java 类
    private static final String[] BASE_PACKAGES = {
            "com.xyz.dto"
    };

    // 未在注解 sources() 指定时采用的默认常量类
    private static final Class<?> DEFAULT_CONST_CLASS = com.xyz.constraints.OrderConstarint.class;

    @Bean
    public OpenApiCustomizer enumByPrefixOpenApiCustomizer() {
        return openApi -> {
            var components = openApi.getComponents();
            if (components == null || components.getSchemas() == null) return;

            Map<String, Schema> schemas = components.getSchemas();

            for (Map.Entry<String, Schema> entry : schemas.entrySet()) {
                String schemaName = entry.getKey();
                Schema<?> schema = entry.getValue();

                Class<?> dtoClass = resolveClassBySchemaName(schemaName);
                if (dtoClass == null || schema.getProperties() == null) continue;

                // 扫描 DTO 字段注解
                for (Field field : dtoClass.getDeclaredFields()) {
                    OpenApiEnumByPrefix anno = field.getAnnotation(OpenApiEnumByPrefix.class);
                    if (anno == null) continue;

                    String propName = field.getName();
                    Schema<?> propSchema = (Schema<?>) schema.getProperties().get(propName);
                    if (propSchema == null) continue;

                    Class<?>[] srcs = (anno.sources().length == 0)
                            ? new Class<?>[]{ DEFAULT_CONST_CLASS }
                            : anno.sources();

                    List<String> values = reflectStringsByPrefix(srcs, anno.prefix());
                    applyEnumAndExample(propSchema, values, anno.exampleDelimiter());
                }
            }
        };
    }

    /** 将字符串集合写入 schema 的 enum 与 example，规避泛型通配符捕获问题 */
    private static void applyEnumAndExample(Schema<?> schema, List<String> values, String delimiter) {
        if (values == null || values.isEmpty()) return;

        if (schema instanceof StringSchema ss) {
            // 强类型安全路径：List<String>
            ss.setEnum(values);
        } else {
            // 兜底路径：用原始类型一次性 setEnum，避免 addEnumItemObject 泛型歧义
            @SuppressWarnings({"rawtypes", "unchecked"})
            Schema raw = (Schema) schema;
            @SuppressWarnings({"rawtypes", "unchecked"})
            List rawList = new ArrayList(values); // 擦除成原始 List
            raw.setEnum(rawList);
        }
        schema.setExample(String.join(delimiter, values));
    }

    /** 按 schemaName（简单类名）尝试在设定包里找到对应的 Class */
    private static Class<?> resolveClassBySchemaName(String schemaName) {
        for (String base : BASE_PACKAGES) {
            try {
                return Class.forName(base + "." + schemaName);
            } catch (ClassNotFoundException ignored) {}
        }
        return null;
    }

    /** 从 sources 中收集 public static final String，且字段名以 prefix 开头的常量值（去重保持顺序） */
    private static List<String> reflectStringsByPrefix(Class<?>[] sources, String prefix) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (Class<?> src : sources) {
            for (Field f : src.getFields()) { // 只取 public 字段
                if (isWantedConstant(f, prefix)) {
                    String v = getStringValue(f);
                    if (v != null) set.add(v);
                }
            }
        }
        return new ArrayList<>(set);
    }

    private static boolean isWantedConstant(Field f, String prefix) {
        int m = f.getModifiers();
        return f.getType() == String.class
                && Modifier.isPublic(m)
                && Modifier.isStatic(m)
                && Modifier.isFinal(m)
                && f.getName().startsWith(prefix);
    }

    private static String getStringValue(Field f) {
        try {
            Object o = f.get(null);
            return (o instanceof String) ? (String) o : null;
        } catch (IllegalAccessException e) {
            return null;
        }
    }
}