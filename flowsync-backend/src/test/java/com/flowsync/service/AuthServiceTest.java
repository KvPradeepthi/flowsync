package com.flowsync.service;

import com.flowsync.dto.request.PasswordResetRequest;
import com.flowsync.entity.User;
import com.flowsync.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditLogService auditLogService;

    @Spy
    private OtpService otpService = new OtpService();

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Jane Doe")
                .email("jane@example.com")
                .password("encoded_old_password")
                .role(User.Role.CUSTOMER)
                .build();
    }

    @Test
    @DisplayName("OtpService generates valid 6-digit OTP and verifies it correctly")
    void testOtpGenerationAndVerification() {
        String email = "test@example.com";
        String otp = otpService.generateAndSendOtp(email).otp();

        assertNotNull(otp);
        assertEquals(6, otp.length());
        assertTrue(otp.chars().allMatch(Character::isDigit));

        // Valid OTP verifies to true
        assertTrue(otpService.verifyOtp(email, otp));

        // Single-use: Second attempt should fail
        assertFalse(otpService.verifyOtp(email, otp));
    }

    @Test
    @DisplayName("OtpService rejects incorrect OTP")
    void testInvalidOtpRejection() {
        String email = "test@example.com";
        otpService.generateAndSendOtp(email);

        assertFalse(otpService.verifyOtp(email, "000000"));
        assertFalse(otpService.verifyOtp("other@example.com", "123456"));
    }

    @Test
    @DisplayName("sendPasswordResetOtp throws IllegalStateException if user does not exist")
    void testSendOtpUserNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () ->
                authService.sendPasswordResetOtp("unknown@example.com")
        );
    }

    @Test
    @DisplayName("sendPasswordResetOtp succeeds for registered user")
    void testSendOtpSuccess() {
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(testUser));

        OtpService.OtpDispatchResult result = authService.sendPasswordResetOtp("jane@example.com");

        assertNotNull(result);
        assertNotNull(result.otp());
        assertEquals(6, result.otp().length());
        verify(auditLogService, atLeastOnce()).log(any(), any(), eq("OTP_REQUESTED"), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("resetPassword throws IllegalStateException for invalid OTP")
    void testResetPasswordInvalidOtp() {
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(testUser));

        PasswordResetRequest request = new PasswordResetRequest();
        request.setEmail("jane@example.com");
        request.setOtp("999999");
        request.setNewPassword("newSecret123");

        assertThrows(IllegalStateException.class, () ->
                authService.resetPassword(request)
        );
    }

    @Test
    @DisplayName("resetPassword successfully updates password when OTP is valid")
    void testResetPasswordSuccess() {
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newSecret123")).thenReturn("encoded_new_secret");

        String otp = otpService.generateAndSendOtp("jane@example.com").otp();

        PasswordResetRequest request = new PasswordResetRequest();
        request.setEmail("jane@example.com");
        request.setOtp(otp);
        request.setNewPassword("newSecret123");

        authService.resetPassword(request);

        assertEquals("encoded_new_secret", testUser.getPassword());
        verify(userRepository, times(1)).save(testUser);
        verify(auditLogService, atLeastOnce()).log(any(), any(), eq("PASSWORD_RESET"), any(), any(), any(), any(), any());
    }
}