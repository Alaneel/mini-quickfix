package com.example.fix.message;

import com.example.fix.exception.FixException;
import com.example.fix.types.FixFieldType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for FixParser class
 */
@DisplayName("FixParser Tests")
class FixParserTest {

    @Test
    @DisplayName("Should parse valid FIX message with SOH delimiter")
    void testParseValidMessage() throws FixException {
        String fixString = "8=FIX.4.4" + FixMessage.SOH +
                "35=D" + FixMessage.SOH +
                "49=CLIENT1" + FixMessage.SOH +
                "56=BROKER1" + FixMessage.SOH +
                "34=1" + FixMessage.SOH +
                "52=20250115-10:30:45" + FixMessage.SOH;

        FixMessage message = FixParser.parse(fixString);

        assertNotNull(message);
        assertEquals("FIX.4.4", message.getFieldValue(FixFieldType.BEGIN_STRING));
        assertEquals("D", message.getFieldValue(FixFieldType.MSG_TYPE));
        assertEquals("CLIENT1", message.getFieldValue(FixFieldType.SENDER_COMP_ID));
        assertEquals("BROKER1", message.getFieldValue(FixFieldType.TARGET_COMP_ID));
        assertEquals("1", message.getFieldValue(FixFieldType.MSG_SEQ_NUM));
        assertEquals("20250115-10:30:45", message.getFieldValue(FixFieldType.SENDING_TIME));
    }

    @Test
    @DisplayName("Should parse readable FIX message with pipe delimiter")
    void testParseReadableMessage() throws FixException {
        String readableFixString = "8=FIX.4.4|35=D|49=CLIENT1|56=BROKER1|34=1|52=20250115-10:30:45|";

        FixMessage message = FixParser.parseReadable(readableFixString);

        assertNotNull(message);
        assertEquals("FIX.4.4", message.getFieldValue(FixFieldType.BEGIN_STRING));
        assertEquals("D", message.getFieldValue(FixFieldType.MSG_TYPE));
        assertEquals("CLIENT1", message.getFieldValue(FixFieldType.SENDER_COMP_ID));
        assertEquals("BROKER1", message.getFieldValue(FixFieldType.TARGET_COMP_ID));
    }

    @Test
    @DisplayName("Should throw exception for null message")
    void testParseNullMessage() {
        FixException exception = assertThrows(FixException.class, () -> {
            FixParser.parse(null);
        });

        assertTrue(exception.getMessage().contains("null or empty"));
    }

    @Test
    @DisplayName("Should throw exception for empty message")
    void testParseEmptyMessage() {
        FixException exception = assertThrows(FixException.class, () -> {
            FixParser.parse("");
        });

        assertTrue(exception.getMessage().contains("null or empty"));
    }

    @Test
    @DisplayName("Should throw exception for null readable message")
    void testParseReadableNullMessage() {
        FixException exception = assertThrows(FixException.class, () -> {
            FixParser.parseReadable(null);
        });

        assertTrue(exception.getMessage().contains("null or empty"));
    }

    @Test
    @DisplayName("Should throw exception for empty readable message")
    void testParseReadableEmptyMessage() {
        FixException exception = assertThrows(FixException.class, () -> {
            FixParser.parseReadable("");
        });

        assertTrue(exception.getMessage().contains("null or empty"));
    }

    @Test
    @DisplayName("Should throw exception for invalid field format")
    void testParseInvalidFieldFormat() {
        String invalidFixString = "8=FIX.4.4" + FixMessage.SOH +
                "INVALID_FIELD" + FixMessage.SOH;

        FixException exception = assertThrows(FixException.class, () -> {
            FixParser.parse(invalidFixString);
        });

        assertTrue(exception.getMessage().contains("Invalid field format"));
    }

    @Test
    @DisplayName("Should throw exception for invalid tag number")
    void testParseInvalidTagNumber() {
        String invalidFixString = "8=FIX.4.4" + FixMessage.SOH +
                "ABC=VALUE" + FixMessage.SOH;

        FixException exception = assertThrows(FixException.class, () -> {
            FixParser.parse(invalidFixString);
        });

        assertTrue(exception.getMessage().contains("Invalid tag number"));
    }

