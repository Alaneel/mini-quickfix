package com.example.fix.message;

import com.example.fix.exception.FixException;
import com.example.fix.types.FixFieldType;
import com.example.fix.types.FixMsgType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for FixBuilder class
 */
@DisplayName("FixBuilder Tests")
class FixBuilderTest {

    private static final DateTimeFormatter FIX_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss");

    @Test
    @DisplayName("Should create builder with message type string")
    void testCreateBuilderWithMsgTypeString() throws FixException {
        FixBuilder builder = new FixBuilder("D");
        FixMessage message = builder
                .sender("CLIENT1")
                .target("BROKER1")
                .msgSeqNum(1)
                .sendingTime()
                .build();

        assertEquals("D", message.getMsgType());
        assertEquals("FIX.4.4", message.getFieldValue(FixFieldType.BEGIN_STRING));
    }

    @Test
    @DisplayName("Should create builder with FixMsgType enum")
    void testCreateBuilderWithMsgTypeEnum() throws FixException {
        FixBuilder builder = new FixBuilder(FixMsgType.NEW_ORDER_SINGLE);
        FixMessage message = builder
                .sender("CLIENT1")
                .target("BROKER1")
                .msgSeqNum(1)
                .sendingTime()
                .build();

        assertEquals("D", message.getMsgType());
    }

    @Test
    @DisplayName("Should create logon message")
    void testCreateLogonMessage() throws FixException {
        FixMessage message = FixBuilder.logon()
                .sender("CLIENT1")
                .target("BROKER1")
                .msgSeqNum(1)
                .sendingTime()
                .field(FixFieldType.HEARTBT_INT, 30)
                .build();

        assertEquals("A", message.getMsgType());
        assertEquals("CLIENT1", message.getFieldValue(FixFieldType.SENDER_COMP_ID));
        assertEquals("BROKER1", message.getFieldValue(FixFieldType.TARGET_COMP_ID));
        assertEquals("1", message.getFieldValue(FixFieldType.MSG_SEQ_NUM));
        assertEquals("30", message.getFieldValue(FixFieldType.HEARTBT_INT));
        assertNotNull(message.getFieldValue(FixFieldType.SENDING_TIME));
    }

    @Test
    @DisplayName("Should create logout message")
    void testCreateLogoutMessage() throws FixException {
        FixMessage message = FixBuilder.logout()
                .sender("CLIENT1")
                .target("BROKER1")
                .msgSeqNum(10)
                .sendingTime()
                .build();

        assertEquals("5", message.getMsgType());
    }

    @Test
    @DisplayName("Should create heartbeat message")
    void testCreateHeartbeatMessage() throws FixException {
        FixMessage message = FixBuilder.heartbeat()
                .sender("CLIENT1")
                .target("BROKER1")
                .msgSeqNum(5)
                .sendingTime()
                .build();

        assertEquals("0", message.getMsgType());
    }

    @Test
    @DisplayName("Should create test request message")
    void testCreateTestRequestMessage() throws FixException {
        FixMessage message = FixBuilder.testRequest("TEST123")
                .sender("CLIENT1")
                .target("BROKER1")
                .msgSeqNum(5)
                .sendingTime()
                .build();

        assertEquals("1", message.getMsgType());
        assertEquals("TEST123", message.getFieldValue(FixFieldType.TEST_REQ_ID));
    }

    @Test
    @DisplayName("Should create new order single message")
    void testCreateNewOrderSingleMessage() throws FixException {
        FixMessage message = FixBuilder.newOrderSingle()
                .sender("CLIENT1")
                .target("BROKER1")
                .msgSeqNum(5)
                .sendingTime()
                .field(FixFieldType.CL_ORD_ID, "ORDER123")
                .field(FixFieldType.SYMBOL, "AAPL")
                .field(FixFieldType.SIDE, "1")
                .field(FixFieldType.ORDER_QTY, 100)
                .field(FixFieldType.ORD_TYPE, "2")
                .field(FixFieldType.PRICE, 150.50)
                .build();

        assertEquals("D", message.getMsgType());
        assertEquals("ORDER123", message.getFieldValue(FixFieldType.CL_ORD_ID));
        assertEquals("AAPL", message.getFieldValue(FixFieldType.SYMBOL));
        assertEquals("1", message.getFieldValue(FixFieldType.SIDE));
        assertEquals("100", message.getFieldValue(FixFieldType.ORDER_QTY));
        assertEquals("2", message.getFieldValue(FixFieldType.ORD_TYPE));
        assertEquals("150.5", message.getFieldValue(FixFieldType.PRICE));
    }

