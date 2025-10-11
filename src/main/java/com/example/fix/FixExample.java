package com.example.fix;

import java.time.LocalDateTime;

/**
 * FIX协议库使用示例
 */
public class FixExample {

    public static void main(String[] args) {
        try {
            // 示例1: 使用FixBuilder创建Logon消息
            example1_CreateLogonMessage();

            // 示例2: 创建NewOrderSingle消息
            example2_CreateNewOrderSingle();

            // 示例3: 解析FIX消息
            example3_ParseFixMessage();

            // 示例4: 使用FixSession管理会话
            example4_UseFixSession();

            // 示例5: 验证校验和
            example5_ValidateChecksum();

        } catch (FixException e) {
            System.err.println("FIX Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 示例1: 创建Logon消息
     */
    public static void example1_CreateLogonMessage() throws FixException {
        System.out.println("=== 示例1: 创建Logon消息 ===");

        FixMessage logon = FixBuilder.logon()
                .sender("CLIENT1")
                .target("BROKER1")
                .msgSeqNum(1)
                .sendingTime()
                .field(FixFieldType.HEARTBT_INT, 30)
                .build();

        System.out.println("Logon消息(可读格式): " + logon);
        System.out.println("Logon消息(FIX格式): " + logon.toFixString().replace(FixMessage.SOH, '|'));
        System.out.println();
    }

    /**
     * 示例2: 创建NewOrderSingle消息
     */
    public static void example2_CreateNewOrderSingle() throws FixException {
        System.out.println("=== 示例2: 创建NewOrderSingle消息 ===");

        FixMessage order = FixBuilder.newOrderSingle()
                .sender("CLIENT1")
                .target("BROKER1")
                .msgSeqNum(5)
                .sendingTime()
                .field(FixFieldType.CL_ORD_ID, "ORDER123")
                .field(FixFieldType.SYMBOL, "AAPL")
                .field(FixFieldType.SIDE, "1")  // 1=Buy
                .field(FixFieldType.ORDER_QTY, 100)
                .field(FixFieldType.ORD_TYPE, "2")  // 2=Limit
                .field(FixFieldType.PRICE, 150.50)
                .field(FixFieldType.TIME_IN_FORCE, "0")  // 0=Day
                .build();

        System.out.println("订单消息: " + order);
        System.out.println();
    }

    /**
     * 示例3: 解析FIX消息
     */
    public static void example3_ParseFixMessage() throws FixException {
        System.out.println("=== 示例3: 解析FIX消息 ===");

        // 使用|作为分隔符的可读格式
        String fixString = "8=FIX.4.4|9=100|35=D|49=CLIENT1|56=BROKER1|34=5|52=20250101-10:30:00|11=ORDER123|55=AAPL|54=1|38=100|40=2|44=150.50|59=0|";

        FixMessage message = FixParser.parseReadable(fixString);

        System.out.println("解析的消息类型: " + message.getMsgType());
        System.out.println("发送方: " + message.getFieldValue(FixFieldType.SENDER_COMP_ID));
        System.out.println("接收方: " + message.getFieldValue(FixFieldType.TARGET_COMP_ID));
        System.out.println("订单ID: " + message.getFieldValue(FixFieldType.CL_ORD_ID));
        System.out.println("股票代码: " + message.getFieldValue(FixFieldType.SYMBOL));
        System.out.println("数量: " + message.getFieldValue(FixFieldType.ORDER_QTY));
        System.out.println("价格: " + message.getFieldValue(FixFieldType.PRICE));
        System.out.println();
    }

    /**
     * 示例4: 使用FixSession管理会话
     */
    public static void example4_UseFixSession() throws FixException {
        System.out.println("=== 示例4: 使用FixSession管理会话 ===");

        // 创建会话(心跳间隔30秒)
        FixSession session = new FixSession("CLIENT1", "BROKER1", 30);

        // 创建并发送Logon消息
        FixMessage logon = session.createLogon();
        System.out.println("Logon: " + logon);
        System.out.println("会话状态: " + (session.isLoggedOn() ? "已登录" : "未登录"));
        System.out.println("出站序列号: " + session.getOutgoingSeqNum());

        // 创建心跳消息
        FixMessage heartbeat = session.createHeartbeat();
        System.out.println("Heartbeat: " + heartbeat);
        System.out.println("出站序列号: " + session.getOutgoingSeqNum());

        // 创建Logout消息
        FixMessage logout = session.createLogout("Session terminated");
        System.out.println("Logout: " + logout);
        System.out.println("会话状态: " + (session.isLoggedOn() ? "已登录" : "未登录"));
        System.out.println();
    }

    /**
     * 示例5: 验证校验和
     */
    public static void example5_ValidateChecksum() throws FixException {
        System.out.println("=== 示例5: 验证校验和 ===");

        FixMessage message = FixBuilder.heartbeat()
                .sender("CLIENT1")
                .target("BROKER1")
                .msgSeqNum(10)
                .sendingTime()
                .build();

        String fixString = message.toFixString();
        boolean isValid = FixParser.validateChecksum(fixString);

        System.out.println("消息: " + fixString.replace(FixMessage.SOH, '|'));
        System.out.println("校验和验证: " + (isValid ? "通过" : "失败"));
        System.out.println();
    }

    /**
     * 示例6: 创建ExecutionReport消息
     */
    public static void example6_CreateExecutionReport() throws FixException {
        System.out.println("=== 示例6: 创建ExecutionReport消息 ===");

        FixMessage execReport = FixBuilder.executionReport()
                .sender("BROKER1")
                .target("CLIENT1")
                .msgSeqNum(20)
                .sendingTime()
                .field(FixFieldType.ORDER_ID, "ORD001")
                .field(FixFieldType.EXEC_ID, "EXEC001")
                .field(FixFieldType.EXEC_TYPE, "0")  // 0=New
                .field(FixFieldType.ORD_STATUS, "0")  // 0=New
                .field(FixFieldType.SYMBOL, "AAPL")
                .field(FixFieldType.SIDE, "1")  // 1=Buy
                .field(FixFieldType.ORDER_QTY, 100)
                .field(FixFieldType.PRICE, 150.50)
                .field(11, "ORDER123")  // ClOrdID
                .build();

        System.out.println("执行报告: " + execReport);
        System.out.println();
    }
}