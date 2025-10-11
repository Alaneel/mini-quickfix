package com.example.fix.types;

/**
 * 常用FIX字段类型定义
 */
public enum FixFieldType {
    // 标准消息头字段
    BEGIN_STRING(8, "BeginString"),
    BODY_LENGTH(9, "BodyLength"),
    MSG_TYPE(35, "MsgType"),
    SENDER_COMP_ID(49, "SenderCompID"),
    TARGET_COMP_ID(56, "TargetCompID"),
    MSG_SEQ_NUM(34, "MsgSeqNum"),
    SENDING_TIME(52, "SendingTime"),
    CHECKSUM(10, "CheckSum"),

    // 常用业务字段
    ACCOUNT(1, "Account"),
    CL_ORD_ID(11, "ClOrdID"),
    ORDER_ID(37, "OrderID"),
    EXEC_ID(17, "ExecID"),
    EXEC_TYPE(150, "ExecType"),
    ORD_STATUS(39, "OrdStatus"),
    SYMBOL(55, "Symbol"),
    SIDE(54, "Side"),
    ORDER_QTY(38, "OrderQty"),
    PRICE(44, "Price"),
    TIME_IN_FORCE(59, "TimeInForce"),
    ORD_TYPE(40, "OrdType"),
    TRANSACT_TIME(60, "TransactTime"),
    TEXT(58, "Text"),

    // 拒绝相关
    REF_SEQ_NUM(45, "RefSeqNum"),
    REF_MSG_TYPE(372, "RefMsgType"),
    SESSION_REJECT_REASON(373, "SessionRejectReason"),

    // 心跳和测试
    TEST_REQ_ID(112, "TestReqID"),
    HEARTBT_INT(108, "HeartBtInt");

    private final int tag;
    private final String name;

    FixFieldType(int tag, String name) {
        this.tag = tag;
        this.name = name;
    }

    public int getTag() {
        return tag;
    }

    public String getName() {
        return name;
    }

    public static FixFieldType fromTag(int tag) {
        for (FixFieldType type : values()) {
            if (type.tag == tag) {
                return type;
            }
        }
        return null;
    }
}