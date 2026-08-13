package org.ecommerce.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ecommerce.auth.entities.User;
import org.ecommerce.auth.repository.RefreshTokenRepository;
import org.ecommerce.auth.repository.UserRepository;
import org.ecommerce.auth.utils.PasswordUtils;
import org.ecommerce.common.dtos.CloudinaryUploadResult;
import org.ecommerce.common.enums.CloudinaryFolder;
import org.ecommerce.common.exception.BadRequestException;
import org.ecommerce.common.exception.ResourceAlreadyExistsException;
import org.ecommerce.common.exception.ResourceNotFoundException;
import org.ecommerce.common.service.CloudinaryService;
import org.ecommerce.user.dtos.user.request.PasswordRequestDto;
import org.ecommerce.user.dtos.user.request.UserRequestDto;
import org.ecommerce.user.dtos.user.response.UserInfoResponseDto;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final PasswordUtils passwordUtils;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CloudinaryService cloudinaryService;

    public UserInfoResponseDto getUserInfo(Authentication authentication) {
        UUID userId = ((User) authentication.getPrincipal()).getId();

        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.info("User information request failed: user not found, userId={}", userId);
            return new ResourceNotFoundException("User not found");
        });

        log.info("User information retrieved successfully: userId={}", userId);

        return objectMapper.convertValue(user, UserInfoResponseDto.class);
    }

    public UserInfoResponseDto updateUserInfo(UserRequestDto updateUserInDto, Authentication authentication) {
        UUID userId = ((User) authentication.getPrincipal()).getId();

        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.warn("User information update failed: user not found, userId={}", userId);
            return new ResourceNotFoundException("User not found");
        });

        if (updateUserInDto.firstName() != null) user.setFirstName(updateUserInDto.firstName().trim());
        if (updateUserInDto.lastName() != null) user.setLastName(updateUserInDto.lastName().trim());
        if (updateUserInDto.phoneNumber() != null) {
            String phoneNumber = updateUserInDto.phoneNumber().trim();

            if (!phoneNumber.equalsIgnoreCase(user.getPhoneNumber())
                    && userRepository.existsByPhoneNumberIgnoreCaseAndIdNot(phoneNumber, userId)) {
                log.warn("User information update rejected: phone number already registered, userId={}", userId);
                throw new ResourceAlreadyExistsException("Phone number is already registered");
            }
            user.setPhoneNumber(phoneNumber);
        }

        userRepository.save(user);

        log.info("User information updated successfully: userId={}", userId);

        return objectMapper.convertValue(user, UserInfoResponseDto.class);
    }


    public void updatePassword(PasswordRequestDto passwordRequestDto, Authentication authentication) {
        UUID userId = ((User) authentication.getPrincipal()).getId();
        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.warn("Password update failed: user not found, userId={}", userId);
            return new ResourceNotFoundException("User not found");
        });

        if (!passwordUtils.passwordMatches(passwordRequestDto.currentPassword(), user.getPassword())) {
            log.warn("Password update rejected: current password is incorrect, userId={}", userId);
            throw new BadRequestException("Current password is incorrect");
        }

        String hashPassword = passwordUtils.encode(passwordRequestDto.password());

        user.setPassword(hashPassword);
        user.setPasswordChangedAt(Instant.now());

        userRepository.save(user);

        // Deactivate tokens
        refreshTokenRepository.findAllByUserId(userId).forEach(token -> {
            token.setRevoked(true);
        });

        log.info("Password updated successfully and refresh tokens revoked: userId={}", userId);
    }

    public UserInfoResponseDto updateProfileImage(MultipartFile profileImage, Authentication authentication) {
        UUID userId = ((User) authentication.getPrincipal()).getId();
        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.warn("Profile image update failed: user not found, userId={}", userId);
            return new ResourceNotFoundException("User not found");
        });
        String oldProfilePublicId = user.getProfileImagePublicId();
        CloudinaryUploadResult cloudinaryUploadResult = cloudinaryService.uploadImage(profileImage, CloudinaryFolder.PROFILE_IMAGES);

        user.setProfileImageUrl(cloudinaryUploadResult.secureUrl());
        user.setProfileImagePublicId(cloudinaryUploadResult.publicId());

        userRepository.save(user);
        log.info("Profile image information updated successfully: userId={}", userId);

        if (oldProfilePublicId != null && !oldProfilePublicId.isBlank()) {
            boolean removed = cloudinaryService.removeImage(oldProfilePublicId);
            if (removed) {
                log.info("Old profile image removed successfully: userId={}, publicId={}", userId, oldProfilePublicId);
            } else {
                log.warn("Failed to remove old profile image: userId={}, publicId={}", userId, oldProfilePublicId);
            }
        }
        log.info("Profile image update completed successfully: userId={}", userId);
        return objectMapper.convertValue(user, UserInfoResponseDto.class);
    }
}