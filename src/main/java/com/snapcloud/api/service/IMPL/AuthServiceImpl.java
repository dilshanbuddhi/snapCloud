package com.snapcloud.api.service.IMPL;

import com.snapcloud.api.domain.User;
import com.snapcloud.api.domain.enums.Role;
import com.snapcloud.api.dto.AuthRequest;
import com.snapcloud.api.dto.AuthResponse;
import com.snapcloud.api.exception.custom.*;
import com.snapcloud.api.repository.UserRepository;
import com.snapcloud.api.security.JwtService;
import com.snapcloud.api.service.AuthService;
import com.snapcloud.api.util.OtpUtils;
import com.snapcloud.api.util.SendMailUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final SendMailUtil sendMailUtil;

    private final Map<String, String> otpStore = new ConcurrentHashMap<>();
    private final Map<String, Instant> otpExpiry = new ConcurrentHashMap<>();
    private static final long OTP_EXP = 600;

    @Override
    public AuthResponse register(AuthRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseGet(() -> {
                    User u = new User();
                    u.setEmail(request.getEmail());
                    u.setRole(Role.USER);
                    u.setEmailVerified(false);
                    return u;
                });

        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        String otp = OtpUtils.generateOtp();
        otpStore.put(user.getEmail(), otp);
        otpExpiry.put(user.getEmail(), Instant.now().plusSeconds(OTP_EXP));

        sendMailUtil.sendEmail(
                user.getEmail(),
                "SnapCloud Verification Code",
                "Your OTP is: " + otp + "\nValid for 10 minutes."
        );

        return AuthResponse.builder()
                .email(user.getEmail())
                .message("OTP sent to email")
                .build();
    }

    @Override
    public AuthResponse verifyOtp(String email, String otp) {

        if (!otp.equals(otpStore.get(email)) ||
                Instant.now().isAfter(otpExpiry.get(email))) {
            throw new InvalidOtpException("Invalid or expired OTP");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setEmailVerified(true);
        userRepository.save(user);

        var userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .authorities("ROLE_" + user.getRole().name())
                .build();

        String token = jwtService.generateToken(userDetails);

        return AuthResponse.success(
                token,
                user.getEmail(),
                user.getRole(),
                jwtService.extractExpiration(token).toInstant()
        );
    }

    @Override
    public AuthResponse authenticate(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        var userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .authorities("ROLE_" + user.getRole().name())
                .build();

        String token = jwtService.generateToken(userDetails);

        return AuthResponse.success(
                token,
                user.getEmail(),
                user.getRole(),
                jwtService.extractExpiration(token).toInstant()
        );
    }
}
