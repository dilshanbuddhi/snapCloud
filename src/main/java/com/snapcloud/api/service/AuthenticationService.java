package com.snapcloud.api.service;

import com.snapcloud.api.repository.UserRepository;
import com.snapcloud.api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.snapcloud.api.dto.AuthRequest;
import com.snapcloud.api.dto.AuthResponse;
import com.snapcloud.api.domain.User;
import com.snapcloud.api.domain.enums.Role;
import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.snapcloud.api.exception.ResourceAlreadyExistsException;
import com.snapcloud.api.exception.InvalidOtpException;
import com.snapcloud.api.exception.ResourceNotFoundException;
import com.snapcloud.api.exception.UnauthorizedException;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthenticationService {
    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    @Autowired(required = false)
    private JavaMailSender mailSender; // mail sender (optional; configure spring.mail.* to enable)

    private static final String USER_NOT_FOUND_MSG = "User not found with email: %s";
    private static final String INVALID_CREDENTIALS_MSG = "Invalid email or password";
    private static final String EMAIL_EXISTS_MSG = "Email already in use: %s";

    // in-memory OTP store: email -> otp, and expiry map
    private final Map<String, String> otpStorage = new ConcurrentHashMap<>();
    private final Map<String, Instant> otpExpiry = new ConcurrentHashMap<>();
    private static final long OTP_EXPIRATION_SECONDS = 10 * 60; // 10 minutes

    // New: register sends OTP to provided email (creates user with emailVerified=false)
    public AuthResponse register(AuthRequest request) {
        String email = request.getEmail();
        String rawPassword = request.getPassword();

        // If already exists and verified -> 409 Conflict via custom exception
        var existing = userRepository.findByEmail(email);
        if (existing.isPresent() && Boolean.TRUE.equals(existing.get().isEmailVerified())) {
            throw new ResourceAlreadyExistsException(String.format(EMAIL_EXISTS_MSG, email));
        }

        User user;
        if (existing.isPresent()) {
            // update password if provided and keep emailVerified=false
            user = existing.get();
            if (rawPassword != null && !rawPassword.isBlank()) {
                user.setPasswordHash(passwordEncoder.encode(rawPassword));
            }
        } else {
            user = new User();
            user.setEmail(email);
            user.setPasswordHash(passwordEncoder.encode(rawPassword));
            user.setRole(Role.USER);
            user.setEmailVerified(false);
        }
        user = userRepository.save(user);

        String otp = generateOtp();
        otpStorage.put(email, otp);
        otpExpiry.put(email, Instant.now().plusSeconds(OTP_EXPIRATION_SECONDS));

        boolean emailSent = sendOtpEmail(email, otp);

        String message;
        if (emailSent) {
            message = "Verification code sent to email";
        } else {
            message = "Verification code generated but email sending is not configured. Contact support to receive the code.";
        }

        return AuthResponse.builder()
                .accessToken(null)
                .email(email)
                .role(user.getRole())
                .message(message)
                .build();
    }

    public AuthResponse verifyOtp(String email, String otp) {
        if (email == null || otp == null || email.isBlank() || otp.isBlank()) {
            throw new InvalidOtpException("Email and otp are required");
        }
        String expected = otpStorage.get(email);
        Instant expiry = otpExpiry.get(email);
        if (expected == null || expiry == null || Instant.now().isAfter(expiry)) {
            throw new InvalidOtpException("Otp expired or not found");
        }
        if (!expected.equals(otp)) {
            throw new InvalidOtpException("Otp incorrect");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(USER_NOT_FOUND_MSG, email)));
        user.setEmailVerified(true);
        userRepository.save(user);

        otpStorage.remove(email);
        otpExpiry.remove(email);

        // build userDetails and generate token
        var userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .authorities("ROLE_" + (user.getRole() != null ? user.getRole().name() : "USER"))
                .build();

        var jwtToken = jwtService.generateToken(userDetails);
        Instant expiration = jwtService.extractExpiration(jwtToken).toInstant();

        return AuthResponse.success(
                jwtToken,
                user.getEmail(),
                user.getRole(),
                expiration
        );
    }

    // existing authenticate method
    public AuthResponse authenticate(AuthRequest request) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
                )
            );

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UnauthorizedException(INVALID_CREDENTIALS_MSG));

            var userDetails = org.springframework.security.core.userdetails.User
                    .withUsername(user.getEmail())
                    .password(user.getPasswordHash())
                    .authorities("ROLE_" + (user.getRole() != null ? user.getRole().name() : "USER"))
                    .build();

            var jwtToken = jwtService.generateToken(userDetails);
            Instant expiration = jwtService.extractExpiration(jwtToken).toInstant();

            return AuthResponse.success(
                    jwtToken,
                    user.getEmail(),
                    user.getRole(),
                    expiration
            );

        } catch (BadCredentialsException ex) {
            throw new UnauthorizedException(INVALID_CREDENTIALS_MSG);
        } catch (AuthenticationException ex) {
            throw new UnauthorizedException("Authentication failed: " + ex.getMessage());
        }
    }

    private String generateOtp() {
        int number = new Random().nextInt(1_000_000);
        return String.format("%06d", number);
    }

    private boolean sendOtpEmail(String to, String otp) {
        if (mailSender == null) {
            log.warn("Mail sender not configured; OTP for {} is {}", to, otp);
            return false;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Your verification code");
            message.setText("Your verification code is: " + otp + "\nThis code expires in 10 minutes.");
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", to, e.getMessage());
            return false;
        }
    }
}
