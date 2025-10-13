package com.example.fix.session;

import com.example.fix.exception.FixException;
import com.example.fix.message.FixMessage;
import com.example.fix.types.FixFieldType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for FixSession class
 */
@DisplayName("FixSession Tests")
class FixSessionTest {

    private FixSession session;
    private static final String SENDER_ID = "CLIENT1";
    private static final String TARGET_ID = "BROKER1";
    private static final int HEARTBEAT_INTERVAL = 30;

    @BeforeEach
    void setUp() {
        session = new FixSession(SENDER_ID, TARGET_ID, HEARTBEAT_INTERVAL);
    }

    @Test
    @DisplayName("Should create session with correct initial state")
    void testCreateSession() {
        assertNotNull(session);
        assertEquals(SENDER_ID, session.getSenderCompId());
        assertEquals(TARGET_ID, session.getTargetCompId());
        assertEquals(HEARTBEAT_INTERVAL, session.getHeartbeatInterval());
        assertEquals(1, session.getOutgoingSeqNum());
        assertEquals(1, session.getIncomingSeqNum());
        assertFalse(session.isLoggedOn());
        assertNotNull(session.getLastMessageTime());
    }

    @Test
    @DisplayName("Should create logon message and increment sequence number")
    void testCreateLogon() throws FixException {
        FixMessage logon = session.createLogon();

        assertNotNull(logon);
        assertEquals("A", logon.getMsgType());
        assertEquals(SENDER_ID, logon.getFieldValue(FixFieldType.SENDER_COMP_ID));
        assertEquals(TARGET_ID, logon.getFieldValue(FixFieldType.TARGET_COMP_ID));
        assertEquals("1", logon.getFieldValue(FixFieldType.MSG_SEQ_NUM));
        assertEquals(String.valueOf(HEARTBEAT_INTERVAL), logon.getFieldValue(FixFieldType.HEARTBT_INT));
        assertTrue(session.isLoggedOn());
        assertEquals(2, session.getOutgoingSeqNum());
    }

    @Test
    @DisplayName("Should create logout message without text")
    void testCreateLogoutWithoutText() throws FixException {
        session.createLogon();
        FixMessage logout = session.createLogout();

        assertNotNull(logout);
        assertEquals("5", logout.getMsgType());
        assertEquals(SENDER_ID, logout.getFieldValue(FixFieldType.SENDER_COMP_ID));
        assertEquals(TARGET_ID, logout.getFieldValue(FixFieldType.TARGET_COMP_ID));
        assertEquals("2", logout.getFieldValue(FixFieldType.MSG_SEQ_NUM));
        assertFalse(session.isLoggedOn());
        assertNull(logout.getFieldValue(FixFieldType.TEXT));
    }

    @Test
    @DisplayName("Should create logout message with text")
    void testCreateLogoutWithText() throws FixException {
        session.createLogon();
        String logoutText = "Session terminated";
        FixMessage logout = session.createLogout(logoutText);

        assertNotNull(logout);
        assertEquals("5", logout.getMsgType());
        assertEquals(logoutText, logout.getFieldValue(FixFieldType.TEXT));
        assertFalse(session.isLoggedOn());
    }

    @Test
    @DisplayName("Should create heartbeat message without test request ID")
    void testCreateHeartbeatWithoutTestReqId() throws FixException {
        FixMessage heartbeat = session.createHeartbeat();

        assertNotNull(heartbeat);
        assertEquals("0", heartbeat.getMsgType());
        assertEquals(SENDER_ID, heartbeat.getFieldValue(FixFieldType.SENDER_COMP_ID));
        assertEquals(TARGET_ID, heartbeat.getFieldValue(FixFieldType.TARGET_COMP_ID));
        assertNull(heartbeat.getFieldValue(FixFieldType.TEST_REQ_ID));
    }

    @Test
    @DisplayName("Should create heartbeat message with test request ID")
    void testCreateHeartbeatWithTestReqId() throws FixException {
        String testReqId = "TEST123";
        FixMessage heartbeat = session.createHeartbeat(testReqId);

        assertNotNull(heartbeat);
        assertEquals("0", heartbeat.getMsgType());
        assertEquals(testReqId, heartbeat.getFieldValue(FixFieldType.TEST_REQ_ID));
    }

