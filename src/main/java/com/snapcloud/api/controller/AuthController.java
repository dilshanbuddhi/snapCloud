package com.snapcloud.api.controller;

import lombok.RequiredArgsConstructor;
import com.snapcloud.api.dto.AuthRequest;
import com.snapcloud.api.dto.AuthResponse;
import com.snapcloud.api.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(
          @RequestBody AuthRequest request
  ) {
    return ResponseEntity.ok(authService.register(request));
  }

  @PostMapping("/authenticate")
  public ResponseEntity<AuthResponse> authenticate(
          @RequestBody AuthRequest request
  ) {
    return ResponseEntity.ok(authService.authenticate(request));
  }

  @PostMapping("/verify")
  public ResponseEntity<AuthResponse> verifyOtp(@RequestBody Map<String, String> body) {
    String email = body.get("email");
    String otp = body.get("otp");
    return ResponseEntity.ok(authService.verifyOtp(email, otp));
  }
}