    @Test
    @DisplayName("Should create execution report message")
    void testCreateExecutionReportMessage() throws FixException {
        FixMessage message = FixBuilder.executionReport()
                .sender("BROKER1")
                .target("CLIENT1")
                .msgSeqNum(6)
                .sendingTime()
                .build();

        assertEquals("8", message.getMsgType());
    }

    @Test
    @DisplayName("Should set sender correctly")
    void testSetSender() throws FixException {
        FixMessage message = FixBuilder.newOrderSingle()
                .sender("SENDER_ID")
                .target("TARGET_ID")
                .msgSeqNum(1)
                .sendingTime()
                .build();

        assertEquals("SENDER_ID", message.getFieldValue(FixFieldType.SENDER_COMP_ID));
    }

    @Test
    @DisplayName("Should set target correctly")
    void testSetTarget() throws FixException {
        FixMessage message = FixBuilder.newOrderSingle()
                .sender("SENDER_ID")
                .target("TARGET_ID")
                .msgSeqNum(1)
                .sendingTime()
                .build();

        assertEquals("TARGET_ID", message.getFieldValue(FixFieldType.TARGET_COMP_ID));
    }

    @Test
    @DisplayName("Should set message sequence number correctly")
    void testSetMsgSeqNum() throws FixException {
        FixMessage message = FixBuilder.newOrderSingle()
                .sender("CLIENT1")
                .target("BROKER1")
                .msgSeqNum(123)
                .sendingTime()
                .build();

        assertEquals("123", message.getFieldValue(FixFieldType.MSG_SEQ_NUM));
    }

    @Test
    @DisplayName("Should set sending time to current time")
    void testSetSendingTimeCurrentTime() throws FixException {
        LocalDateTime before = LocalDateTime.now();

        FixMessage message = FixBuilder.newOrderSingle()
                .sender("CLIENT1")
                .target("BROKER1")
                .msgSeqNum(1)
                .sendingTime()
                .build();

        LocalDateTime after = LocalDateTime.now();

        String sendingTime = message.getFieldValue(FixFieldType.SENDING_TIME);
        assertNotNull(sendingTime);

        LocalDateTime parsedTime = LocalDateTime.parse(sendingTime, FIX_TIME_FORMAT);
        assertTrue(!parsedTime.isBefore(before.minusSeconds(1)));
        assertTrue(!parsedTime.isAfter(after.plusSeconds(1)));
    }

    @Test
    @DisplayName("Should set sending time to specified LocalDateTime")
    void testSetSendingTimeSpecificDateTime() throws FixException {
        LocalDateTime specificTime = LocalDateTime.of(2025, 1, 15, 10, 30, 45);

        FixMessage message = FixBuilder.newOrderSingle()
                .sender("CLIENT1")
                .target("BROKER1")
                .msgSeqNum(1)
                .sendingTime(specificTime)
                .build();

        assertEquals("20250115-10:30:45", message.getFieldValue(FixFieldType.SENDING_TIME));
    }

    @Test
    @DisplayName("Should set sending time from string")
    void testSetSendingTimeFromString() throws FixException {
        FixMessage message = FixBuilder.newOrderSingle()
                .sender("CLIENT1")
                .target("BROKER1")
                .msgSeqNum(1)
                .sendingTime("20250115-10:30:45")
                .build();

        assertEquals("20250115-10:30:45", message.getFieldValue(FixFieldType.SENDING_TIME));
    }

    @Test
    @DisplayName("Should add field by tag and string value")
    void testAddFieldByTagString() throws FixException {
        FixMessage message = FixBuilder.newOrderSingle()
                .sender("CLIENT1")
                .target("BROKER1")
                .msgSeqNum(1)
                .sendingTime()
                .field(11, "ORDER123")
                .build();

        assertEquals("ORDER123", message.getFieldValue(11));
    }

