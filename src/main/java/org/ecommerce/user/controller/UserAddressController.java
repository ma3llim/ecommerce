package org.ecommerce.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.ecommerce.common.response.ApiSuccessResponse;
import org.ecommerce.user.dtos.response.AddressResponseDto;
import org.ecommerce.user.repository.UserAddressRepository;
import org.ecommerce.user.service.UserAddressService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/me/addresses")
@RequiredArgsConstructor
public class UserAddressController {
    private final UserAddressService userAddressService;
    private final UserAddressRepository userAddressRepository;

    @GetMapping
    public ResponseEntity<ApiSuccessResponse<List<AddressResponseDto>>> getAllUserAddresses(
            Authentication authentication, HttpServletRequest request) {

        List<AddressResponseDto> addressResponseDtoLists = userAddressService.getAllUserAddress(authentication);

        return ResponseEntity.ok(
                ApiSuccessResponse.<List<AddressResponseDto>>builder()
                        .success(true)
                        .message("User addresses retrieved successfully")
                        .data(addressResponseDtoLists)
                        .path(request.getRequestURI())
                        .build()
        );
    }
}
