package com.snapcloud.api.exception.custom;

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

