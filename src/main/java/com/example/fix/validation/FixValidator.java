package com.example.fix.validation;

import com.example.fix.message.FixMessage;
import com.example.fix.types.FixFieldType;
import com.example.fix.types.FixMsgType;

import java.util.*;

/**
 * FIX消息验证器
 * 验证消息的完整性和业务规则
 */
public class FixValidator {

    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        private final List<String> warnings;

        public ValidationResult() {
            this.valid = true;
            this.errors = new ArrayList<>();
            this.warnings = new ArrayList<>();
        }

        public void addError(String error) {
            errors.add(error);
        }

        public void addWarning(String warning) {
            warnings.add(warning);
        }

        public boolean isValid() {
            return errors.isEmpty();
        }

        public List<String> getErrors() {
            return Collections.unmodifiableList(errors);
        }

        public List<String> getWarnings() {
            return Collections.unmodifiableList(warnings);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Valid: ").append(isValid()).append("\n");
            if (!errors.isEmpty()) {
                sb.append("Errors:\n");
                for (String error : errors) {
                    sb.append("  - ").append(error).append("\n");
                }
            }
            if (!warnings.isEmpty()) {
                sb.append("Warnings:\n");
                for (String warning : warnings) {
                    sb.append("  - ").append(warning).append("\n");
                }
            }
            return sb.toString();
        }
    }

    /**
     * 验证消息的基本结构
     */
    public static ValidationResult validateStructure(FixMessage message) {
        ValidationResult result = new ValidationResult();

        // 验证BeginString
        if (!message.hasField(FixFieldType.BEGIN_STRING)) {
            result.addError("Missing BeginString (8)");
        } else {
            String beginString = message.getFieldValue(FixFieldType.BEGIN_STRING);
            if (!beginString.startsWith("FIX.")) {
                result.addError("Invalid BeginString format: " + beginString);
            }
        }

        // 验证MsgType
        if (!message.hasField(FixFieldType.MSG_TYPE)) {
            result.addError("Missing MsgType (35)");
        } else {
            String msgType = message.getMsgType();
            if (FixMsgType.fromValue(msgType) == null) {
                result.addWarning("Unknown MsgType: " + msgType);
            }
        }

        // 验证会话字段
        if (!message.hasField(FixFieldType.SENDER_COMP_ID)) {
            result.addError("Missing SenderCompID (49)");
        }

        if (!message.hasField(FixFieldType.TARGET_COMP_ID)) {
            result.addError("Missing TargetCompID (56)");
        }

        if (!message.hasField(FixFieldType.MSG_SEQ_NUM)) {
            result.addError("Missing MsgSeqNum (34)");
        } else {
            try {
                int seqNum = Integer.parseInt(
                        message.getFieldValue(FixFieldType.MSG_SEQ_NUM));
                if (seqNum < 1) {
                    result.addError("Invalid MsgSeqNum: must be >= 1");
                }
            } catch (NumberFormatException e) {
                result.addError("Invalid MsgSeqNum format");
            }
        }

        if (!message.hasField(FixFieldType.SENDING_TIME)) {
            result.addError("Missing SendingTime (52)");
        }

        return result;
    }

    /**
     * 验证NewOrderSingle消息
     */
    public static ValidationResult validateNewOrderSingle(FixMessage message) {
        ValidationResult result = validateStructure(message);

        // 验证消息类型
        String msgType = message.getMsgType();
        if (!"D".equals(msgType)) {
            result.addError("Invalid MsgType for NewOrderSingle: " + msgType);
            return result;
        }

        // 验证必填字段
        if (!message.hasField(FixFieldType.CL_ORD_ID)) {
            result.addError("Missing ClOrdID (11)");
        }

        if (!message.hasField(FixFieldType.SYMBOL)) {
            result.addError("Missing Symbol (55)");
        }

        if (!message.hasField(FixFieldType.SIDE)) {
            result.addError("Missing Side (54)");
        } else {
            String side = message.getFieldValue(FixFieldType.SIDE);
            if (!"1".equals(side) && !"2".equals(side)) {
                result.addError("Invalid Side: must be 1 (Buy) or 2 (Sell)");
            }
        }

        if (!message.hasField(FixFieldType.ORDER_QTY)) {
            result.addError("Missing OrderQty (38)");
        } else {
            try {
                double qty = Double.parseDouble(
                        message.getFieldValue(FixFieldType.ORDER_QTY));
                if (qty <= 0) {
                    result.addError("Invalid OrderQty: must be > 0");
                }
            } catch (NumberFormatException e) {
                result.addError("Invalid OrderQty format");
            }
        }

        if (!message.hasField(FixFieldType.ORD_TYPE)) {
            result.addError("Missing OrdType (40)");
        } else {
            String ordType = message.getFieldValue(FixFieldType.ORD_TYPE);
            // 验证订单类型 (1=Market, 2=Limit, 3=Stop, 4=Stop Limit)
            if (!ordType.matches("[1-4]")) {
                result.addWarning("Uncommon OrdType: " + ordType);
            }

            // 限价单必须有价格
            if ("2".equals(ordType) && !message.hasField(FixFieldType.PRICE)) {
                result.addError("Limit order (OrdType=2) requires Price (44)");
            }
        }

        // 验证价格字段(如果存在)
        if (message.hasField(FixFieldType.PRICE)) {
            try {
                double price = Double.parseDouble(
                        message.getFieldValue(FixFieldType.PRICE));
                if (price <= 0) {
                    result.addError("Invalid Price: must be > 0");
                }
            } catch (NumberFormatException e) {
                result.addError("Invalid Price format");
            }
        }

        return result;
    }

    /**
     * 验证ExecutionReport消息
     */
    public static ValidationResult validateExecutionReport(FixMessage message) {
        ValidationResult result = validateStructure(message);

        String msgType = message.getMsgType();
        if (!"8".equals(msgType)) {
            result.addError("Invalid MsgType for ExecutionReport: " + msgType);
            return result;
        }

        // 必填字段
        String[] requiredFields = {"37", "17", "150", "39", "55", "54"};
        String[] fieldNames = {"OrderID", "ExecID", "ExecType", "OrdStatus", "Symbol", "Side"};

        for (int i = 0; i < requiredFields.length; i++) {
            if (!message.hasField(Integer.parseInt(requiredFields[i]))) {
                result.addError("Missing " + fieldNames[i] + " (" + requiredFields[i] + ")");
            }
        }

        return result;
    }

    /**
     * 验证Logon消息
     */
    public static ValidationResult validateLogon(FixMessage message) {
        ValidationResult result = validateStructure(message);

        String msgType = message.getMsgType();
        if (!"A".equals(msgType)) {
            result.addError("Invalid MsgType for Logon: " + msgType);
            return result;
        }

        // 验证心跳间隔
        if (!message.hasField(FixFieldType.HEARTBT_INT)) {
            result.addWarning("Missing HeartBtInt (108) - recommended for Logon");
        } else {
            try {
                int heartbeat = Integer.parseInt(
                        message.getFieldValue(FixFieldType.HEARTBT_INT));
                if (heartbeat < 0) {
                    result.addError("Invalid HeartBtInt: must be >= 0");
                }
            } catch (NumberFormatException e) {
                result.addError("Invalid HeartBtInt format");
            }
        }

        return result;
    }

    /**
     * 根据消息类型自动选择验证方法
     */
    public static ValidationResult validate(FixMessage message) {
        String msgType = message.getMsgType();
        if (msgType == null) {
            ValidationResult result = new ValidationResult();
            result.addError("Cannot validate: MsgType is null");
            return result;
        }

        switch (msgType) {
            case "A":
                return validateLogon(message);
            case "D":
                return validateNewOrderSingle(message);
            case "8":
                return validateExecutionReport(message);
            default:
                return validateStructure(message);
        }
    }
}