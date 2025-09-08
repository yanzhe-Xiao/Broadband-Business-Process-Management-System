package com.xyz.constraints;

/**
 * <p>Package Name: com.xyz.constant </p>
 * <p>Description: 约束IP常量 </p>
 * <p>Create Time: 2025/9/4 </p>
 *
 * @author <a href="https://github.com/yanzhe-xiao">YANZHE XIAO</a>
 * @version 1.0
 * @since
 */
public class IpConstraint {
    public final static String IP_STATUS_AVALIABLE = "FREE";
    public final static String IP_STATUS_NOAVALIABLE = "ASSIGNED";

    public static final String IPV4_STRICT =
            "^(?:(?:25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\.){3}"
                    +   "(?:25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)$";
}
