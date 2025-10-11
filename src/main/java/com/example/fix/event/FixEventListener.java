package com.example.fix.event;

import com.example.fix.message.FixMessage;
import com.example.fix.session.FixSession;
import com.example.fix.types.FixFieldType;
import com.example.fix.types.FixMsgType;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * FIX事件监听器接口
 */
public interface FixEventListener {

    /**
     * 会话创建事件
     */
    default void onSessionCreated(FixSession session) {}

    /**
     * 会话登录事件
     */
    default void onLogon(FixSession session, FixMessage logonMessage) {}

    /**
     * 会话登出事件
     */
    default void onLogout(FixSession session, FixMessage logoutMessage) {}

    /**
     * 消息发送前事件
     */
    default void onBeforeSend(FixSession session, FixMessage message) {}

    /**
     * 消息发送后事件
     */
    default void onAfterSend(FixSession session, FixMessage message) {}

    /**
     * 消息接收事件
     */
    default void onMessageReceived(FixSession session, FixMessage message) {}

    /**
     * 心跳超时事件
     */
    default void onHeartbeatTimeout(FixSession session) {}

    /**
     * 序列号不匹配事件
     */
    default void onSequenceNumberMismatch(FixSession session,
                                          int expected, int actual) {}

    /**
     * 错误事件
     */
    default void onError(FixSession session, Exception error) {}

    /**
     * 连接建立事件
     */
    default void onConnected(String host, int port) {}

    /**
     * 连接断开事件
     */
    default void onDisconnected(String host, int port) {}
}

/**
 * 事件管理器
 */
class FixEventManager {
    private final List<FixEventListener> listeners;

    public FixEventManager() {
        this.listeners = new CopyOnWriteArrayList<>();
    }

    /**
     * 添加监听器
     */
    public void addListener(FixEventListener listener) {
        listeners.add(listener);
    }

    /**
     * 移除监听器
     */
    public void removeListener(FixEventListener listener) {
        listeners.remove(listener);
    }

    /**
     * 触发会话创建事件
     */
    public void fireSessionCreated(FixSession session) {
        for (FixEventListener listener : listeners) {
            try {
                listener.onSessionCreated(session);
            } catch (Exception e) {
                handleListenerException(e);
            }
        }
    }

    /**
     * 触发登录事件
     */
    public void fireLogon(FixSession session, FixMessage message) {
        for (FixEventListener listener : listeners) {
            try {
                listener.onLogon(session, message);
            } catch (Exception e) {
                handleListenerException(e);
            }
        }
    }

    /**
     * 触发登出事件
     */
    public void fireLogout(FixSession session, FixMessage message) {
        for (FixEventListener listener : listeners) {
            try {
                listener.onLogout(session, message);
            } catch (Exception e) {
                handleListenerException(e);
            }
        }
    }

    /**
     * 触发消息发送前事件
     */
    public void fireBeforeSend(FixSession session, FixMessage message) {
        for (FixEventListener listener : listeners) {
            try {
                listener.onBeforeSend(session, message);
            } catch (Exception e) {
                handleListenerException(e);
            }
        }
    }

    /**
     * 触发消息发送后事件
     */
    public void fireAfterSend(FixSession session, FixMessage message) {
        for (FixEventListener listener : listeners) {
            try {
                listener.onAfterSend(session, message);
            } catch (Exception e) {
                handleListenerException(e);
            }
        }
    }

    /**
     * 触发消息接收事件
     */
    public void fireMessageReceived(FixSession session, FixMessage message) {
        for (FixEventListener listener : listeners) {
            try {
                listener.onMessageReceived(session, message);
            } catch (Exception e) {
                handleListenerException(e);
            }
        }
    }

    /**
     * 触发错误事件
     */
    public void fireError(FixSession session, Exception error) {
        for (FixEventListener listener : listeners) {
            try {
                listener.onError(session, error);
            } catch (Exception e) {
                handleListenerException(e);
            }
        }
    }

    private void handleListenerException(Exception e) {
        System.err.println("Exception in event listener: " + e.getMessage());
        e.printStackTrace();
    }
}

/**
 * 日志监听器 - 记录所有事件
 */
class LoggingListener implements FixEventListener {

    @Override
    public void onSessionCreated(FixSession session) {
        log("Session created: " + session.getSenderCompId() +
                " -> " + session.getTargetCompId());
    }

    @Override
    public void onLogon(FixSession session, FixMessage logonMessage) {
        log("Logon: " + session.getSenderCompId() + " logged on");
    }

    @Override
    public void onLogout(FixSession session, FixMessage logoutMessage) {
        log("Logout: " + session.getSenderCompId() + " logged out");
    }

    @Override
    public void onBeforeSend(FixSession session, FixMessage message) {
        log("Sending: " + message.getMsgType() + " SEQ=" +
                message.getFieldValue(FixFieldType.MSG_SEQ_NUM));
    }

