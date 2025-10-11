package com.example.fix.transport;

import com.example.fix.message.FixMessage;
import com.example.fix.exception.FixException;
import com.example.fix.message.FixParser;
import com.example.fix.session.FixSession;
import com.example.fix.types.FixFieldType;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * FIX TCP网络连接器
 * 处理FIX消息的发送和接收
 */
public class FixConnector {
    private final String host;
    private final int port;
    private Socket socket;
    private BufferedOutputStream outputStream;
    private BufferedInputStream inputStream;
    private ExecutorService executorService;
    private volatile boolean running;
    private Consumer<FixMessage> messageHandler;
    private Consumer<Exception> errorHandler;

    public FixConnector(String host, int port) {
        this.host = host;
        this.port = port;
        this.running = false;
        this.executorService = Executors.newFixedThreadPool(2);
    }

    /**
     * 设置消息处理器
     */
    public void setMessageHandler(Consumer<FixMessage> handler) {
        this.messageHandler = handler;
    }

    /**
     * 设置错误处理器
     */
    public void setErrorHandler(Consumer<Exception> handler) {
        this.errorHandler = handler;
    }

    /**
     * 连接到FIX服务器
     */
    public void connect() throws IOException {
        if (socket != null && socket.isConnected()) {
            throw new IllegalStateException("Already connected");
        }

        socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), 30000);
        socket.setTcpNoDelay(true);
        socket.setKeepAlive(true);

        outputStream = new BufferedOutputStream(socket.getOutputStream());
        inputStream = new BufferedInputStream(socket.getInputStream());

        running = true;

        // 启动接收线程
        executorService.submit(this::receiveLoop);

        System.out.println("Connected to " + host + ":" + port);
    }

    /**
     * 断开连接
     */
    public void disconnect() throws IOException {
        running = false;

        if (outputStream != null) {
            outputStream.close();
        }
        if (inputStream != null) {
            inputStream.close();
        }
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }

        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        System.out.println("Disconnected from " + host + ":" + port);
    }

    /**
     * 发送FIX消息
     */
    public void send(FixMessage message) throws IOException {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected");
        }

        String fixString = message.toFixString();
        byte[] bytes = fixString.getBytes(StandardCharsets.US_ASCII);

        synchronized (outputStream) {
            outputStream.write(bytes);
            outputStream.flush();
        }

        System.out.println("Sent: " + fixString.replace(FixMessage.SOH, '|'));
    }

    /**
     * 接收消息循环
     */
    private void receiveLoop() {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        try {
            while (running && !socket.isClosed()) {
                int b = inputStream.read();
                if (b == -1) {
                    break; // 连接关闭
                }

                buffer.write(b);

                // 检查是否接收到完整消息 (以CheckSum结束)
                if (isCompleteMessage(buffer.toByteArray())) {
                    String fixString = buffer.toString(StandardCharsets.US_ASCII.name());
                    buffer.reset();

                    System.out.println("Received: " + fixString.replace(FixMessage.SOH, '|'));

                    // 解析并处理消息
                    try {
                        FixMessage message = FixParser.parse(fixString);
                        if (messageHandler != null) {
                            executorService.submit(() -> messageHandler.accept(message));
                        }
                    } catch (FixException e) {
                        handleError(e);
                    }
                }
            }
        } catch (IOException e) {
            if (running) {
                handleError(e);
            }
        }
    }

    /**
     * 检查是否是完整的FIX消息
     */
    private boolean isCompleteMessage(byte[] data) {
        if (data.length < 10) {
            return false;
        }

        // 检查是否包含 "10=xxx" 校验和字段
        String str = new String(data, StandardCharsets.US_ASCII);
        int checksumPos = str.lastIndexOf("10=");
        if (checksumPos == -1) {
            return false;
        }

        // 检查校验和后是否有SOH
        int sohPos = str.indexOf(FixMessage.SOH, checksumPos);
        return sohPos != -1;
    }

    /**
     * 处理错误
     */
    private void handleError(Exception e) {
        if (errorHandler != null) {
            errorHandler.accept(e);
        } else {
            e.printStackTrace();
        }
    }

    /**
     * 检查连接状态
     */
    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed() && running;
    }

    /**
     * 获取连接信息
     */
    public String getConnectionInfo() {
        return host + ":" + port;
    }
}

/**
 * FIX客户端示例
 */
class FixClientExample {
    public static void main(String[] args) {
        FixConnector connector = new FixConnector("localhost", 9878);
        FixSession session = new FixSession("CLIENT1", "BROKER1", 30);

        // 设置消息处理器
        connector.setMessageHandler(message -> {
            System.out.println("Handling message: " + message.getMsgType());

            try {
                // 根据消息类型处理
                String msgType = message.getMsgType();
                switch (msgType) {
                    case "A": // Logon
                        System.out.println("Logon received");
                        break;
                    case "0": // Heartbeat
                        System.out.println("Heartbeat received");
                        break;
                    case "1": // TestRequest
                        String testReqId = message.getFieldValue(FixFieldType.TEST_REQ_ID);
                        FixMessage heartbeat = session.createHeartbeat(testReqId);
                        connector.send(heartbeat);
                        break;
                    case "8": // ExecutionReport
                        System.out.println("Execution report received");
                        break;
                }

                // 处理序列号
                session.processIncomingMessage(message);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // 设置错误处理器
        connector.setErrorHandler(error -> {
            System.err.println("Connection error: " + error.getMessage());
        });

        try {
            // 连接
            connector.connect();

            // 发送Logon
            FixMessage logon = session.createLogon();
            connector.send(logon);

            // 启动心跳线程
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(() -> {
                try {
                    if (session.needsHeartbeat()) {
                        FixMessage heartbeat = session.createHeartbeat();
                        connector.send(heartbeat);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 10, 10, TimeUnit.SECONDS);

            // 等待一段时间
            Thread.sleep(60000);

            // 发送Logout并断开
            FixMessage logout = session.createLogout();
            connector.send(logout);

            Thread.sleep(1000);
            connector.disconnect();
            scheduler.shutdown();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}