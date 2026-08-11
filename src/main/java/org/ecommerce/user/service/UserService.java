package org.ecommerce.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ecommerce.auth.entities.User;
import org.ecommerce.auth.repository.UserRepository;
import org.ecommerce.common.exception.ResourceNotFoundException;
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
}
