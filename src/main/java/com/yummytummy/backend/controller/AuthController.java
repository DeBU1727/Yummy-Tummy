package com.yummytummy.backend.controller;

import com.yummytummy.backend.dto.AuthResponseDto;
import com.yummytummy.backend.dto.LoginRequestDto;
import com.yummytummy.backend.dto.RegistrationRequestDto;
import com.yummytummy.backend.entity.User;
import com.yummytummy.backend.repository.UserRepository;
import com.yummytummy.backend.service.JwtUtil;
import com.yummytummy.backend.service.OtpService;
import com.yummytummy.backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final OtpService otpService;

    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository, 
                          PasswordEncoder passwordEncoder, JwtUtil jwtUtil, 
                          UserService userService, OtpService otpService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.otpService = otpService;
    }

    @PostMapping("/otp/send")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        try {
            otpService.sendOtp(email);
            return ResponseEntity.ok("OTP sent to your email.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send OTP.");
        }
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");
        if (otpService.verifyOtp(email, code)) {
            return ResponseEntity.ok("OTP verified successfully.");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired OTP.");
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.ok("Email exists.");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with this email.");
    }



    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String newPassword = request.get("password");
        
        if (!otpService.isOtpVerified(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("OTP not verified.");
        }

        try {
            userService.resetPassword(email, newPassword);
            otpService.clearOtp(email);
            return ResponseEntity.ok("Password reset successful");
        } catch (RuntimeException e) {
            if (e.getMessage().equals("User not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with this email.");
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to reset password");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to reset password");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegistrationRequestDto registrationRequest) {
        if (!otpService.isOtpVerified(registrationRequest.email())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("OTP not verified.");
        }

        if (userRepository.findByEmail(registrationRequest.email()).isPresent()) {
            return new ResponseEntity<>("Email address is already in use.", HttpStatus.BAD_REQUEST);
        }

        User user = new User();
        user.setFullName(registrationRequest.fullName());
        user.setEmail(registrationRequest.email());
        user.setPassword(passwordEncoder.encode(registrationRequest.password()));
        user.setRoles("ROLE_USER");

        userRepository.save(user);
        otpService.clearOtp(registrationRequest.email());

        final String token = jwtUtil.generateToken(user);
        return ResponseEntity.ok(new AuthResponseDto(token));
    }

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody LoginRequestDto loginRequest) throws Exception {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }

        // Check if OTP is verified (if 2FA is required for all)
        if (!otpService.isOtpVerified(loginRequest.email())) {
            // Credentials are correct, but need OTP
            try {
                otpService.sendOtp(loginRequest.email());
                return ResponseEntity.status(HttpStatus.ACCEPTED).body("OTP_REQUIRED");
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Login successful but failed to send OTP: " + e.getMessage());
            }
        }

        final UserDetails userDetails = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new Exception("User not found"));

        final String token = jwtUtil.generateToken(userDetails);
        otpService.clearOtp(loginRequest.email());

        return ResponseEntity.ok(new AuthResponseDto(token));
    }
}
