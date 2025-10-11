package com.example.fix.message;

import java.util.*;

/**
 * FIX重复组支持
 * 用于处理FIX消息中的重复组结构,如市场数据快照中的多个条目
 */
public class FixRepeatingGroup {
    private final int countTag; // 组计数字段的tag
    private final int firstFieldTag; // 组内第一个字段的tag
    private final List<Map<Integer, String>> entries; // 组内的条目列表

    /**
     * 创建重复组
     * @param countTag 组计数字段的tag (例如: 268 for NoMDEntries)
     * @param firstFieldTag 组内第一个字段的tag (例如: 269 for MDEntryType)
     */
    public FixRepeatingGroup(int countTag, int firstFieldTag) {
        this.countTag = countTag;
        this.firstFieldTag = firstFieldTag;
        this.entries = new ArrayList<>();
    }

    /**
     * 添加一个条目
     */
    public FixRepeatingGroup addEntry() {
        entries.add(new LinkedHashMap<>());
        return this;
    }

    /**
     * 在当前条目中添加字段
     */
    public FixRepeatingGroup setField(int tag, String value) {
        if (entries.isEmpty()) {
            addEntry();
        }
        entries.get(entries.size() - 1).put(tag, value);
        return this;
    }

    /**
     * 在指定条目中添加字段
     */
    public FixRepeatingGroup setField(int entryIndex, int tag, String value) {
        if (entryIndex >= entries.size()) {
            throw new IndexOutOfBoundsException("Entry index out of bounds: " + entryIndex);
        }
        entries.get(entryIndex).put(tag, value);
        return this;
    }

    /**
     * 获取条目数量
     */
    public int getCount() {
        return entries.size();
    }

    /**
     * 获取所有条目
     */
    public List<Map<Integer, String>> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    /**
     * 获取指定条目
     */
    public Map<Integer, String> getEntry(int index) {
        return entries.get(index);
    }

    /**
     * 获取指定条目的字段值
     */
    public String getFieldValue(int entryIndex, int tag) {
        if (entryIndex >= entries.size()) {
            return null;
        }
        return entries.get(entryIndex).get(tag);
    }

    /**
     * 将重复组添加到FIX消息中
     */
    public void addToMessage(FixMessage message) {
        // 添加计数字段
        message.setField(countTag, String.valueOf(entries.size()));

        // 添加每个条目的字段
        for (Map<Integer, String> entry : entries) {
            for (Map.Entry<Integer, String> field : entry.entrySet()) {
                message.setField(field.getKey(), field.getValue());
            }
        }
    }

    /**
     * 从FIX消息中解析重复组
     */
    public static FixRepeatingGroup parseFromMessage(
            FixMessage message, int countTag, int firstFieldTag) {

        FixRepeatingGroup group = new FixRepeatingGroup(countTag, firstFieldTag);

        // 获取条目数量
        String countStr = message.getFieldValue(countTag);
        if (countStr == null) {
            return group;
        }

        int count = Integer.parseInt(countStr);

        // 解析每个条目
        // 注意: 这是简化实现,实际解析需要更复杂的逻辑
        Collection<FixField> allFields = message.getAllFields();
        Map<Integer, String> currentEntry = null;
        int entryCount = 0;

        for (FixField field : allFields) {
            if (field.getTag() == firstFieldTag) {
                // 新条目开始
                if (currentEntry != null) {
                    group.entries.add(currentEntry);
                    entryCount++;
                }
                currentEntry = new LinkedHashMap<>();
                currentEntry.put(field.getTag(), field.getValue());

                if (entryCount >= count) {
                    break;
                }
            } else if (currentEntry != null) {
                currentEntry.put(field.getTag(), field.getValue());
            }
        }

        // 添加最后一个条目
        if (currentEntry != null && entryCount < count) {
            group.entries.add(currentEntry);
        }

        return group;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RepeatingGroup[countTag=").append(countTag)
                .append(", count=").append(entries.size()).append("]\n");

        for (int i = 0; i < entries.size(); i++) {
            sb.append("  Entry ").append(i).append(":\n");
            for (Map.Entry<Integer, String> field : entries.get(i).entrySet()) {
                sb.append("    ").append(field.getKey())
                        .append("=").append(field.getValue()).append("\n");
            }
        }

        return sb.toString();
    }
}

/**
 * 常见重复组定义
 */
class CommonRepeatingGroups {
    /**
     * 市场数据条目组 (Market Data Entries)
     * NoMDEntries(268) -> MDEntryType(269)
     */
    public static FixRepeatingGroup createMarketDataEntries() {
        return new FixRepeatingGroup(268, 269);
    }

    /**
     * 行情订阅列表 (Market Data Request)
     * NoRelatedSym(146) -> Symbol(55)
     */
    public static FixRepeatingGroup createSymbolList() {
        return new FixRepeatingGroup(146, 55);
    }

    /**
     * 执行腿 (Execution Legs)
     * NoLegs(555) -> LegSymbol(600)
     */
    public static FixRepeatingGroup createExecutionLegs() {
        return new FixRepeatingGroup(555, 600);
    }
}