    @Override
    public void onMessageReceived(FixSession session, FixMessage message) {
        log("Received: " + message.getMsgType() + " SEQ=" +
                message.getFieldValue(FixFieldType.MSG_SEQ_NUM));
    }

    @Override
    public void onHeartbeatTimeout(FixSession session) {
        log("WARNING: Heartbeat timeout for " + session.getSenderCompId());
    }

    @Override
    public void onSequenceNumberMismatch(FixSession session, int expected, int actual) {
        log("ERROR: Sequence number mismatch - expected: " +
                expected + ", actual: " + actual);
    }

    @Override
    public void onError(FixSession session, Exception error) {
        log("ERROR: " + error.getMessage());
    }

    @Override
    public void onConnected(String host, int port) {
        log("Connected to " + host + ":" + port);
    }

    @Override
    public void onDisconnected(String host, int port) {
        log("Disconnected from " + host + ":" + port);
    }

    private void log(String message) {
        System.out.println("[" + LocalDateTime.now() + "] " + message);
    }
}

/**
 * 统计监听器 - 收集性能指标
 */
class StatisticsListener implements FixEventListener {
    private long messagesReceived = 0;
    private long messagesSent = 0;
    private long errors = 0;
    private final Map<String, Long> messageTypeCount;
    private LocalDateTime startTime;

    public StatisticsListener() {
        this.messageTypeCount = new HashMap<>();
        this.startTime = LocalDateTime.now();
    }

    @Override
    public void onSessionCreated(FixSession session) {
        startTime = LocalDateTime.now();
    }

    @Override
    public void onAfterSend(FixSession session, FixMessage message) {
        messagesSent++;
        countMessageType(message.getMsgType());
    }

    @Override
    public void onMessageReceived(FixSession session, FixMessage message) {
        messagesReceived++;
        countMessageType(message.getMsgType());
    }

    @Override
    public void onError(FixSession session, Exception error) {
        errors++;
    }

    private void countMessageType(String msgType) {
        messageTypeCount.merge(msgType, 1L, Long::sum);
    }

    /**
     * 打印统计信息
     */
    public void printStatistics() {
        System.out.println("\n=== FIX Session Statistics ===");
        System.out.println("Start time: " + startTime);
        System.out.println("Messages sent: " + messagesSent);
        System.out.println("Messages received: " + messagesReceived);
        System.out.println("Total messages: " + (messagesSent + messagesReceived));
        System.out.println("Errors: " + errors);
        System.out.println("\nMessage type breakdown:");

        messageTypeCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(entry -> {
                    FixMsgType msgType = FixMsgType.fromValue(entry.getKey());
                    String name = msgType != null ? msgType.getName() : entry.getKey();
                    System.out.println("  " + name + ": " + entry.getValue());
                });
        System.out.println("==============================\n");
    }

    public void reset() {
        messagesReceived = 0;
        messagesSent = 0;
        errors = 0;
        messageTypeCount.clear();
        startTime = LocalDateTime.now();
    }
}

/**
 * 业务监听器示例 - 处理特定业务逻辑
 */
class BusinessLogicListener implements FixEventListener {

    @Override
    public void onMessageReceived(FixSession session, FixMessage message) {
        String msgType = message.getMsgType();

        switch (msgType) {
            case "D": // NewOrderSingle
                handleNewOrder(message);
                break;
            case "8": // ExecutionReport
                handleExecutionReport(message);
                break;
            case "F": // OrderCancelRequest
                handleCancelRequest(message);
                break;
        }
    }

    private void handleNewOrder(FixMessage message) {
        String clOrdId = message.getFieldValue(FixFieldType.CL_ORD_ID);
        String symbol = message.getFieldValue(FixFieldType.SYMBOL);
        String side = message.getFieldValue(FixFieldType.SIDE);
        String qty = message.getFieldValue(FixFieldType.ORDER_QTY);

        System.out.println("New Order: " + clOrdId +
                " - " + (side.equals("1") ? "BUY" : "SELL") +
                " " + qty + " " + symbol);

        // 在这里实现订单处理逻辑
    }

    private void handleExecutionReport(FixMessage message) {
        String orderId = message.getFieldValue(FixFieldType.ORDER_ID);
        String execType = message.getFieldValue(FixFieldType.EXEC_TYPE);
        String ordStatus = message.getFieldValue(FixFieldType.ORD_STATUS);

        System.out.println("Execution Report: Order " + orderId +
                " - ExecType=" + execType +
                ", Status=" + ordStatus);

        // 在这里实现执行报告处理逻辑
    }

    private void handleCancelRequest(FixMessage message) {
        String origClOrdId = message.getFieldValue(11); // ClOrdID
        System.out.println("Cancel Request for order: " + origClOrdId);

        // 在这里实现撤单逻辑
    }
}