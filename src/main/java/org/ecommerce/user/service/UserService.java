package org.ecommerce.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ecommerce.auth.entities.User;
import org.ecommerce.auth.repository.UserRepository;
import org.ecommerce.common.exception.ResourceAlreadyExistsException;
import org.ecommerce.common.exception.ResourceNotFoundException;
import org.ecommerce.user.dtos.request.UserRequestDto;
import org.ecommerce.user.dtos.response.UserInfoResponseDto;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public UserInfoResponseDto getUserInfo(Authentication authentication) {
        User userPrincipal = (User) authentication.getPrincipal();
        UUID userId = userPrincipal.getId();
        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.warn("User not found for User Info, userId={}", userId);
            return new ResourceNotFoundException("User not found");
        });
        return objectMapper.convertValue(user, UserInfoResponseDto.class);
    }

    public UserInfoResponseDto updateUserInfo(UserRequestDto updateUserInDto, Authentication authentication) {
        UUID userId = ((User) authentication.getPrincipal()).getId();
        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.warn("User not found for update user info, userId={}", userId);
            return new ResourceNotFoundException("User not found");
        });

        if (updateUserInDto.firstName() != null) user.setFirstName(updateUserInDto.firstName().trim());
        if (updateUserInDto.lastName() != null) user.setLastName(updateUserInDto.lastName().trim());
        if (updateUserInDto.phoneNumber() != null) {
            String phoneNumber = updateUserInDto.phoneNumber().trim();
            if (!phoneNumber.equalsIgnoreCase(user.getPhoneNumber()) && userRepository.existsByPhoneNumberIgnoreCaseAndIdNot(phoneNumber, userId)) {
                throw new ResourceAlreadyExistsException("Phone Number is already register");
            }
            user.setPhoneNumber(phoneNumber);
        }

        userRepository.save(user);

        return objectMapper.convertValue(user, UserInfoResponseDto.class);

    }
}
