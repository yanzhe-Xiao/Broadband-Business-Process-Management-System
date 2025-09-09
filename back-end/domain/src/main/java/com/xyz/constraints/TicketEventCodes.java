package com.xyz.constraints;

public enum TicketEventCodes {
    SITE_SURVEY("现场勘察", null),
    WIRING_SPLICING("布线/熔纤", SITE_SURVEY),
    NETWORK_SPEED_TEST("上网测速", WIRING_SPLICING),
    DEVICE_INSTALLATION("设备安装与上电", NETWORK_SPEED_TEST),
    OPTICAL_POWER_TEST("光功率测试", DEVICE_INSTALLATION),
    CUSTOMER_SIGNATURE("用户签字确认", OPTICAL_POWER_TEST);

    private final String description;
    private final TicketEventCodes previous;

    TicketEventCodes(String description, TicketEventCodes previous) {
        this.description = description;
        this.previous = previous;
    }

    public String getDescription() {
        return description;
    }

    public TicketEventCodes getPrevious() {
        return previous;
    }

    // 可以添加获取下一个状态的方法
    public TicketEventCodes getNext() {
        for (TicketEventCodes code : values()) {
            if (code.previous == this) {
                return code;
            }
        }
        return null;
    }

    // 根据description获取对应的枚举值
    public static TicketEventCodes fromDescription(String description) {
        for (TicketEventCodes code : values()) {
            if (code.description.equals(description)) {
                return code;
            }
        }
        return null;
    }

    // 根据枚举名称获取对应的枚举值
    public static TicketEventCodes fromName(String name) {
        for (TicketEventCodes code : values()) {
            if (code.name().equals(name)) {
                return code;
            }
        }
        return null;
    }
}