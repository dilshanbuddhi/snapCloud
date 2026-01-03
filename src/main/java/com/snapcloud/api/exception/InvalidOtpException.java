package com.snapcloud.api.exception;

public class InvalidOtpException extends RuntimeException {
    public InvalidOtpException() {
        super();
    }
    public InvalidOtpException(String message) {
        super(message);
    }
    public InvalidOtpException(String message, Throwable cause) {
        super(message, cause);
    }
}

