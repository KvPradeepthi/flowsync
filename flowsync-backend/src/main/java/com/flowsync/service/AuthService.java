package com.flowsync.service;

import com.flowsync.dto.request.LoginRequest;
import com.flowsync.dto.request.RegisterRequest;
import com.flowsync.dto.response.AuthResponse;
import com.flowsync.entity.User;
import com.flowsync.repository.UserRepository;
import com.flowsync.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication service — handles registration and login.
 *
 * Interview note:
 *   AuthenticationManager.authenticate() internally calls
 *   UserDetailsService.loadUserByUsername() and PasswordEncoder.matches().
 *   If either check fails it throws BadCredentialsException.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final AuditLogService auditLogService;
    private final OtpService otpService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.CUSTOMER)
                .build();

        userRepository.save(user);
        log.info("Registered new user: {} ({})", user.getName(), user.getEmail());

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        // Throws BadCredentialsException if email/password don't match
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        log.info("User logged in: {}", user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .build();
    }

    /**
     * Generate and dispatch a secure 6-digit OTP to the registered account.
     */
    public OtpService.OtpDispatchResult sendPasswordResetOtp(String email) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new IllegalStateException("No account found registered with email: " + email));

        OtpService.OtpDispatchResult result = otpService.generateAndSendOtp(user.getEmail());
        auditLogService.log(
                user.getId(),
                user.getEmail(),
                "OTP_REQUESTED",
                "User",
                user.getId(),
                null,
                null,
                "Password reset OTP was generated. Live email sent: " + result.emailSent()
        );
        return result;
    }

    /**
     * Verify OTP and atomically reset password.
     */
    @Transactional
    public void resetPassword(com.flowsync.dto.request.PasswordResetRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalStateException("No account registered with email: " + request.getEmail()));

        boolean otpValid = otpService.verifyOtp(normalizedEmail, request.getOtp());
        if (!otpValid) {
            throw new IllegalStateException("Invalid or expired verification OTP. Please check the code or request a new one.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        auditLogService.log(
                user.getId(),
                user.getEmail(),
                "PASSWORD_RESET",
                "User",
                user.getId(),
                "PROTECTED",
                "PROTECTED",
                "Password was successfully reset via OTP verification"
        );
        log.info("Password successfully reset via OTP for user: {}", user.getEmail());
    }

    /**
     * Change password for authenticated users.
     */
    @Transactional
    public void changePassword(String userEmail, com.flowsync.dto.request.ChangePasswordRequest request) {
        User user = userRepository.findByEmail(userEmail.trim().toLowerCase())
                .orElseThrow(() -> new IllegalStateException("User account not found: " + userEmail));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalStateException("Current password does not match our records.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        auditLogService.log(
                user.getId(),
                user.getEmail(),
                "PASSWORD_CHANGED",
                "User",
                user.getId(),
                "PROTECTED",
                "PROTECTED",
                "User changed their password while authenticated"
        );
        log.info("User {} changed their password successfully.", userEmail);
    }
}
