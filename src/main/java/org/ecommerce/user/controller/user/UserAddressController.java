package org.ecommerce.user.controller.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ecommerce.common.response.ApiSuccessResponse;
import org.ecommerce.user.dtos.user.request.AddNewAddressDto;
import org.ecommerce.user.dtos.user.request.UpdateAddressDto;
import org.ecommerce.user.dtos.user.response.AddressResponseDto;
import org.ecommerce.user.service.UserAddressService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/me/addresses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "User Addresses", description = "APIs for managing the authenticated user's addresses")
public class UserAddressController {
    private final UserAddressService userAddressService;

    @Operation(summary = "Get all user addresses", description = "Retrieves all addresses belonging to the authenticated user.")
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

    @Operation(summary = "Add a new address", description = "Adds a new address for the authenticated user.")
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

    @Operation(summary = "Update an address", description = "Updates an existing address belonging to the authenticated user.")
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

    @Operation(summary = "Delete an address", description = "Deletes an existing address belonging to the authenticated user.")
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

    @Operation(summary = "Set default shipping address", description = "Sets the specified address as the default shipping address for the authenticated user.")
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

    @Operation(summary = "Set default billing address", description = "Sets the specified address as the default billing address for the authenticated user.")
    @PatchMapping("/{addressId}/default-billing")
    public ResponseEntity<ApiSuccessResponse<Void>> defaultBilling(
            @PathVariable UUID addressId, Authentication authentication, HttpServletRequest request
    ) {
        userAddressService.updateDefaultBilling(addressId, authentication);

        return ResponseEntity.ok(
                ApiSuccessResponse.<Void>builder()
                        .success(true)
                        .message("Default billing address updated successfully")
                        .data(null)
                        .path(request.getRequestURI())
                        .build()
        );
    }
}
