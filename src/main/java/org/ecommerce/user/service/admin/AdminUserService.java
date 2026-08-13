package org.ecommerce.user.service.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ecommerce.auth.entities.User;
import org.ecommerce.auth.enums.AccountStatus;
import org.ecommerce.auth.repository.UserRepository;
import org.ecommerce.common.dtos.PageResponse;
import org.ecommerce.user.dtos.admin.AdminUserInfoResponseDto;
import org.ecommerce.user.specification.admin.UserSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminUserService {
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

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
}