    @Test
    @DisplayName("Should add field by FixFieldType and string value")
    void testAddFieldByFieldTypeString() throws FixException {
        FixMessage message = FixBuilder.newOrderSingle()
                .sender("CLIENT1")
                .target("BROKER1")
                .msgSeqNum(1)
                .sendingTime()
                .field(FixFieldType.CL_ORD_ID, "ORDER123")
                .build();

        assertEquals("ORDER123", message.getFieldValue(FixFieldType.CL_ORD_ID));
    }

    @Test
    @DisplayName("Should add field by tag and integer value")
    void testAddFieldByTagInt() throws FixException {
        FixMessage message = FixBuilder.newOrderSingle()
                .sender("CLIENT1")
                .target("BROKER1")
                .msgSeqNum(1)
                .sendingTime()
                .field(38, 1000)
                .build();

        assertEquals("1000", message.getFieldValue(38));
    }

    @Test
    @DisplayName("Should add field by FixFieldType and integer value")
    void testAddFieldByFieldTypeInt() throws FixException {
        FixMessage message = FixBuilder.newOrderSingle()
                .sender("CLIENT1")
                .target("BROKER1")
                .msgSeqNum(1)
                .sendingTime()
                .field(FixFieldType.ORDER_QTY, 1000)
                .build();

        assertEquals("1000", message.getFieldValue(FixFieldType.ORDER_QTY));
    }

    @Test
    @DisplayName("Should add field by tag and double value")
    void testAddFieldByTagDouble() throws FixException {
        FixMessage message = FixBuilder.newOrderSingle()
                .sender("CLIENT1")
                .target("BROKER1")
                .msgSeqNum(1)
                .sendingTime()
                .field(44, 150.50)
                .build();

        assertEquals("150.5", message.getFieldValue(44));
    }

    @Test
    @DisplayName("Should add field by FixFieldType and double value")
    void testAddFieldByFieldTypeDouble() throws FixException {
        FixMessage message = FixBuilder.newOrderSingle()
                .sender("CLIENT1")
                .target("BROKER1")
                .msgSeqNum(1)
                .sendingTime()
                .field(FixFieldType.PRICE, 150.50)
                .build();

        assertEquals("150.5", message.getFieldValue(FixFieldType.PRICE));
    }

    @Test
    @DisplayName("Should throw exception when sender is missing")
    void testBuildWithoutSender() {
        FixBuilder builder = FixBuilder.newOrderSingle()
                .target("BROKER1")
                .msgSeqNum(1);

        FixException exception = assertThrows(FixException.class, builder::build);
        assertTrue(exception.getMessage().contains("SenderCompID"));
    }

    @Test
    @DisplayName("Should throw exception when target is missing")
    void testBuildWithoutTarget() {
        FixBuilder builder = FixBuilder.newOrderSingle()
                .sender("CLIENT1")
                .msgSeqNum(1);

        FixException exception = assertThrows(FixException.class, builder::build);
        assertTrue(exception.getMessage().contains("TargetCompID"));
    }

    @Test
    @DisplayName("Should throw exception when message sequence number is missing")
    void testBuildWithoutMsgSeqNum() {
        FixBuilder builder = FixBuilder.newOrderSingle()
                .sender("CLIENT1")
                .target("BROKER1");

        FixException exception = assertThrows(FixException.class, builder::build);
        assertTrue(exception.getMessage().contains("MsgSeqNum"));
    }

    @Test
    @DisplayName("Should auto-set sending time if not provided")
    void testBuildAutoSetsSendingTime() throws FixException {
        FixMessage message = FixBuilder.newOrderSingle()
                .sender("CLIENT1")
                .target("BROKER1")
                .msgSeqNum(1)
                .build();

        assertNotNull(message.getFieldValue(FixFieldType.SENDING_TIME));
    }

    @Test
    @DisplayName("Should support method chaining")
    void testMethodChaining() throws FixException {
        FixMessage message = FixBuilder.newOrderSingle()
                .sender("CLIENT1")
                .target("BROKER1")
                .msgSeqNum(1)
                .sendingTime()
                .field(FixFieldType.CL_ORD_ID, "ORDER123")
                .field(FixFieldType.SYMBOL, "AAPL")
                .field(FixFieldType.SIDE, "1")
                .build();

        assertNotNull(message);
        assertEquals("D", message.getMsgType());
        assertEquals("CLIENT1", message.getFieldValue(FixFieldType.SENDER_COMP_ID));
    }
}