    @Test
    @DisplayName("Should create test request message")
    void testCreateTestRequest() throws FixException {
        String testReqId = "TEST456";
        FixMessage testRequest = session.createTestRequest(testReqId);

        assertNotNull(testRequest);
        assertEquals("1", testRequest.getMsgType());
        assertEquals(testReqId, testRequest.getFieldValue(FixFieldType.TEST_REQ_ID));
        assertEquals(SENDER_ID, testRequest.getFieldValue(FixFieldType.SENDER_COMP_ID));
        assertEquals(TARGET_ID, testRequest.getFieldValue(FixFieldType.TARGET_COMP_ID));
    }

    @Test
    @DisplayName("Should create custom message type")
    void testCreateCustomMessage() throws FixException {
        String customMsgType = "D";
        FixMessage message = session.createMessage(customMsgType);

        assertNotNull(message);
        assertEquals(customMsgType, message.getMsgType());
        assertEquals(SENDER_ID, message.getFieldValue(FixFieldType.SENDER_COMP_ID));
        assertEquals(TARGET_ID, message.getFieldValue(FixFieldType.TARGET_COMP_ID));
    }

    @Test
    @DisplayName("Should increment sequence number for each outgoing message")
    void testSequenceNumberIncrement() throws FixException {
        assertEquals(1, session.getOutgoingSeqNum());

        session.createLogon();
        assertEquals(2, session.getOutgoingSeqNum());

        session.createHeartbeat();
        assertEquals(3, session.getOutgoingSeqNum());

        session.createMessage("D");
        assertEquals(4, session.getOutgoingSeqNum());

        session.createLogout();
        assertEquals(5, session.getOutgoingSeqNum());
    }

    @Test
    @DisplayName("Should update last message time when creating messages")
    void testLastMessageTimeUpdate() throws FixException, InterruptedException {
        LocalDateTime before = LocalDateTime.now();
        Thread.sleep(10);

        session.createLogon();
        LocalDateTime after = session.getLastMessageTime();

        assertTrue(!after.isBefore(before));
    }

    @Test
    @DisplayName("Should process valid incoming message")
    void testProcessValidIncomingMessage() throws FixException {
        FixMessage incoming = new FixMessage();
        incoming.setField(FixFieldType.SENDER_COMP_ID, TARGET_ID);
        incoming.setField(FixFieldType.TARGET_COMP_ID, SENDER_ID);
        incoming.setField(FixFieldType.MSG_SEQ_NUM, "1");

        assertDoesNotThrow(() -> session.processIncomingMessage(incoming));
        assertEquals(2, session.getIncomingSeqNum());
    }

    @Test
    @DisplayName("Should throw exception for invalid sender in incoming message")
    void testProcessInvalidSender() {
        FixMessage incoming = new FixMessage();
        incoming.setField(FixFieldType.SENDER_COMP_ID, "WRONG_SENDER");
        incoming.setField(FixFieldType.TARGET_COMP_ID, SENDER_ID);
        incoming.setField(FixFieldType.MSG_SEQ_NUM, "1");

        FixException exception = assertThrows(FixException.class,
                () -> session.processIncomingMessage(incoming));
        assertTrue(exception.getMessage().contains("SenderCompID"));
    }

    @Test
    @DisplayName("Should throw exception for invalid target in incoming message")
    void testProcessInvalidTarget() {
        FixMessage incoming = new FixMessage();
        incoming.setField(FixFieldType.SENDER_COMP_ID, TARGET_ID);
        incoming.setField(FixFieldType.TARGET_COMP_ID, "WRONG_TARGET");
        incoming.setField(FixFieldType.MSG_SEQ_NUM, "1");

        FixException exception = assertThrows(FixException.class,
                () -> session.processIncomingMessage(incoming));
        assertTrue(exception.getMessage().contains("TargetCompID"));
    }

    @Test
    @DisplayName("Should throw exception for sequence number too low")
    void testProcessSequenceNumberTooLow() throws FixException {
        // Process first message
        FixMessage first = new FixMessage();
        first.setField(FixFieldType.SENDER_COMP_ID, TARGET_ID);
        first.setField(FixFieldType.TARGET_COMP_ID, SENDER_ID);
        first.setField(FixFieldType.MSG_SEQ_NUM, "1");
        session.processIncomingMessage(first);

        // Try to process message with lower sequence number
        FixMessage second = new FixMessage();
        second.setField(FixFieldType.SENDER_COMP_ID, TARGET_ID);
        second.setField(FixFieldType.TARGET_COMP_ID, SENDER_ID);
        second.setField(FixFieldType.MSG_SEQ_NUM, "1");

        FixException exception = assertThrows(FixException.class,
                () -> session.processIncomingMessage(second));
        assertTrue(exception.getMessage().contains("too low"));
    }

