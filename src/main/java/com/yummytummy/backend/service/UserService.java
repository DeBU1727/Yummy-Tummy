package com.yummytummy.backend.service;

import com.yummytummy.backend.dto.PasswordUpdateDto;
import com.yummytummy.backend.dto.UserProfileDto;
import com.yummytummy.backend.dto.UserUpdateDto;
import com.yummytummy.backend.entity.User;
import com.yummytummy.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, FileStorageService fileStorageService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileStorageService = fileStorageService;
    }

    public UserProfileDto getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return new UserProfileDto(user.getId(), user.getFullName(), user.getEmail(), user.getProfilePicturePath());
    }

    public UserProfileDto updateUserProfile(String email, UserUpdateDto userUpdateDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setFullName(userUpdateDto.fullName());
        User updatedUser = userRepository.save(user);
        return new UserProfileDto(updatedUser.getId(), updatedUser.getFullName(), updatedUser.getEmail(), updatedUser.getProfilePicturePath());
    }

    public void verifyCurrentPassword(String email, String currentPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Invalid current password");
        }
    }

    public void updateUserPassword(String email, PasswordUpdateDto passwordUpdateDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(passwordUpdateDto.currentPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid current password");
        }

        user.setPassword(passwordEncoder.encode(passwordUpdateDto.newPassword()));
        userRepository.save(user);
    }

    public void resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public UserProfileDto updateUserProfilePicture(String email, MultipartFile file) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (file.isEmpty()) {
            throw new RuntimeException("Failed to store empty file.");
        }

        String imageUrl = fileStorageService.storeFile(file);
        user.setProfilePicturePath(imageUrl);
        User updatedUser = userRepository.save(user);
        return new UserProfileDto(updatedUser.getId(), updatedUser.getFullName(), updatedUser.getEmail(), updatedUser.getProfilePicturePath());
    }
}
