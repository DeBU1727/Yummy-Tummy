package com.yummytummy.backend.service;

import com.yummytummy.backend.entity.Otp;
import com.yummytummy.backend.entity.User;
import com.yummytummy.backend.repository.OtpRepository;
import com.yummytummy.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Optional;
import java.util.Random;

@Service
public class OtpService {

    private final OtpRepository otpRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    // OTP expiration time in minutes
    private final int OTP_EXPIRATION_MINUTES = 5;
    // Max resend attempts
    private final int MAX_RESEND_ATTEMPTS = 3;

    @Autowired
    public OtpService(OtpRepository otpRepository, UserRepository userRepository, EmailService emailService) {
        this.otpRepository = otpRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    // Generate a 6-digit OTP
    private String generateOtpCode() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // Generates a number between 100000 and 999999
        return String.valueOf(otp);
    }

    @Transactional
    public void sendOtp(String email) { 
        // Invalidate any previous unverified OTPs for this email that are still active
        // Find the latest OTP for the email
        otpRepository.findTopByEmailOrderByExpirationTimeDesc(email).ifPresent(otp -> {
            if (!otp.isVerified() && !otp.isExpired()) {
                otp.setExpirationTime(LocalDateTime.now().minusMinutes(1)); // Expire immediately
                otpRepository.save(otp);
            }
        });

        // Check resend attempts for the latest OTP
        Optional<Otp> latestOtpOpt = otpRepository.findTopByEmailOrderByExpirationTimeDesc(email);
        if (latestOtpOpt.isPresent()) {
            Otp latestOtp = latestOtpOpt.get();
            if (latestOtp.getResendCount() >= MAX_RESEND_ATTEMPTS && !latestOtp.isExpired()) {
                throw new RuntimeException("Maximum OTP resend attempts reached. Please try again later.");
            }
        }

        String otpCode = generateOtpCode();
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES);
        
        Otp newOtp = new Otp(email, otpCode, expirationTime);
        latestOtpOpt.ifPresent(latestOtp -> {
            newOtp.setResendCount(latestOtp.getResendCount() + 1);
        });
        otpRepository.save(newOtp);

        // Send OTP via email
        emailService.sendOtpEmail(email, otpCode);
        System.out.println("OTP sent to " + email + ": " + otpCode);
    }

    @Transactional
    public boolean verifyOtp(String email, String otpCode) { // Changed return type to boolean for simplicity
        Optional<Otp> otpOptional = otpRepository.findTopByEmailOrderByExpirationTimeDesc(email)
                                             .filter(o -> o.getCode().equals(otpCode));

        if (otpOptional.isEmpty()) {
            return false; // Invalid OTP or Email
        }

        Otp otp = otpOptional.get();

        if (otp.isExpired()) {
            return false; // OTP has expired
        }
        if (otp.isVerified()) {
            return false; // OTP has already been used
        }

        otp.setVerified(true);
        otpRepository.save(otp);
        return true;
    }

    // New method: Check if the latest OTP for an email is verified and not expired
    @Transactional(readOnly = true)
    public boolean isOtpVerified(String email) {
        return otpRepository.findTopByEmailOrderByExpirationTimeDesc(email)
                .filter(Otp::isVerified)
                .filter(otp -> !otp.isExpired())
                .isPresent();
    }

    // New method: Clear/invalidate the latest OTP for an email
    @Transactional
    public void clearOtp(String email) {
        otpRepository.findTopByEmailOrderByExpirationTimeDesc(email).ifPresent(otp -> {
            otp.setExpirationTime(LocalDateTime.now().minusMinutes(1)); // Expire immediately
            otp.setVerified(false); // Mark as not verified if we clear it
            otpRepository.save(otp);
        });
    }
}
