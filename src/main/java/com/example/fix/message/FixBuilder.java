package com.example.fix.message;

import com.example.fix.exception.FixException;
import com.example.fix.types.FixFieldType;
import com.example.fix.types.FixMsgType;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * FIX消息构建器
 */
public class FixBuilder {
    private static final DateTimeFormatter FIX_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss");

    private final FixMessage message;
    private String senderCompId;
    private String targetCompId;
    private int msgSeqNum;

    public FixBuilder(String msgType) {
        this.message = new FixMessage();
        message.setField(FixFieldType.BEGIN_STRING, "FIX.4.4");
        message.setField(FixFieldType.MSG_TYPE, msgType);
    }

    public FixBuilder(FixMsgType msgType) {
        this(msgType.getValue());
    }

    /**
     * 设置发送方
     */
    public FixBuilder sender(String senderCompId) {
        this.senderCompId = senderCompId;
        message.setField(FixFieldType.SENDER_COMP_ID, senderCompId);
        return this;
    }

    /**
     * 设置接收方
     */
    public FixBuilder target(String targetCompId) {
        this.targetCompId = targetCompId;
        message.setField(FixFieldType.TARGET_COMP_ID, targetCompId);
        return this;
    }

    /**
     * 设置消息序列号
     */
    public FixBuilder msgSeqNum(int msgSeqNum) {
        this.msgSeqNum = msgSeqNum;
        message.setField(FixFieldType.MSG_SEQ_NUM, String.valueOf(msgSeqNum));
        return this;
    }

    /**
     * 设置发送时间(使用当前时间)
     */
    public FixBuilder sendingTime() {
        String sendingTime = LocalDateTime.now().format(FIX_TIME_FORMAT);
        message.setField(FixFieldType.SENDING_TIME, sendingTime);
        return this;
    }

    /**
     * 设置发送时间(指定时间)
     */
    public FixBuilder sendingTime(LocalDateTime dateTime) {
        String sendingTime = dateTime.format(FIX_TIME_FORMAT);
        message.setField(FixFieldType.SENDING_TIME, sendingTime);
        return this;
    }

    /**
     * 设置发送时间(字符串格式)
     */
    public FixBuilder sendingTime(String sendingTime) {
        message.setField(FixFieldType.SENDING_TIME, sendingTime);
        return this;
    }

    /**
     * 添加字段
     */
    public FixBuilder field(int tag, String value) {
        message.setField(tag, value);
        return this;
    }

    public FixBuilder field(FixFieldType fieldType, String value) {
        message.setField(fieldType, value);
        return this;
    }

    public FixBuilder field(int tag, int value) {
        message.setField(tag, String.valueOf(value));
        return this;
    }

    public FixBuilder field(FixFieldType fieldType, int value) {
        message.setField(fieldType, String.valueOf(value));
        return this;
    }

    public FixBuilder field(int tag, double value) {
        message.setField(tag, String.valueOf(value));
        return this;
    }

    public FixBuilder field(FixFieldType fieldType, double value) {
        message.setField(fieldType, String.valueOf(value));
        return this;
    }

    /**
     * 构建消息
     */
    public FixMessage build() throws FixException {
        // 验证必需字段
        if (senderCompId == null) {
            throw new FixException("SenderCompID is required");
        }
        if (targetCompId == null) {
            throw new FixException("TargetCompID is required");
        }
        if (msgSeqNum == 0) {
            throw new FixException("MsgSeqNum is required");
        }
        if (!message.hasField(FixFieldType.SENDING_TIME)) {
            sendingTime(); // 使用当前时间
        }

        return message;
    }

    /**
     * 创建Logon消息
     */
    public static FixBuilder logon() {
        return new FixBuilder(FixMsgType.LOGON);
    }

    /**
     * 创建Logout消息
     */
    public static FixBuilder logout() {
        return new FixBuilder(FixMsgType.LOGOUT);
    }

    /**
     * 创建Heartbeat消息
     */
    public static FixBuilder heartbeat() {
        return new FixBuilder(FixMsgType.HEARTBEAT);
    }

    /**
     * 创建TestRequest消息
     */
    public static FixBuilder testRequest(String testReqId) {
        return new FixBuilder(FixMsgType.TEST_REQUEST)
                .field(FixFieldType.TEST_REQ_ID, testReqId);
    }

    /**
     * 创建NewOrderSingle消息
     */
    public static FixBuilder newOrderSingle() {
        return new FixBuilder(FixMsgType.NEW_ORDER_SINGLE);
    }

    /**
     * 创建ExecutionReport消息
     */
    public static FixBuilder executionReport() {
        return new FixBuilder(FixMsgType.EXECUTION_REPORT);
    }
}