package com.snapcloud.api.util;

import java.security.SecureRandom;

public final class OtpUtils {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int OTP_LENGTH = 6;

    private OtpUtils() { /* no instances */ }

    public static String generateOtp() {
        int number = RANDOM.nextInt((int) Math.pow(10, OTP_LENGTH));
        return String.format("%0" + OTP_LENGTH + "d", number);
    }
}

