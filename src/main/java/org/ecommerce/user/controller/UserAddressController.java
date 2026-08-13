package org.ecommerce.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ecommerce.common.response.ApiSuccessResponse;
import org.ecommerce.user.dtos.request.AddNewAddressDto;
import org.ecommerce.user.dtos.request.UpdateAddressDto;
import org.ecommerce.user.dtos.response.AddressResponseDto;
import org.ecommerce.user.service.UserAddressService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/me/addresses")
@RequiredArgsConstructor
public class UserAddressController {
    private final UserAddressService userAddressService;

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

    @PostMapping
    public ResponseEntity<ApiSuccessResponse<AddressResponseDto>> addNewAddress(
            @Valid @RequestBody AddNewAddressDto addNewAddress,
            Authentication authentication, HttpServletRequest request) {
        AddressResponseDto addressResponseDto = userAddressService.addNewAddress(addNewAddress, authentication);
        return ResponseEntity.ok(
                ApiSuccessResponse.<AddressResponseDto>builder()
                        .success(true)
                        .message("Address added successfully")
                        .data(addressResponseDto)
                        .path(request.getRequestURI())
                        .build()
        );
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<ApiSuccessResponse<AddressResponseDto>> updateAddress(
            @PathVariable UUID addressId, Authentication authentication,
            @Valid @RequestBody UpdateAddressDto updateAddressDto, HttpServletRequest request
    ) {
        AddressResponseDto addressResponseDto = userAddressService.updateAddress(addressId, authentication, updateAddressDto);

        return ResponseEntity.ok(
                ApiSuccessResponse.<AddressResponseDto>builder()
                        .success(true)
                        .message("Address updated successfully")
                        .data(addressResponseDto)
                        .path(request.getRequestURI())
                        .build()
        );
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<ApiSuccessResponse<Void>> deleteAddress(
            @PathVariable UUID addressId, Authentication authentication,
            HttpServletRequest request
    ) {
        String message = userAddressService.deleteAddress(addressId, authentication);

        return ResponseEntity.ok(
                ApiSuccessResponse.<Void>builder()
                        .success(true)
                        .message(message)
                        .data(null)
                        .path(request.getRequestURI())
                        .build()
        );
    }

    @PatchMapping("/{addressId}/default-shipping")
    public ResponseEntity<ApiSuccessResponse<Void>> defaultShipping(
            @PathVariable UUID addressId, Authentication authentication, HttpServletRequest request
    ) {
        userAddressService.updateDefaultShipping(addressId, authentication);

        return ResponseEntity.ok(
                ApiSuccessResponse.<Void>builder()
                        .success(true)
                        .message("Default shipping address updated successfully")
                        .data(null)
                        .path(request.getRequestURI())
                        .build()
        );
    }

    @PatchMapping("/{addressId}/default-billing")
    public ResponseEntity<ApiSuccessResponse<Void>> defaultBilling(
            @PathVariable UUID addressId, Authentication authentication, HttpServletRequest request
    ) {
        userAddressService.updateDefaultBilling(addressId, authentication);

        return ResponseEntity.ok(
                ApiSuccessResponse.<Void>builder()
                        .success(true)
                        .message("Default Billing address updated successfully")
                        .data(null)
                        .path(request.getRequestURI())
                        .build()
        );
    }
}
