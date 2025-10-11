package com.example.fix;

/**
 * FIX字段类,表示FIX消息中的一个tag=value对
 */
public class FixField {
    private final int tag;
    private final String value;

    public FixField(int tag, String value) {
        if (value == null) {
            throw new IllegalArgumentException("Field value cannot be null");
        }
        this.tag = tag;
        this.value = value;
    }

    public int getTag() {
        return tag;
    }

    public String getValue() {
        return value;
    }

    public int getValueAsInt() {
        return Integer.parseInt(value);
    }

    public double getValueAsDouble() {
        return Double.parseDouble(value);
    }

    public long getValueAsLong() {
        return Long.parseLong(value);
    }

    @Override
    public String toString() {
        return tag + "=" + value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FixField fixField = (FixField) o;
        return tag == fixField.tag && value.equals(fixField.value);
    }

    @Override
    public int hashCode() {
        return 31 * tag + value.hashCode();
    }
}