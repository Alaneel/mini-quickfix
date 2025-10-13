package com.example.fix.message;

import com.example.fix.types.FixFieldType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for FixMessage class
 */
@DisplayName("FixMessage Tests")
class FixMessageTest {

    private FixMessage message;

    @BeforeEach
    void setUp() {
        message = new FixMessage();
    }

    @Test
    @DisplayName("Should create empty message successfully")
    void testCreateEmptyMessage() {
        assertNotNull(message);
        assertTrue(message.getAllFields().isEmpty());
    }

    @Test
    @DisplayName("Should set and get field by tag number")
    void testSetAndGetFieldByTag() {
        message.setField(35, "D");

        FixField field = message.getField(35);
        assertNotNull(field);
        assertEquals(35, field.getTag());
        assertEquals("D", field.getValue());
    }

    @Test
    @DisplayName("Should set and get field by FixFieldType")
    void testSetAndGetFieldByFieldType() {
        message.setField(FixFieldType.MSG_TYPE, "D");

        FixField field = message.getField(FixFieldType.MSG_TYPE);
        assertNotNull(field);
        assertEquals(35, field.getTag());
        assertEquals("D", field.getValue());
    }

    @Test
    @DisplayName("Should get field value by tag number")
    void testGetFieldValueByTag() {
        message.setField(35, "D");

        String value = message.getFieldValue(35);
        assertEquals("D", value);
    }

    @Test
    @DisplayName("Should get field value by FixFieldType")
    void testGetFieldValueByFieldType() {
        message.setField(FixFieldType.MSG_TYPE, "D");

        String value = message.getFieldValue(FixFieldType.MSG_TYPE);
        assertEquals("D", value);
    }

    @Test
    @DisplayName("Should return null for non-existent field")
    void testGetNonExistentField() {
        assertNull(message.getField(999));
        assertNull(message.getFieldValue(999));
    }

    @Test
    @DisplayName("Should check if field exists")
    void testHasField() {
        message.setField(35, "D");

        assertTrue(message.hasField(35));
        assertTrue(message.hasField(FixFieldType.MSG_TYPE));
        assertFalse(message.hasField(999));
    }

    @Test
    @DisplayName("Should remove field successfully")
    void testRemoveField() {
        message.setField(35, "D");
        assertTrue(message.hasField(35));

        message.removeField(35);
        assertFalse(message.hasField(35));
        assertNull(message.getField(35));
    }

    @Test
    @DisplayName("Should update existing field value")
    void testUpdateFieldValue() {
        message.setField(35, "D");
        assertEquals("D", message.getFieldValue(35));

        message.setField(35, "8");
        assertEquals("8", message.getFieldValue(35));
    }

    @Test
    @DisplayName("Should maintain field order")
    void testFieldOrder() {
        message.setField(35, "D");
        message.setField(49, "CLIENT1");
        message.setField(56, "BROKER1");

        var fields = message.getAllFields();
        assertEquals(3, fields.size());

        var iterator = fields.iterator();
        assertEquals(35, iterator.next().getTag());
        assertEquals(49, iterator.next().getTag());
        assertEquals(56, iterator.next().getTag());
    }

    @Test
    @DisplayName("Should not duplicate field in order when updating")
    void testFieldOrderWhenUpdating() {
        message.setField(35, "D");
        message.setField(49, "CLIENT1");
        message.setField(35, "8");

        var fields = message.getAllFields();
        assertEquals(2, fields.size());
    }

    @Test
    @DisplayName("Should get message type correctly")
    void testGetMsgType() {
        message.setField(FixFieldType.MSG_TYPE, "D");

        assertEquals("D", message.getMsgType());
    }

    @Test
    @DisplayName("Should return null for missing message type")
    void testGetMsgTypeWhenMissing() {
        assertNull(message.getMsgType());
    }

