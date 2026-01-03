package com.snapcloud.api.dto;

import com.snapcloud.api.domain.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String email;
    private Role role;
    private Instant expiresAt;
    private String message;

    public static AuthResponse success(String accessToken, String email, Role role, Instant expiresAt) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .email(email)
                .role(role)
                .expiresAt(expiresAt)
                .message("Login Successful")
                .build();
    }
}