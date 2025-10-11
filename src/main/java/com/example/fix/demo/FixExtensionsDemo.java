package com.example.fix.demo;

import com.example.fix.exception.FixException;
import com.example.fix.message.*;
import com.example.fix.session.FixSession;
import com.example.fix.types.*;
import com.example.fix.validation.FixValidator;
import com.example.fix.store.FixMessageStore;
import com.example.fix.event.FixEventListener;

/**
 * Comprehensive demonstration of Mini QuickFIX extension features
 *
 * This demo showcases:
 * - Message Validation (FixValidator)
 * - Repeating Groups (FixRepeatingGroup)
 * - Message Persistence (FixMessageStore)
 * - Event Listeners (FixEventListener)
 */
public class FixExtensionsDemo {

    public static void main(String[] args) {
        try {
            System.out.println("========================================");
            System.out.println("  Mini QuickFIX Extensions Demo");
            System.out.println("========================================\n");

            // Feature 1: Message Validation
            demonstrateValidation();

            // Feature 2: Repeating Groups
            demonstrateRepeatingGroups();

            // Feature 3: Message Persistence
            demonstratePersistence();

            // Feature 4: Event Listeners
            demonstrateEventListeners();

            System.out.println("\n========================================");
            System.out.println("  Demo Complete!");
            System.out.println("========================================");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Feature 1: Message Validation
     * Demonstrates automatic validation of FIX messages
     */
    private static void demonstrateValidation() throws FixException {
        System.out.println("=== Feature 1: Message Validation ===\n");

        // Create a valid order
        FixMessage validOrder = FixBuilder.newOrderSingle()
                .sender("CLIENT1")
                .target("BROKER1")
                .msgSeqNum(1)
                .sendingTime()
                .field(FixFieldType.CL_ORD_ID, "ORDER123")
                .field(FixFieldType.SYMBOL, "AAPL")
                .field(FixFieldType.SIDE, "1")  // Buy
                .field(FixFieldType.ORDER_QTY, 100)
                .field(FixFieldType.ORD_TYPE, "2")  // Limit
                .field(FixFieldType.PRICE, 150.50)
                .build();

        FixValidator.ValidationResult result1 = FixValidator.validateNewOrderSingle(validOrder);
        System.out.println("Valid Order Validation:");
        System.out.println("  Status: " + (result1.isValid() ? "PASS" : "FAIL"));
        System.out.println("  Errors: " + result1.getErrors().size());
        System.out.println("  Warnings: " + result1.getWarnings().size());

        // Create an invalid order (missing required fields)
        FixMessage invalidOrder = FixBuilder.newOrderSingle()
                .sender("CLIENT1")
                .target("BROKER1")
                .msgSeqNum(2)
                .sendingTime()
                .field(FixFieldType.CL_ORD_ID, "ORDER124")
                .field(FixFieldType.SYMBOL, "GOOGL")
                // Missing: Side, OrderQty, OrdType
                .build();

        FixValidator.ValidationResult result2 = FixValidator.validateNewOrderSingle(invalidOrder);
        System.out.println("\nInvalid Order Validation:");
        System.out.println("  Status: " + (result2.isValid() ? "PASS" : "FAIL"));
        System.out.println("  Errors: " + result2.getErrors().size());
        if (!result2.getErrors().isEmpty()) {
            System.out.println("  Error Details:");
            for (String error : result2.getErrors()) {
                System.out.println("    - " + error);
            }
        }

        System.out.println();
    }

    /**
     * Feature 2: Repeating Groups
     * Demonstrates handling of repeating groups for market data
     */
    private static void demonstrateRepeatingGroups() throws FixException {
        System.out.println("=== Feature 2: Repeating Groups ===\n");

        // Create market data snapshot message
        FixMessage marketData = new FixBuilder("W")  // MarketDataSnapshot
                .sender("EXCHANGE1")
                .target("CLIENT1")
                .msgSeqNum(100)
                .sendingTime()
                .field(FixFieldType.SYMBOL, "AAPL")
                .build();

        // Create repeating group for market data entries (NoMDEntries=268, MDEntryType=269)
        FixRepeatingGroup mdEntries = new FixRepeatingGroup(268, 269);

        // Add bid entry
        mdEntries.addEntry()
                .setField(269, "0")      // MDEntryType = Bid
                .setField(270, "150.25")  // MDEntryPx
                .setField(271, "1000");   // MDEntrySize

        // Add offer entry
        mdEntries.addEntry()
                .setField(269, "1")      // MDEntryType = Offer
                .setField(270, "150.30")  // MDEntryPx
                .setField(271, "800");    // MDEntrySize

        // Add last trade entry
        mdEntries.addEntry()
                .setField(269, "2")      // MDEntryType = Trade
                .setField(270, "150.27")  // MDEntryPx
                .setField(271, "500");    // MDEntrySize

        // Add to message
        mdEntries.addToMessage(marketData);

        System.out.println("Market Data Snapshot with Repeating Groups:");
        System.out.println("  Symbol: AAPL");
        System.out.println("  Number of Entries: " + mdEntries.getCount());
        System.out.println("  Entry 0 (Bid):   Price=" + mdEntries.getFieldValue(0, 270) +
                           ", Size=" + mdEntries.getFieldValue(0, 271));
        System.out.println("  Entry 1 (Offer): Price=" + mdEntries.getFieldValue(1, 270) +
                           ", Size=" + mdEntries.getFieldValue(1, 271));
        System.out.println("  Entry 2 (Trade): Price=" + mdEntries.getFieldValue(2, 270) +
                           ", Size=" + mdEntries.getFieldValue(2, 271));

        System.out.println();
    }

    /**
     * Feature 3: Message Persistence
     * Demonstrates message storage and sequence number management
     */
    private static void demonstratePersistence() throws Exception {
        System.out.println("=== Feature 3: Message Persistence ===\n");

        // Create message store
        FixMessageStore store = new FixMessageStore("./fix_demo_store", "CLIENT1-BROKER1");

        // Load existing sequence numbers (or start with 1,1)
        FixMessageStore.SequenceNumbers seqNums = store.loadSequenceNumbers();
        System.out.println("Loaded Sequence Numbers: " + seqNums);

        // Create session
        FixSession session = new FixSession("CLIENT1", "BROKER1", 30);

        // Create and store logon message
        FixMessage logon = session.createLogon();
        int logonSeq = Integer.parseInt(logon.getFieldValue(FixFieldType.MSG_SEQ_NUM));
        store.storeOutgoingMessage(logonSeq, logon);
        System.out.println("Stored Logon message with SEQ=" + logonSeq);

        // Create and store order message
        FixMessage order = FixBuilder.newOrderSingle()
                .sender("CLIENT1")
                .target("BROKER1")
                .msgSeqNum(session.getOutgoingSeqNum())
                .sendingTime()
                .field(FixFieldType.CL_ORD_ID, "ORDER001")
                .field(FixFieldType.SYMBOL, "AAPL")
                .field(FixFieldType.SIDE, "1")
                .field(FixFieldType.ORDER_QTY, 100)
                .field(FixFieldType.ORD_TYPE, "2")
                .field(FixFieldType.PRICE, 150.00)
                .build();

        int orderSeq = Integer.parseInt(order.getFieldValue(FixFieldType.MSG_SEQ_NUM));
        store.storeOutgoingMessage(orderSeq, order);
        System.out.println("Stored Order message with SEQ=" + orderSeq);

        // Demonstrate message retrieval for resend
        System.out.println("\nRetrieving messages for resend (SEQ 1-2):");
        int count = 0;
        for (String msg : store.getMessages(1, 2)) {
            if (msg != null) {
                count++;
                System.out.println("  SEQ " + count + ": " + msg.substring(0, Math.min(60, msg.length())) + "...");
            }
        }

        store.close();
        System.out.println();
    }

    /**
     * Feature 4: Event Listeners
     * Demonstrates event-driven architecture with listeners
     */
    private static void demonstrateEventListeners() throws Exception {
        System.out.println("=== Feature 4: Event Listeners ===\n");

        // Create custom event listener inline
        FixEventListener simpleListener = new FixEventListener() {
            private int messagesSent = 0;
            private int messagesReceived = 0;

            @Override
            public void onLogon(FixSession session, FixMessage logonMessage) {
                System.out.println("[Listener] Logon: " + session.getSenderCompId() + " logged on");
            }

            @Override
            public void onBeforeSend(FixSession session, FixMessage message) {
                messagesSent++;
                System.out.println("[Listener] Sending: " + message.getMsgType() +
                                 " (Total sent: " + messagesSent + ")");
            }

            @Override
            public void onMessageReceived(FixSession session, FixMessage message) {
                messagesReceived++;
                System.out.println("[Listener] Received: " + message.getMsgType() +
                                 " (Total received: " + messagesReceived + ")");

                // Handle specific message types
                if ("D".equals(message.getMsgType())) {
                    String clOrdId = message.getFieldValue(FixFieldType.CL_ORD_ID);
                    String symbol = message.getFieldValue(FixFieldType.SYMBOL);
                    System.out.println("[Listener]   New Order: " + clOrdId + " for " + symbol);
                }
            }

            @Override
            public void onLogout(FixSession session, FixMessage logoutMessage) {
                System.out.println("[Listener] Logout: Session ended");
                System.out.println("[Listener] Final Stats - Sent: " + messagesSent +
                                 ", Received: " + messagesReceived);
            }

            @Override
            public void onError(FixSession session, Exception error) {
                System.out.println("[Listener] Error: " + error.getMessage());
            }
        };

        System.out.println("Registered custom event listener\n");

        // Create session
        FixSession session = new FixSession("CLIENT1", "BROKER1", 30);
        simpleListener.onSessionCreated(session);

        // Simulate logon
        FixMessage logon = session.createLogon();
        simpleListener.onBeforeSend(session, logon);
        simpleListener.onLogon(session, logon);

        // Simulate sending an order
        FixMessage order = FixBuilder.newOrderSingle()
                .sender("CLIENT1")
                .target("BROKER1")
                .msgSeqNum(session.getOutgoingSeqNum())
                .sendingTime()
                .field(FixFieldType.CL_ORD_ID, "ORDER001")
                .field(FixFieldType.SYMBOL, "AAPL")
                .field(FixFieldType.SIDE, "1")
                .field(FixFieldType.ORDER_QTY, 100)
                .field(FixFieldType.ORD_TYPE, "2")
                .field(FixFieldType.PRICE, 150.00)
                .build();

        simpleListener.onBeforeSend(session, order);

        // Simulate receiving execution report
        FixMessage execReport = FixBuilder.executionReport()
                .sender("BROKER1")
                .target("CLIENT1")
                .msgSeqNum(1)
                .sendingTime()
                .field(FixFieldType.ORDER_ID, "ORD001")
                .field(FixFieldType.EXEC_ID, "EXEC001")
                .field(FixFieldType.EXEC_TYPE, "0")  // New
                .field(FixFieldType.ORD_STATUS, "0")  // New
                .field(FixFieldType.SYMBOL, "AAPL")
                .field(FixFieldType.SIDE, "1")
                .field(FixFieldType.ORDER_QTY, 100)
                .field(11, "ORDER001")  // ClOrdID
                .build();

        simpleListener.onMessageReceived(session, execReport);

        // Send heartbeats
        for (int i = 0; i < 2; i++) {
            FixMessage heartbeat = session.createHeartbeat();
            simpleListener.onBeforeSend(session, heartbeat);
        }

        // Simulate logout
        FixMessage logout = session.createLogout();
        simpleListener.onBeforeSend(session, logout);
        simpleListener.onLogout(session, logout);

        System.out.println();
    }
}
