package com.example.fix;

/**
 * FIX协议相关异常
 */
public class FixException extends Exception {

    public FixException(String message) {
        super(message);
    }

    public FixException(String message, Throwable cause) {
        super(message, cause);
    }

    public FixException(Throwable cause) {
        super(cause);
    }
}