    @Test
    @DisplayName("Should throw exception when BeginString is missing")
    void testParseMissingBeginString() {
        String fixString = "35=D" + FixMessage.SOH +
                "49=CLIENT1" + FixMessage.SOH +
                "56=BROKER1" + FixMessage.SOH +
                "34=1" + FixMessage.SOH +
                "52=20250115-10:30:45" + FixMessage.SOH;

        FixException exception = assertThrows(FixException.class, () -> {
            FixParser.parse(fixString);
        });

        assertTrue(exception.getMessage().contains("BeginString"));
    }

    @Test
    @DisplayName("Should throw exception when MsgType is missing")
    void testParseMissingMsgType() {
        String fixString = "8=FIX.4.4" + FixMessage.SOH +
                "49=CLIENT1" + FixMessage.SOH +
                "56=BROKER1" + FixMessage.SOH +
                "34=1" + FixMessage.SOH +
                "52=20250115-10:30:45" + FixMessage.SOH;

        FixException exception = assertThrows(FixException.class, () -> {
            FixParser.parse(fixString);
        });

        assertTrue(exception.getMessage().contains("MsgType"));
    }

    @Test
    @DisplayName("Should throw exception when SenderCompID is missing")
    void testParseMissingSenderCompID() {
        String fixString = "8=FIX.4.4" + FixMessage.SOH +
                "35=D" + FixMessage.SOH +
                "56=BROKER1" + FixMessage.SOH +
                "34=1" + FixMessage.SOH +
                "52=20250115-10:30:45" + FixMessage.SOH;

        FixException exception = assertThrows(FixException.class, () -> {
            FixParser.parse(fixString);
        });

        assertTrue(exception.getMessage().contains("SenderCompID"));
    }

    @Test
    @DisplayName("Should throw exception when TargetCompID is missing")
    void testParseMissingTargetCompID() {
        String fixString = "8=FIX.4.4" + FixMessage.SOH +
                "35=D" + FixMessage.SOH +
                "49=CLIENT1" + FixMessage.SOH +
                "34=1" + FixMessage.SOH +
                "52=20250115-10:30:45" + FixMessage.SOH;

        FixException exception = assertThrows(FixException.class, () -> {
            FixParser.parse(fixString);
        });

        assertTrue(exception.getMessage().contains("TargetCompID"));
    }

    @Test
    @DisplayName("Should throw exception when MsgSeqNum is missing")
    void testParseMissingMsgSeqNum() {
        String fixString = "8=FIX.4.4" + FixMessage.SOH +
                "35=D" + FixMessage.SOH +
                "49=CLIENT1" + FixMessage.SOH +
                "56=BROKER1" + FixMessage.SOH +
                "52=20250115-10:30:45" + FixMessage.SOH;

        FixException exception = assertThrows(FixException.class, () -> {
            FixParser.parse(fixString);
        });

        assertTrue(exception.getMessage().contains("MsgSeqNum"));
    }

    @Test
    @DisplayName("Should throw exception when SendingTime is missing")
    void testParseMissingSendingTime() {
        String fixString = "8=FIX.4.4" + FixMessage.SOH +
                "35=D" + FixMessage.SOH +
                "49=CLIENT1" + FixMessage.SOH +
                "56=BROKER1" + FixMessage.SOH +
                "34=1" + FixMessage.SOH;

        FixException exception = assertThrows(FixException.class, () -> {
            FixParser.parse(fixString);
        });

        assertTrue(exception.getMessage().contains("SendingTime"));
    }

    @Test
    @DisplayName("Should parse message with additional fields")
    void testParseMessageWithAdditionalFields() throws FixException {
        String fixString = "8=FIX.4.4" + FixMessage.SOH +
                "35=D" + FixMessage.SOH +
                "49=CLIENT1" + FixMessage.SOH +
                "56=BROKER1" + FixMessage.SOH +
                "34=1" + FixMessage.SOH +
                "52=20250115-10:30:45" + FixMessage.SOH +
                "11=ORDER123" + FixMessage.SOH +
                "55=AAPL" + FixMessage.SOH +
                "54=1" + FixMessage.SOH +
                "38=100" + FixMessage.SOH +
                "40=2" + FixMessage.SOH +
                "44=150.50" + FixMessage.SOH;

        FixMessage message = FixParser.parse(fixString);

        assertEquals("ORDER123", message.getFieldValue(FixFieldType.CL_ORD_ID));
        assertEquals("AAPL", message.getFieldValue(FixFieldType.SYMBOL));
        assertEquals("1", message.getFieldValue(FixFieldType.SIDE));
        assertEquals("100", message.getFieldValue(FixFieldType.ORDER_QTY));
        assertEquals("2", message.getFieldValue(FixFieldType.ORD_TYPE));
        assertEquals("150.50", message.getFieldValue(FixFieldType.PRICE));
    }

