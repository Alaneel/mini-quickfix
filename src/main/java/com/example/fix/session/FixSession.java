package com.example.fix.session;

import com.example.fix.exception.FixException;
import com.example.fix.message.FixBuilder;
import com.example.fix.message.FixMessage;
import com.example.fix.types.FixFieldType;
import com.example.fix.types.FixMsgType;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * FIX会话管理类
 */
public class FixSession {
    private final String senderCompId;
    private final String targetCompId;
    private final AtomicInteger outgoingSeqNum;
    private final AtomicInteger incomingSeqNum;
    private volatile boolean loggedOn;
    private LocalDateTime lastMessageTime;
    private final int heartbeatInterval; // 秒

    public FixSession(String senderCompId, String targetCompId, int heartbeatInterval) {
        this.senderCompId = senderCompId;
        this.targetCompId = targetCompId;
        this.heartbeatInterval = heartbeatInterval;
        this.outgoingSeqNum = new AtomicInteger(1);
        this.incomingSeqNum = new AtomicInteger(1);
        this.loggedOn = false;
        this.lastMessageTime = LocalDateTime.now();
    }

    /**
     * 创建Logon消息
     */
    public FixMessage createLogon() throws FixException {
        FixMessage message = FixBuilder.logon()
                .sender(senderCompId)
                .target(targetCompId)
                .msgSeqNum(outgoingSeqNum.getAndIncrement())
                .sendingTime()
                .field(FixFieldType.HEARTBT_INT, heartbeatInterval)
                .build();

        loggedOn = true;
        lastMessageTime = LocalDateTime.now();
        return message;
    }

    /**
     * 创建Logout消息
     */
    public FixMessage createLogout() throws FixException {
        return createLogout(null);
    }

    public FixMessage createLogout(String text) throws FixException {
        FixBuilder builder = FixBuilder.logout()
                .sender(senderCompId)
                .target(targetCompId)
                .msgSeqNum(outgoingSeqNum.getAndIncrement())
                .sendingTime();

        if (text != null) {
            builder.field(FixFieldType.TEXT, text);
        }

        FixMessage message = builder.build();
        loggedOn = false;
        lastMessageTime = LocalDateTime.now();
        return message;
    }

    /**
     * 创建Heartbeat消息
     */
    public FixMessage createHeartbeat() throws FixException {
        return createHeartbeat(null);
    }

    public FixMessage createHeartbeat(String testReqId) throws FixException {
        FixBuilder builder = FixBuilder.heartbeat()
                .sender(senderCompId)
                .target(targetCompId)
                .msgSeqNum(outgoingSeqNum.getAndIncrement())
                .sendingTime();

        if (testReqId != null) {
            builder.field(FixFieldType.TEST_REQ_ID, testReqId);
        }

        FixMessage message = builder.build();
        lastMessageTime = LocalDateTime.now();
        return message;
    }

    /**
     * 创建TestRequest消息
     */
    public FixMessage createTestRequest(String testReqId) throws FixException {
        FixMessage message = FixBuilder.testRequest(testReqId)
                .sender(senderCompId)
                .target(targetCompId)
                .msgSeqNum(outgoingSeqNum.getAndIncrement())
                .sendingTime()
                .build();

        lastMessageTime = LocalDateTime.now();
        return message;
    }

    /**
     * 创建业务消息
     */
    public FixMessage createMessage(String msgType) throws FixException {
        FixMessage message = new FixBuilder(msgType)
                .sender(senderCompId)
                .target(targetCompId)
                .msgSeqNum(outgoingSeqNum.getAndIncrement())
                .sendingTime()
                .build();

        lastMessageTime = LocalDateTime.now();
        return message;
    }

    /**
     * 处理接收到的消息
     */
    public void processIncomingMessage(FixMessage message) throws FixException {
        // 验证发送方和接收方
        String msgSender = message.getFieldValue(FixFieldType.SENDER_COMP_ID);
        String msgTarget = message.getFieldValue(FixFieldType.TARGET_COMP_ID);

        if (!targetCompId.equals(msgSender)) {
            throw new FixException("Invalid SenderCompID: expected " + targetCompId +
                    ", got " + msgSender);
        }

        if (!senderCompId.equals(msgTarget)) {
            throw new FixException("Invalid TargetCompID: expected " + senderCompId +
                    ", got " + msgTarget);
        }

        // 验证序列号
        int msgSeqNum = Integer.parseInt(
                message.getFieldValue(FixFieldType.MSG_SEQ_NUM));
        int expectedSeqNum = incomingSeqNum.get();

        if (msgSeqNum < expectedSeqNum) {
            throw new FixException("Sequence number too low: expected " +
                    expectedSeqNum + ", got " + msgSeqNum);
        } else if (msgSeqNum > expectedSeqNum) {
            throw new FixException("Sequence number too high: expected " +
                    expectedSeqNum + ", got " + msgSeqNum);
        }

        incomingSeqNum.incrementAndGet();
        lastMessageTime = LocalDateTime.now();
    }

    /**
     * 检查是否需要发送心跳
     */
    public boolean needsHeartbeat() {
        if (!loggedOn) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        long secondsSinceLastMessage =
                java.time.Duration.between(lastMessageTime, now).getSeconds();

        return secondsSinceLastMessage >= heartbeatInterval;
    }

    // Getters
    public String getSenderCompId() {
        return senderCompId;
    }

    public String getTargetCompId() {
        return targetCompId;
    }

    public int getOutgoingSeqNum() {
        return outgoingSeqNum.get();
    }

    public int getIncomingSeqNum() {
        return incomingSeqNum.get();
    }

    public boolean isLoggedOn() {
        return loggedOn;
    }

    public LocalDateTime getLastMessageTime() {
        return lastMessageTime;
    }

    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }

    /**
     * 重置会话序列号
     */
    public void reset() {
        outgoingSeqNum.set(1);
        incomingSeqNum.set(1);
        loggedOn = false;
    }
}