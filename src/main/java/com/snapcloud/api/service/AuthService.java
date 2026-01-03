package com.snapcloud.api.service;

import com.snapcloud.api.dto.AuthRequest;
import com.snapcloud.api.dto.AuthResponse;

public interface AuthService {
    // Register a user (sends OTP) — returns AuthResponse with message (no token yet)
    AuthResponse register(AuthRequest request);

    // Authenticate user credentials and return JWT token in AuthResponse
    AuthResponse authenticate(AuthRequest request);

    // Verify OTP for given email and return AuthResponse containing token when successful
    AuthResponse verifyOtp(String email, String otp);
}
