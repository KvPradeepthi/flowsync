package com.flowsync.controller;

import com.flowsync.dto.request.LoginRequest;
import com.flowsync.dto.request.RegisterRequest;
import com.flowsync.dto.response.AuthResponse;
import com.flowsync.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication endpoints — public, no JWT required.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/register
     * Register a new customer account.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    /**
     * POST /api/auth/login
     * Authenticate and receive a JWT token.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * POST /api/auth/send-otp
     * Generate and dispatch a 6-digit verification OTP to the user's email.
     */
    @PostMapping("/send-otp")
    public ResponseEntity<java.util.Map<String, Object>> sendOtp(
            @Valid @RequestBody com.flowsync.dto.request.OtpSendRequest request) {
        com.flowsync.service.OtpService.OtpDispatchResult result = authService.sendPasswordResetOtp(request.getEmail());
        if (!result.emailSent()) {
            throw new IllegalStateException("Failed to deliver verification email to " + request.getEmail()
                    + ". Please verify that mail server credentials (MAIL_USERNAME and MAIL_PASSWORD) are configured in the environment.");
        }
        return ResponseEntity.ok(java.util.Map.of(
                "message", result.message(),
                "emailSent", true
        ));
    }

    /**
     * POST /api/auth/reset-password
     * Verify OTP and reset password for an existing account.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<java.util.Map<String, String>> resetPassword(
            @Valid @RequestBody com.flowsync.dto.request.PasswordResetRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(java.util.Map.of("message", "Password has been successfully reset. You can now sign in with your new password."));
    }

    /**
     * POST /api/auth/change-password
     * Change password for currently authenticated user.
     */
    @PostMapping("/change-password")
    public ResponseEntity<java.util.Map<String, String>> changePassword(
            java.security.Principal principal,
            @Valid @RequestBody com.flowsync.dto.request.ChangePasswordRequest request) {
        if (principal == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }
        authService.changePassword(principal.getName(), request);
        return ResponseEntity.ok(java.util.Map.of("message", "Your password has been changed successfully."));
    }
}
