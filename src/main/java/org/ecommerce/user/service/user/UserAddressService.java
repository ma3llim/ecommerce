package org.ecommerce.user.service.user;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ecommerce.auth.entities.User;
import org.ecommerce.auth.repository.UserRepository;
import org.ecommerce.common.exception.BadRequestException;
import org.ecommerce.common.exception.ResourceNotFoundException;
import org.ecommerce.user.dtos.user.request.AddNewAddressDto;
import org.ecommerce.user.dtos.user.request.UpdateAddressDto;
import org.ecommerce.user.dtos.user.response.AddressResponseDto;
import org.ecommerce.user.entity.UserAddress;
import org.ecommerce.user.enums.AddressType;
import org.ecommerce.user.repository.UserAddressRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            log.warn("Get all addresses failed: user not found, userId={}", userId);
            return new ResourceNotFoundException("User not found");
        });

        List<UserAddress> userAddresses = userAddressRepository.findAllByUserId(user.getId());

        return objectMapper.convertValue(userAddresses, new TypeReference<List<AddressResponseDto>>() {
        });
    }

    @Transactional
    public AddressResponseDto addNewAddress(AddNewAddressDto addNewAddress, Authentication authentication) {
        UUID userId = ((User) authentication.getPrincipal()).getId();
        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.warn("Add address request failed: user not found, userId={}", userId);
            return new ResourceNotFoundException("User not found");
        });

        boolean isFirstAddress = !userAddressRepository.existsByUserId(user.getId());

        if (addNewAddress.addressType() != AddressType.OTHER && userAddressRepository.existsByUserIdAndAddressType(user.getId(), addNewAddress.addressType())) {
            log.warn("Add address request rejected: address type already exists, userId={}, addressType={}", userId, addNewAddress.addressType());
            throw new BadRequestException("An address of type " + addNewAddress.addressType() + " already exists");
        }

        boolean defaultShipping;
        boolean defaultBilling;

        if (isFirstAddress) {
            defaultShipping = true;
            defaultBilling = true;
        } else {
            defaultShipping = addNewAddress.defaultShipping();
            defaultBilling = addNewAddress.defaultBilling();

            if (defaultShipping) {
                userAddressRepository.removeDefaultShipping(userId);
            }

            if (defaultBilling) {
                userAddressRepository.removeDefaultBilling(userId);
            }
        }

        UserAddress newUserAddress = UserAddress.builder()
                .userId(userId)
                .fullName(addNewAddress.fullName())
                .phoneNumber(addNewAddress.phoneNumber())
                .addressLineOne(addNewAddress.addressLineOne())
                .addressLineTwo(addNewAddress.addressLineTwo())
                .city(addNewAddress.city())
                .state(addNewAddress.state())
                .country(addNewAddress.country())
                .postalCode(addNewAddress.postalCode())
                .addressType(addNewAddress.addressType())
                .defaultShipping(defaultShipping)
                .defaultBilling(defaultBilling)
                .build();

        UserAddress savedNewAddress = userAddressRepository.save(newUserAddress);
        log.info("Address created successfully, userId={}, addressId={}", userId, savedNewAddress.getId());
        return objectMapper.convertValue(savedNewAddress, AddressResponseDto.class);
    }

    @Transactional
    public AddressResponseDto updateAddress(UUID addressId, Authentication authentication, UpdateAddressDto updateAddressDto) {
        UUID userId = ((User) authentication.getPrincipal()).getId();
        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.warn("Update address request failed: user not found, userId={}, addressId={}", userId, addressId);
            return new ResourceNotFoundException("User not found");
        });

        UserAddress existingAddress = userAddressRepository.findById(addressId).orElseThrow(() -> {
            log.warn("Update address request failed: address not found, userId={}, addressId={}", userId, addressId);
            return new ResourceNotFoundException("Address not found");
        });

        if (!existingAddress.getUserId().equals(user.getId())) {
            log.warn("Update address request rejected: address does not belong to user, userId={}, addressId={}", userId, addressId);
            throw new ResourceNotFoundException("Address not found");
        }


        if (updateAddressDto.fullName() != null) existingAddress.setFullName(updateAddressDto.fullName().trim());
        if (updateAddressDto.phoneNumber() != null)
            existingAddress.setPhoneNumber(updateAddressDto.phoneNumber().trim());
        if (updateAddressDto.addressLineOne() != null)
            existingAddress.setAddressLineOne(updateAddressDto.addressLineOne().trim());
        if (updateAddressDto.addressLineTwo() != null)
            existingAddress.setAddressLineTwo(updateAddressDto.addressLineTwo().trim());

        if (updateAddressDto.city() != null) existingAddress.setCity(updateAddressDto.city().trim());
        if (updateAddressDto.state() != null) existingAddress.setState(updateAddressDto.state().trim());
        if (updateAddressDto.country() != null) existingAddress.setCountry(updateAddressDto.country().trim());
        if (updateAddressDto.postalCode() != null) existingAddress.setPostalCode(updateAddressDto.postalCode().trim());
        if (updateAddressDto.addressType() != null && updateAddressDto.addressType() != existingAddress.getAddressType()) {
            if (updateAddressDto.addressType() != AddressType.OTHER && userAddressRepository.existsByUserIdAndAddressType(user.getId(), updateAddressDto.addressType())) {
                log.warn("Update address request rejected: address type already exists, userId={}, addressId={}, addressType={}", userId, addressId, updateAddressDto.addressType());

                throw new BadRequestException("An address of type " + updateAddressDto.addressType() + " already exists");
            }
            existingAddress.setAddressType(updateAddressDto.addressType());
        }

        log.info("Address updated successfully, userId={}, addressId={}", userId, addressId);

        return objectMapper.convertValue(existingAddress, AddressResponseDto.class);
    }

    @Transactional
    public String deleteAddress(UUID addressId, Authentication authentication) {
        UUID userId = ((User) authentication.getPrincipal()).getId();

        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.warn("Delete address request failed: user not found, userId={}, addressId={}", userId, addressId);
            return new ResourceNotFoundException("User not found");
        });

        UserAddress existingAddress = userAddressRepository.findById(addressId).orElseThrow(() -> {
            log.warn("Delete address request failed: address not found, userId={}, addressId={}", userId, addressId);
            return new ResourceNotFoundException("Address not found");
        });

        if (!existingAddress.getUserId().equals(user.getId())) {
            log.warn("Delete address request rejected: address does not belong to user, userId={}, addressId={}", userId, addressId);
            throw new ResourceNotFoundException("Address not found");
        }

        boolean isDefaultShipping = existingAddress.isDefaultShipping();
        boolean isDefaultBilling = existingAddress.isDefaultBilling();

        userAddressRepository.delete(existingAddress);

        log.info("Address deleted successfully, userId={}, addressId={}, defaultShipping={}, defaultBilling={}", userId, addressId, isDefaultShipping, isDefaultBilling);

        if (isDefaultShipping && isDefaultBilling) {
            return "Address deleted successfully. It was the default shipping and billing address";
        }

        if (isDefaultShipping) {
            return "Address deleted successfully. It was the default shipping address";
        }

        if (isDefaultBilling) {
            return "Address deleted successfully. It was the default billing address";
        }

        return "Address deleted successfully";
    }

    @Transactional
    public void updateDefaultShipping(UUID addressId, Authentication authentication) {
        UUID userId = ((User) authentication.getPrincipal()).getId();

        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.warn("Set default shipping request failed: user not found, userId={}, addressId={}", userId, addressId);
            return new ResourceNotFoundException("User not found");
        });

        UserAddress existingAddress = userAddressRepository.findById(addressId).orElseThrow(() -> {
            log.warn("Set default shipping request failed: address not found, userId={}, addressId={}", userId, addressId);
            return new ResourceNotFoundException("Address not found");
        });

        if (!existingAddress.getUserId().equals(user.getId())) {
            log.warn("Set default shipping request rejected: address does not belong to user, userId={}, addressId={}", userId, addressId);
            throw new ResourceNotFoundException("Address not found");
        }

        if (existingAddress.isDefaultShipping()) {
            log.info("Set default shipping request skipped: address is already default shipping, userId={}, addressId={}", userId, addressId);
            return;
        }
        userAddressRepository.removeDefaultShipping(user.getId());

        existingAddress.setDefaultShipping(true);
        log.info("Default shipping address updated successfully, userId={}, addressId={}", userId, addressId);
    }

    @Transactional
    public void updateDefaultBilling(UUID addressId, Authentication authentication) {
        UUID userId = ((User) authentication.getPrincipal()).getId();

        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.warn("Set default billing request failed: user not found, userId={}, addressId={}", userId, addressId);
            return new ResourceNotFoundException("User not found");
        });

        UserAddress existingAddress = userAddressRepository.findById(addressId).orElseThrow(() -> {
            log.warn("Set default billing request failed: address not found, userId={}, addressId={}", userId, addressId);
            return new ResourceNotFoundException("Address not found");
        });

        if (!existingAddress.getUserId().equals(user.getId())) {
            log.warn("Set Billing shipping request rejected: address does not belong to user, userId={}, addressId={}", userId, addressId);
            throw new ResourceNotFoundException("Address not found");
        }

        if (existingAddress.isDefaultBilling()) {
            log.info("Set default billing request skipped: address is already default billing, userId={}, addressId={}", userId, addressId);
            return;
        }
        userAddressRepository.removeDefaultBilling(user.getId());

        existingAddress.setDefaultBilling(true);
        log.info("Default Billing address updated successfully, userId={}, addressId={}", userId, addressId);
    }
}