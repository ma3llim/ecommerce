package org.ecommerce.user.service.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ecommerce.auth.entities.User;
import org.ecommerce.auth.enums.AccountStatus;
import org.ecommerce.auth.enums.Role;
import org.ecommerce.auth.repository.RefreshTokenRepository;
import org.ecommerce.auth.repository.UserRepository;
import org.ecommerce.common.dtos.PageResponse;
import org.ecommerce.common.exception.BadRequestException;
import org.ecommerce.common.exception.ResourceNotFoundException;
import org.ecommerce.user.dtos.admin.request.adminStatusRequestDto;
import org.ecommerce.user.dtos.admin.response.AdminUserDetailsResponseDto;
import org.ecommerce.user.dtos.admin.response.AdminUserInfoResponseDto;
import org.ecommerce.user.dtos.user.response.AddressResponseDto;
import org.ecommerce.user.entity.UserAddress;
import org.ecommerce.user.repository.UserAddressRepository;
import org.ecommerce.user.specification.admin.UserSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminUserService {
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final UserAddressRepository userAddressRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public PageResponse<AdminUserInfoResponseDto> getAllUsers(String search, AccountStatus accountStatus, Pageable pageable) {
        Specification<User> specification = UserSpecification.search(search).and(UserSpecification.hasStatus(accountStatus));

        Page<User> users = userRepository.findAll(specification, pageable);

        Page<AdminUserInfoResponseDto> userInfoResponse = users.map(
                user -> objectMapper.convertValue(user, AdminUserInfoResponseDto.class));


        return new PageResponse<>(
                userInfoResponse.getContent(),
                userInfoResponse.getNumber(),
                userInfoResponse.getSize(),
                userInfoResponse.getTotalElements(),
                userInfoResponse.getTotalPages(),
                userInfoResponse.isFirst(),
                userInfoResponse.isLast()
        );
    }

    public AdminUserDetailsResponseDto getUserDetails(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.warn("User details request failed: user not found, userId={}", userId);
            return new ResourceNotFoundException("User not found");
        });

        List<UserAddress> userAddressesEntities = userAddressRepository.findAllByUserId(userId);

        List<AddressResponseDto> userAddress = userAddressesEntities.stream()
                .map(address -> objectMapper.convertValue(address, AddressResponseDto.class))
                .toList();

        return AdminUserDetailsResponseDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .emailVerified(user.isEmailVerified())
                .profileImageUrl(user.getProfileImageUrl())
                .accountStatus(user.getAccountStatus())
                .role(user.getRole())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .addresses(userAddress)
                .build();
    }

    public AccountStatus updateAccountStatus(UUID userId, adminStatusRequestDto statusRequestDto) {
        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.warn("Account status update failed: user not found, userId={}", userId);
            return new ResourceNotFoundException("User not found");
        });

        if (user.getAccountStatus() == statusRequestDto.accountStatus()) {
            return statusRequestDto.accountStatus();
        }

        if (user.getRole() == Role.ADMIN) {
            log.warn("Account status update rejected for admin user, userId={}", userId);
            throw new BadRequestException("Admin account status cannot be changed");
        }
        user.setAccountStatus(statusRequestDto.accountStatus());
        userRepository.save(user);

        if (statusRequestDto.accountStatus() == AccountStatus.LOCKED || statusRequestDto.accountStatus() == AccountStatus.DISABLED) {
            refreshTokenRepository.findAllByUserId(user.getId()).forEach(
                    refreshToken -> refreshToken.setRevoked(true)
            );
        }
        return statusRequestDto.accountStatus();
    }
}