    @Test
    @DisplayName("Should throw exception for sequence number too high")
    void testProcessSequenceNumberTooHigh() {
        FixMessage incoming = new FixMessage();
        incoming.setField(FixFieldType.SENDER_COMP_ID, TARGET_ID);
        incoming.setField(FixFieldType.TARGET_COMP_ID, SENDER_ID);
        incoming.setField(FixFieldType.MSG_SEQ_NUM, "10");

        FixException exception = assertThrows(FixException.class,
                () -> session.processIncomingMessage(incoming));
        assertTrue(exception.getMessage().contains("too high"));
    }

    @Test
    @DisplayName("Should process multiple incoming messages in sequence")
    void testProcessMultipleIncomingMessages() throws FixException {
        for (int i = 1; i <= 5; i++) {
            FixMessage incoming = new FixMessage();
            incoming.setField(FixFieldType.SENDER_COMP_ID, TARGET_ID);
            incoming.setField(FixFieldType.TARGET_COMP_ID, SENDER_ID);
            incoming.setField(FixFieldType.MSG_SEQ_NUM, String.valueOf(i));

            session.processIncomingMessage(incoming);
            assertEquals(i + 1, session.getIncomingSeqNum());
        }
    }

    @Test
    @DisplayName("Should not need heartbeat when not logged on")
    void testNeedsHeartbeatWhenNotLoggedOn() {
        assertFalse(session.needsHeartbeat());
    }

    @Test
    @DisplayName("Should not need heartbeat immediately after logon")
    void testNeedsHeartbeatAfterLogon() throws FixException {
        session.createLogon();
        assertFalse(session.needsHeartbeat());
    }

    @Test
    @DisplayName("Should need heartbeat after interval has passed")
    void testNeedsHeartbeatAfterInterval() throws FixException, InterruptedException {
        FixSession shortSession = new FixSession(SENDER_ID, TARGET_ID, 1);
        shortSession.createLogon();

        // Wait for more than the heartbeat interval
        Thread.sleep(1100);

        assertTrue(shortSession.needsHeartbeat());
    }

    @Test
    @DisplayName("Should reset session to initial state")
    void testResetSession() throws FixException {
        // Create some messages
        session.createLogon();
        session.createHeartbeat();
        session.createMessage("D");

        assertEquals(4, session.getOutgoingSeqNum());
        assertTrue(session.isLoggedOn());

        // Reset session
        session.reset();

        assertEquals(1, session.getOutgoingSeqNum());
        assertEquals(1, session.getIncomingSeqNum());
        assertFalse(session.isLoggedOn());
    }

    @Test
    @DisplayName("Should maintain separate incoming and outgoing sequence numbers")
    void testSeparateSequenceNumbers() throws FixException {
        // Send outgoing messages
        session.createLogon();
        session.createHeartbeat();
        assertEquals(3, session.getOutgoingSeqNum());
        assertEquals(1, session.getIncomingSeqNum());

        // Process incoming message
        FixMessage incoming = new FixMessage();
        incoming.setField(FixFieldType.SENDER_COMP_ID, TARGET_ID);
        incoming.setField(FixFieldType.TARGET_COMP_ID, SENDER_ID);
        incoming.setField(FixFieldType.MSG_SEQ_NUM, "1");
        session.processIncomingMessage(incoming);

        assertEquals(3, session.getOutgoingSeqNum());
        assertEquals(2, session.getIncomingSeqNum());
    }

    @Test
    @DisplayName("Should handle thread-safe sequence number operations")
    void testThreadSafeSequenceNumbers() throws Exception {
        int numThreads = 10;
        Thread[] threads = new Thread[numThreads];

        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(() -> {
                try {
                    session.createHeartbeat();
                } catch (FixException e) {
                    fail("Unexpected exception: " + e.getMessage());
                }
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        assertEquals(numThreads + 1, session.getOutgoingSeqNum());
    }
}