    @Test
    @DisplayName("Should validate checksum correctly for valid message")
    void testValidateChecksumValid() throws FixException {
        FixMessage message = FixBuilder.newOrderSingle()
                .sender("CLIENT1")
                .target("BROKER1")
                .msgSeqNum(1)
                .sendingTime()
                .build();

        String fixString = message.toFixString();
        assertTrue(FixParser.validateChecksum(fixString));
    }

    @Test
    @DisplayName("Should validate checksum returns false for invalid checksum")
    void testValidateChecksumInvalid() {
        String fixString = "8=FIX.4.4" + FixMessage.SOH +
                "9=50" + FixMessage.SOH +
                "35=D" + FixMessage.SOH +
                "49=CLIENT1" + FixMessage.SOH +
                "10=999" + FixMessage.SOH;

        assertFalse(FixParser.validateChecksum(fixString));
    }

    @Test
    @DisplayName("Should validate checksum returns false for null string")
    void testValidateChecksumNull() {
        assertFalse(FixParser.validateChecksum(null));
    }

    @Test
    @DisplayName("Should validate checksum returns false for empty string")
    void testValidateChecksumEmpty() {
        assertFalse(FixParser.validateChecksum(""));
    }

    @Test
    @DisplayName("Should validate checksum returns false for missing checksum field")
    void testValidateChecksumMissingField() {
        String fixString = "8=FIX.4.4" + FixMessage.SOH +
                "35=D" + FixMessage.SOH +
                "49=CLIENT1" + FixMessage.SOH;

        assertFalse(FixParser.validateChecksum(fixString));
    }

    @Test
    @DisplayName("Should validate checksum returns false for malformed checksum")
    void testValidateChecksumMalformed() {
        String fixString = "8=FIX.4.4" + FixMessage.SOH +
                "35=D" + FixMessage.SOH +
                "10=ABC" + FixMessage.SOH;

        assertFalse(FixParser.validateChecksum(fixString));
    }

    @Test
    @DisplayName("Should handle empty fields in message")
    void testParseWithEmptyFields() throws FixException {
        String fixString = "8=FIX.4.4" + FixMessage.SOH +
                FixMessage.SOH +
                "35=D" + FixMessage.SOH +
                "49=CLIENT1" + FixMessage.SOH +
                "56=BROKER1" + FixMessage.SOH +
                "34=1" + FixMessage.SOH +
                "52=20250115-10:30:45" + FixMessage.SOH;

        FixMessage message = FixParser.parse(fixString);

        assertNotNull(message);
        assertEquals("D", message.getMsgType());
    }

    @Test
    @DisplayName("Should parse message with field value containing equals sign")
    void testParseFieldValueWithEqualsSign() throws FixException {
        String fixString = "8=FIX.4.4" + FixMessage.SOH +
                "35=D" + FixMessage.SOH +
                "49=CLIENT1" + FixMessage.SOH +
                "56=BROKER1" + FixMessage.SOH +
                "34=1" + FixMessage.SOH +
                "52=20250115-10:30:45" + FixMessage.SOH +
                "58=Text with = sign" + FixMessage.SOH;

        FixMessage message = FixParser.parse(fixString);

        assertEquals("Text with = sign", message.getFieldValue(58));
    }

    @Test
    @DisplayName("Should parse different message types correctly")
    void testParseDifferentMessageTypes() throws FixException {
        String[] messageTypes = {"A", "0", "1", "5", "D", "8", "G", "F"};

        for (String msgType : messageTypes) {
            String fixString = "8=FIX.4.4" + FixMessage.SOH +
                    "35=" + msgType + FixMessage.SOH +
                    "49=CLIENT1" + FixMessage.SOH +
                    "56=BROKER1" + FixMessage.SOH +
                    "34=1" + FixMessage.SOH +
                    "52=20250115-10:30:45" + FixMessage.SOH;

            FixMessage message = FixParser.parse(fixString);
            assertEquals(msgType, message.getMsgType());
        }
    }
}
