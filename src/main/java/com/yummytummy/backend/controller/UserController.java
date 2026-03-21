package com.yummytummy.backend.controller;

import com.yummytummy.backend.dto.PasswordUpdateDto;
import com.yummytummy.backend.dto.UserProfileDto;
import com.yummytummy.backend.dto.UserUpdateDto;
import com.yummytummy.backend.service.UserService;
import com.yummytummy.backend.service.OtpService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final OtpService otpService;

    public UserController(UserService userService, OtpService otpService) {
        this.userService = userService;
        this.otpService = otpService;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDto> getUserProfile(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(userService.getUserProfile(email));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileDto> updateUserProfile(Authentication authentication, @RequestBody UserUpdateDto userUpdateDto) {
        String email = authentication.getName();
        return ResponseEntity.ok(userService.updateUserProfile(email, userUpdateDto));
    }

    @PutMapping("/password")
    public ResponseEntity<?> updateUserPassword(Authentication authentication, @RequestBody PasswordUpdateDto passwordUpdateDto) {
        String email = authentication.getName();
        
        // 1. First, verify if the current password is correct
        try {
            userService.verifyCurrentPassword(email, passwordUpdateDto.currentPassword());
        } catch (RuntimeException e) {
            // Stop immediately if password is wrong
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }

        // 2. If correct, check for OTP verification
        if (!otpService.isOtpVerified(email)) {
            otpService.sendOtp(email);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body("OTP_REQUIRED");
        }

        // 3. Finalize password update
        try {
            userService.updateUserPassword(email, passwordUpdateDto);
            otpService.clearOtp(email);
            return ResponseEntity.ok("Password updated successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/profile-picture")
    public ResponseEntity<UserProfileDto> updateProfilePicture(Authentication authentication, @RequestParam("file") MultipartFile file) {
        String email = authentication.getName();
        return ResponseEntity.ok(userService.updateUserProfilePicture(email, file));
    }
}
