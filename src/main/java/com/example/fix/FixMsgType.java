package com.example.fix;

/**
 * FIX消息类型定义
 */
public enum FixMsgType {
    // 会话层消息
    HEARTBEAT("0", "Heartbeat"),
    TEST_REQUEST("1", "TestRequest"),
    RESEND_REQUEST("2", "ResendRequest"),
    REJECT("3", "Reject"),
    SEQUENCE_RESET("4", "SequenceReset"),
    LOGOUT("5", "Logout"),
    LOGON("A", "Logon"),

    // 应用层消息
    NEW_ORDER_SINGLE("D", "NewOrderSingle"),
    EXECUTION_REPORT("8", "ExecutionReport"),
    ORDER_CANCEL_REQUEST("F", "OrderCancelRequest"),
    ORDER_CANCEL_REJECT("9", "OrderCancelReject"),
    ORDER_STATUS_REQUEST("H", "OrderStatusRequest"),

    // 市场数据
    MARKET_DATA_REQUEST("V", "MarketDataRequest"),
    MARKET_DATA_SNAPSHOT("W", "MarketDataSnapshot"),
    MARKET_DATA_INCREMENTAL_REFRESH("X", "MarketDataIncrementalRefresh"),

    // 其他常用消息
    BUSINESS_MESSAGE_REJECT("j", "BusinessMessageReject");

    private final String value;
    private final String name;

    FixMsgType(String value, String name) {
        this.value = value;
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public static FixMsgType fromValue(String value) {
        for (FixMsgType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return null;
    }
}