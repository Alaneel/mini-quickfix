package com.example.fix.message;

import com.example.fix.exception.FixException;
import com.example.fix.types.FixFieldType;

/**
 * FIX消息解析器
 */
public class FixParser {

    /**
     * 解析FIX消息字符串
     */
    public static FixMessage parse(String fixString) throws FixException {
        if (fixString == null || fixString.isEmpty()) {
            throw new FixException("FIX message string is null or empty");
        }

        FixMessage message = new FixMessage();

        // FIX消息使用SOH(0x01)作为分隔符
        String[] fields = fixString.split(String.valueOf(FixMessage.SOH));

        for (String field : fields) {
            if (field.isEmpty()) {
                continue;
            }

            int equalPos = field.indexOf('=');
            if (equalPos == -1) {
                throw new FixException("Invalid field format: " + field);
            }

            try {
                int tag = Integer.parseInt(field.substring(0, equalPos));
                String value = field.substring(equalPos + 1);
                message.setField(tag, value);
            } catch (NumberFormatException e) {
                throw new FixException("Invalid tag number in field: " + field, e);
            }
        }

        // 验证必需字段
        validateMessage(message);

        return message;
    }

    /**
     * 解析使用|作为分隔符的可读格式FIX消息
     */
    public static FixMessage parseReadable(String readableFixString) throws FixException {
        if (readableFixString == null || readableFixString.isEmpty()) {
            throw new FixException("FIX message string is null or empty");
        }

        // 将|替换为SOH,然后解析
        String fixString = readableFixString.replace('|', FixMessage.SOH);
        return parse(fixString);
    }

    /**
     * 验证消息必需字段
     */
    private static void validateMessage(FixMessage message) throws FixException {
        // 验证BeginString
        if (!message.hasField(FixFieldType.BEGIN_STRING)) {
            throw new FixException("Missing required field: BeginString (8)");
        }

        // 验证MsgType
        if (!message.hasField(FixFieldType.MSG_TYPE)) {
            throw new FixException("Missing required field: MsgType (35)");
        }

        // 验证SenderCompID
        if (!message.hasField(FixFieldType.SENDER_COMP_ID)) {
            throw new FixException("Missing required field: SenderCompID (49)");
        }

        // 验证TargetCompID
        if (!message.hasField(FixFieldType.TARGET_COMP_ID)) {
            throw new FixException("Missing required field: TargetCompID (56)");
        }

        // 验证MsgSeqNum
        if (!message.hasField(FixFieldType.MSG_SEQ_NUM)) {
            throw new FixException("Missing required field: MsgSeqNum (34)");
        }

        // 验证SendingTime
        if (!message.hasField(FixFieldType.SENDING_TIME)) {
            throw new FixException("Missing required field: SendingTime (52)");
        }
    }

    /**
     * 验证消息校验和
     */
    public static boolean validateChecksum(String fixString) {
        if (fixString == null || fixString.isEmpty()) {
            return false;
        }

        // 查找CheckSum字段
        int checksumPos = fixString.lastIndexOf("10=");
        if (checksumPos == -1) {
            return false;
        }

        // 提取校验和值
        int sohPos = fixString.indexOf(FixMessage.SOH, checksumPos);
        if (sohPos == -1) {
            return false;
        }

        String checksumStr = fixString.substring(checksumPos + 3, sohPos);
        int receivedChecksum;
        try {
            receivedChecksum = Integer.parseInt(checksumStr);
        } catch (NumberFormatException e) {
            return false;
        }

        // 计算实际校验和(不包括CheckSum字段本身)
        String messageWithoutChecksum = fixString.substring(0, checksumPos);
        int calculatedChecksum = 0;
        for (char c : messageWithoutChecksum.toCharArray()) {
            calculatedChecksum += c;
        }
        calculatedChecksum = calculatedChecksum % 256;

        return receivedChecksum == calculatedChecksum;
    }
}