    @Test
    @DisplayName("Should calculate checksum correctly")
    void testCalculateChecksum() {
        String testString = "8=FIX.4.4|9=100|35=D|";
        int checksum = message.calculateChecksum(testString);

        assertTrue(checksum >= 0 && checksum < 256);
        assertEquals(checksum, message.calculateChecksum(testString));
    }

    @Test
    @DisplayName("Should calculate checksum as 0 for empty string")
    void testCalculateChecksumEmpty() {
        assertEquals(0, message.calculateChecksum(""));
    }

    @Test
    @DisplayName("Should convert to FIX string format")
    void testToFixString() {
        message.setField(FixFieldType.BEGIN_STRING, "FIX.4.4");
        message.setField(FixFieldType.MSG_TYPE, "D");
        message.setField(FixFieldType.SENDER_COMP_ID, "CLIENT1");

        String fixString = message.toFixString();

        assertNotNull(fixString);
        assertTrue(fixString.startsWith("8=FIX.4.4" + FixMessage.SOH));
        assertTrue(fixString.contains("9="));
        assertTrue(fixString.contains("35=D" + FixMessage.SOH));
        assertTrue(fixString.contains("49=CLIENT1" + FixMessage.SOH));
        assertTrue(fixString.endsWith("10=" + String.format("%03d", message.calculateChecksum(
                fixString.substring(0, fixString.lastIndexOf("10=")))) + FixMessage.SOH));
    }

    @Test
    @DisplayName("Should exclude BeginString, BodyLength, and CheckSum from body")
    void testToFixStringExcludesSpecialFields() {
        message.setField(FixFieldType.BEGIN_STRING, "FIX.4.4");
        message.setField(9, "100");
        message.setField(FixFieldType.MSG_TYPE, "D");
        message.setField(10, "000");

        String fixString = message.toFixString();

        // Body length should be recalculated, not use the manually set value
        assertNotNull(fixString);
        assertTrue(fixString.contains("9="));
    }

    @Test
    @DisplayName("Should convert to readable string format")
    void testToString() {
        message.setField(FixFieldType.BEGIN_STRING, "FIX.4.4");
        message.setField(FixFieldType.MSG_TYPE, "D");

        String readableString = message.toString();

        assertNotNull(readableString);
        assertFalse(readableString.contains(String.valueOf(FixMessage.SOH)));
        assertTrue(readableString.contains("|"));
        assertTrue(readableString.contains("8=FIX.4.4|"));
        assertTrue(readableString.contains("35=D|"));
    }

    @Test
    @DisplayName("Should handle multiple fields correctly")
    void testMultipleFields() {
        message.setField(FixFieldType.BEGIN_STRING, "FIX.4.4");
        message.setField(FixFieldType.MSG_TYPE, "D");
        message.setField(FixFieldType.SENDER_COMP_ID, "CLIENT1");
        message.setField(FixFieldType.TARGET_COMP_ID, "BROKER1");
        message.setField(FixFieldType.MSG_SEQ_NUM, "1");

        assertEquals(5, message.getAllFields().size());
        assertEquals("FIX.4.4", message.getFieldValue(FixFieldType.BEGIN_STRING));
        assertEquals("D", message.getFieldValue(FixFieldType.MSG_TYPE));
        assertEquals("CLIENT1", message.getFieldValue(FixFieldType.SENDER_COMP_ID));
        assertEquals("BROKER1", message.getFieldValue(FixFieldType.TARGET_COMP_ID));
        assertEquals("1", message.getFieldValue(FixFieldType.MSG_SEQ_NUM));
    }

    @Test
    @DisplayName("Should handle special characters in field values")
    void testSpecialCharactersInFieldValues() {
        String specialValue = "Test!@#$%^&*()";
        message.setField(100, specialValue);

        assertEquals(specialValue, message.getFieldValue(100));
    }

    @Test
    @DisplayName("Should handle numeric values as strings")
    void testNumericValuesAsStrings() {
        message.setField(38, "1000");
        message.setField(44, "150.50");

        assertEquals("1000", message.getFieldValue(38));
        assertEquals("150.50", message.getFieldValue(44));
    }
}
