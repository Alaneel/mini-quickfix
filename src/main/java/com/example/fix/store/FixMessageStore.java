package com.example.fix.store;

import com.example.fix.message.FixMessage;
import com.example.fix.session.FixSession;
import com.example.fix.exception.FixException;
import com.example.fix.types.FixFieldType;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FIX消息存储
 * 用于持久化消息和序列号,支持会话恢复
 */
public class FixMessageStore {
    private static final DateTimeFormatter FILE_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss.SSS");

    private final Path storePath;
    private final String sessionId;
    private final Map<Integer, String> messageCache;
    private BufferedWriter messageWriter;

    /**
     * 创建消息存储
     * @param storePath 存储目录
     * @param sessionId 会话标识 (例如: CLIENT1-BROKER1)
     */
    public FixMessageStore(String storePath, String sessionId) throws IOException {
        this.storePath = Paths.get(storePath);
        this.sessionId = sessionId;
        this.messageCache = new ConcurrentHashMap<>();

        // 创建存储目录
        Files.createDirectories(this.storePath);

        // 初始化今天的消息文件
        initMessageFile();
    }

    /**
     * 初始化消息文件
     */
    private void initMessageFile() throws IOException {
        String date = LocalDateTime.now().format(FILE_DATE_FORMAT);
        String filename = String.format("%s-%s.log", sessionId, date);
        Path messagePath = storePath.resolve(filename);

        messageWriter = Files.newBufferedWriter(messagePath,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    /**
     * 存储出站消息
     */
    public void storeOutgoingMessage(int seqNum, FixMessage message) throws IOException {
        String fixString = message.toFixString();
        messageCache.put(seqNum, fixString);

        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String logEntry = String.format("[%s] OUT SEQ=%d: %s%n",
                timestamp, seqNum, fixString.replace(FixMessage.SOH, '|'));

        synchronized (messageWriter) {
            messageWriter.write(logEntry);
            messageWriter.flush();
        }
    }

    /**
     * 存储入站消息
     */
    public void storeIncomingMessage(int seqNum, FixMessage message) throws IOException {
        String fixString = message.toFixString();

        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String logEntry = String.format("[%s] IN  SEQ=%d: %s%n",
                timestamp, seqNum, fixString.replace(FixMessage.SOH, '|'));

        synchronized (messageWriter) {
            messageWriter.write(logEntry);
            messageWriter.flush();
        }
    }

    /**
     * 获取指定序列号的消息(用于重传)
     */
    public String getMessage(int seqNum) {
        return messageCache.get(seqNum);
    }

    /**
     * 获取序列号范围内的消息
     */
    public List<String> getMessages(int beginSeqNum, int endSeqNum) {
        List<String> messages = new ArrayList<>();
        for (int i = beginSeqNum; i <= endSeqNum; i++) {
            String msg = messageCache.get(i);
            if (msg != null) {
                messages.add(msg);
            }
        }
        return messages;
    }

    /**
     * 保存序列号
     */
    public void saveSequenceNumbers(int outgoingSeqNum, int incomingSeqNum)
            throws IOException {
        Path seqPath = storePath.resolve(sessionId + ".seq");

        try (BufferedWriter writer = Files.newBufferedWriter(seqPath,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            writer.write("OutgoingSeqNum=" + outgoingSeqNum);
            writer.newLine();
            writer.write("IncomingSeqNum=" + incomingSeqNum);
            writer.newLine();
            writer.write("Timestamp=" + LocalDateTime.now().format(TIMESTAMP_FORMAT));
            writer.newLine();
        }
    }

    /**
     * 加载序列号
     */
    public SequenceNumbers loadSequenceNumbers() throws IOException {
        Path seqPath = storePath.resolve(sessionId + ".seq");

        if (!Files.exists(seqPath)) {
            return new SequenceNumbers(1, 1);
        }

        int outgoing = 1;
        int incoming = 1;

        try (BufferedReader reader = Files.newBufferedReader(seqPath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("OutgoingSeqNum=")) {
                    outgoing = Integer.parseInt(line.substring(15));
                } else if (line.startsWith("IncomingSeqNum=")) {
                    incoming = Integer.parseInt(line.substring(15));
                }
            }
        }

        return new SequenceNumbers(outgoing, incoming);
    }

    /**
     * 重置存储(清除消息和序列号)
     */
    public void reset() throws IOException {
        messageCache.clear();

        // 删除序列号文件
        Path seqPath = storePath.resolve(sessionId + ".seq");
        Files.deleteIfExists(seqPath);

        // 重新初始化消息文件
        close();
        initMessageFile();
    }

    /**
     * 加载历史消息到缓存
     */
    public void loadHistoricalMessages() throws IOException {
        // 读取今天的日志文件
        String date = LocalDateTime.now().format(FILE_DATE_FORMAT);
        String filename = String.format("%s-%s.log", sessionId, date);
        Path messagePath = storePath.resolve(filename);

        if (!Files.exists(messagePath)) {
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(messagePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                // 解析日志行: [timestamp] OUT/IN SEQ=xxx: message
                if (line.contains("OUT SEQ=")) {
                    int seqStart = line.indexOf("SEQ=") + 4;
                    int seqEnd = line.indexOf(":", seqStart);
                    int seqNum = Integer.parseInt(line.substring(seqStart, seqEnd).trim());

                    int msgStart = seqEnd + 2;
                    String fixString = line.substring(msgStart)
                            .replace('|', FixMessage.SOH);

                    messageCache.put(seqNum, fixString);
                }
            }
        }
    }

    /**
     * 关闭存储
     */
    public void close() throws IOException {
        if (messageWriter != null) {
            messageWriter.close();
        }
    }

    /**
     * 序列号对
     */
    public static class SequenceNumbers {
        private final int outgoing;
        private final int incoming;

        public SequenceNumbers(int outgoing, int incoming) {
            this.outgoing = outgoing;
            this.incoming = incoming;
        }

        public int getOutgoing() {
            return outgoing;
        }

        public int getIncoming() {
            return incoming;
        }

        @Override
        public String toString() {
            return "SequenceNumbers{outgoing=" + outgoing +
                    ", incoming=" + incoming + "}";
        }
    }
}

/**
 * 增强的FIX会话(支持消息存储)
 */
class FixSessionWithStore extends FixSession {
    private final FixMessageStore store;

    public FixSessionWithStore(String senderCompId, String targetCompId,
                               int heartbeatInterval, FixMessageStore store) {
        super(senderCompId, targetCompId, heartbeatInterval);
        this.store = store;

        // 加载序列号
        try {
            FixMessageStore.SequenceNumbers seqNums = store.loadSequenceNumbers();
            System.out.println("Loaded sequence numbers: " + seqNums);
        } catch (IOException e) {
            System.err.println("Failed to load sequence numbers: " + e.getMessage());
        }
    }

    /**
     * 创建消息并存储
     */
    @Override
    public FixMessage createMessage(String msgType) throws FixException {
        FixMessage message = super.createMessage(msgType);

        try {
            int seqNum = getOutgoingSeqNum() - 1; // 已经递增了
            store.storeOutgoingMessage(seqNum, message);
            store.saveSequenceNumbers(getOutgoingSeqNum(), getIncomingSeqNum());
        } catch (IOException e) {
            throw new FixException("Failed to store message", e);
        }

        return message;
    }

    /**
     * 处理入站消息并存储
     */
    @Override
    public void processIncomingMessage(FixMessage message) throws FixException {
        super.processIncomingMessage(message);

        try {
            int seqNum = getIncomingSeqNum() - 1; // 已经递增了
            store.storeIncomingMessage(seqNum, message);
            store.saveSequenceNumbers(getOutgoingSeqNum(), getIncomingSeqNum());
        } catch (IOException e) {
            throw new FixException("Failed to store message", e);
        }
    }

    /**
     * 获取消息用于重传
     */
    public List<String> getMessagesForResend(int beginSeqNum, int endSeqNum) {
        return store.getMessages(beginSeqNum, endSeqNum);
    }
}