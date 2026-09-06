package com.flowsync.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class OtpService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    private static final long OTP_VALIDITY_SECONDS = 600; // 10 minutes
    private final SecureRandom random = new SecureRandom();

    private record OtpEntry(String otp, Instant expiresAt) {}

    // In-memory thread-safe OTP store
    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();

    public record OtpDispatchResult(String otp, boolean emailSent, String message) {}

    /**
     * Generate, store, and send a 6-digit OTP to the recipient email.
     */
    public OtpDispatchResult generateAndSendOtp(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        String otp = String.format("%06d", random.nextInt(1_000_000));
        Instant expiresAt = Instant.now().plusSeconds(OTP_VALIDITY_SECONDS);

        otpStore.put(normalizedEmail, new OtpEntry(otp, expiresAt));
        log.info("[OTP Service] Generated OTP for {}: {}", normalizedEmail, otp);

        // Send email via JavaMailSender if available
        boolean emailSent = false;
        if (mailSender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                if (mailSender instanceof org.springframework.mail.javamail.JavaMailSenderImpl impl
                        && impl.getUsername() != null && !impl.getUsername().isBlank()) {
                    message.setFrom(impl.getUsername());
                } else {
                    message.setFrom("deepthikamichetty336@gmail.com");
                }
                message.setTo(normalizedEmail);
                message.setSubject("FlowSync — Password Reset Verification Code");
                message.setText("Hello,\n\n"
                        + "You requested to reset your password on FlowSync.\n\n"
                        + "Your 6-digit verification code is:\n\n"
                        + "    " + otp + "\n\n"
                        + "This verification code will expire in 10 minutes.\n\n"
                        + "If you did not make this request, please disregard this email.\n\n"
                        + "Best regards,\n"
                        + "FlowSync Security Team");

                mailSender.send(message);
                emailSent = true;
                log.info("[OTP Service] OTP email dispatched successfully via SMTP to {}", normalizedEmail);
            } catch (Exception e) {
                log.warn("[OTP Service] Could not send live email to {}: {}", normalizedEmail, e.getMessage());
            }
        } else {
            log.warn("[OTP Service] JavaMailSender is not initialized (SMTP host not specified).");
        }

        String message = emailSent
                ? "A 6-digit verification code has been dispatched to your email inbox (" + normalizedEmail + "). Please check your inbox or spam folder."
                : "A verification code has been generated.";

        return new OtpDispatchResult(otp, emailSent, message);
    }

    /**
     * Verify if the provided OTP is valid and unexpired for the email.
     */
    public boolean verifyOtp(String email, String providedOtp) {
        if (email == null || providedOtp == null) return false;
        String normalizedEmail = email.trim().toLowerCase();

        OtpEntry entry = otpStore.get(normalizedEmail);
        if (entry == null) {
            log.warn("[OTP Service] No OTP request found for {}", normalizedEmail);
            return false;
        }

        if (Instant.now().isAfter(entry.expiresAt())) {
            otpStore.remove(normalizedEmail);
            log.warn("[OTP Service] OTP expired for {}", normalizedEmail);
            return false;
        }

        boolean matches = entry.otp().equals(providedOtp.trim());
        if (matches) {
            otpStore.remove(normalizedEmail); // Invalidate once verified
            log.info("[OTP Service] OTP successfully verified for {}", normalizedEmail);
        } else {
            log.warn("[OTP Service] OTP mismatch for {}: provided '{}', expected '{}'", normalizedEmail, providedOtp, entry.otp());
        }

        return matches;
    }
}
