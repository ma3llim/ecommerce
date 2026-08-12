package org.ecommerce.user.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ecommerce.auth.entities.User;
import org.ecommerce.auth.repository.UserRepository;
import org.ecommerce.common.exception.ResourceNotFoundException;
import org.ecommerce.user.dtos.response.AddressResponseDto;
import org.ecommerce.user.entity.UserAddress;
import org.ecommerce.user.repository.UserAddressRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAddressService {
    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;
    private final ObjectMapper objectMapper;

    public List<AddressResponseDto> getAllUserAddress(Authentication authentication) {
        UUID userId = ((User) authentication.getPrincipal()).getId();

        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.info("User Address request failed: user not found, userId={}", userId);
            return new ResourceNotFoundException("User not found");
        });

        List<UserAddress> userAddresses = userAddressRepository.findAllByUserId(user.getId());

        return objectMapper.convertValue(userAddresses, new TypeReference<List<AddressResponseDto>>() {
        });
    }
}
