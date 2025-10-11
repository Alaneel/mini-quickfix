package com.example.fix;

import java.util.*;

/**
 * FIX消息类,包含消息头、消息体和消息尾
 */
public class FixMessage {
    public static final char SOH = '\u0001'; // FIX分隔符

    private final Map<Integer, FixField> fields;
    private final List<Integer> fieldOrder; // 保持字段顺序

    public FixMessage() {
        this.fields = new LinkedHashMap<>();
        this.fieldOrder = new ArrayList<>();
    }

    /**
     * 添加字段
     */
    public void setField(int tag, String value) {
        FixField field = new FixField(tag, value);
        if (!fields.containsKey(tag)) {
            fieldOrder.add(tag);
        }
        fields.put(tag, field);
    }

    public void setField(FixFieldType fieldType, String value) {
        setField(fieldType.getTag(), value);
    }

    /**
     * 获取字段
     */
    public FixField getField(int tag) {
        return fields.get(tag);
    }

    public FixField getField(FixFieldType fieldType) {
        return fields.get(fieldType.getTag());
    }

    /**
     * 获取字段值
     */
    public String getFieldValue(int tag) {
        FixField field = fields.get(tag);
        return field != null ? field.getValue() : null;
    }

    public String getFieldValue(FixFieldType fieldType) {
        return getFieldValue(fieldType.getTag());
    }

    /**
     * 检查字段是否存在
     */
    public boolean hasField(int tag) {
        return fields.containsKey(tag);
    }

    public boolean hasField(FixFieldType fieldType) {
        return fields.containsKey(fieldType.getTag());
    }

    /**
     * 移除字段
     */
    public void removeField(int tag) {
        fields.remove(tag);
        fieldOrder.remove(Integer.valueOf(tag));
    }

    /**
     * 获取所有字段
     */
    public Collection<FixField> getAllFields() {
        List<FixField> result = new ArrayList<>();
        for (Integer tag : fieldOrder) {
            result.add(fields.get(tag));
        }
        return result;
    }

    /**
     * 获取消息类型
     */
    public String getMsgType() {
        return getFieldValue(FixFieldType.MSG_TYPE);
    }

    /**
     * 计算校验和
     */
    public int calculateChecksum(String message) {
        int checksum = 0;
        for (char c : message.toCharArray()) {
            checksum += c;
        }
        return checksum % 256;
    }

    /**
     * 将消息转换为FIX格式字符串
     */
    public String toFixString() {
        StringBuilder body = new StringBuilder();

        // 构建消息体(除了BeginString, BodyLength和CheckSum)
        for (Integer tag : fieldOrder) {
            if (tag != 8 && tag != 9 && tag != 10) {
                FixField field = fields.get(tag);
                body.append(field.getTag()).append('=')
                        .append(field.getValue()).append(SOH);
            }
        }

        // 计算BodyLength
        int bodyLength = body.length();

        // 构建完整消息
        StringBuilder message = new StringBuilder();

        // BeginString
        String beginString = getFieldValue(8);
        if (beginString != null) {
            message.append("8=").append(beginString).append(SOH);
        }

        // BodyLength
        message.append("9=").append(bodyLength).append(SOH);

        // Body
        message.append(body);

        // 计算CheckSum
        int checksum = calculateChecksum(message.toString());
        String checksumStr = String.format("%03d", checksum);
        message.append("10=").append(checksumStr).append(SOH);

        return message.toString();
    }

    @Override
    public String toString() {
        return toFixString().replace(SOH, '|');
    }
}