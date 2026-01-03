package com.snapcloud.api.controller;

import lombok.RequiredArgsConstructor;
import com.snapcloud.api.dto.AuthRequest;
import com.snapcloud.api.dto.AuthResponse;
import com.snapcloud.api.service.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthenticationService authenticationService;

  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(
          @RequestBody AuthRequest request
  ) {
    return ResponseEntity.ok(authenticationService.register(request));
  }

  @PostMapping("/authenticate")
  public ResponseEntity<AuthResponse> authenticate(
          @RequestBody AuthRequest request
  ) {
    return ResponseEntity.ok(authenticationService.authenticate(request));
  }

  @PostMapping("/verify")
  public ResponseEntity<AuthResponse> verifyOtp(@RequestBody Map<String, String> body) {
    String email = body.get("email");
    String otp = body.get("otp");
    return ResponseEntity.ok(authenticationService.verifyOtp(email, otp));
  }
}