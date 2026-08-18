package org.ecommerce.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ecommerce.auth.entities.User;
import org.ecommerce.auth.repository.UserRepository;
import org.ecommerce.cart.entities.Cart;
import org.ecommerce.cart.entities.CartItem;
import org.ecommerce.cart.repository.CartItemRepository;
import org.ecommerce.cart.repository.CartRepository;
import org.ecommerce.common.exception.ResourceNotFoundException;
import org.ecommerce.order.dtos.request.CreateOrderRequest;
import org.ecommerce.order.dtos.response.OrderResponse;
import org.ecommerce.user.entity.UserAddress;
import org.ecommerce.user.repository.UserAddressRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {
    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public OrderResponse createOrder(CreateOrderRequest request, Authentication authentication) {
        UUID userId = ((User) authentication.getPrincipal()).getId();
        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.warn("create order request failed: user not found, userId={}", userId);
            return new ResourceNotFoundException("User not found");
        });

        UserAddress address = userAddressRepository.findByUserIdAndAddressId(user.getId(), request.shippingAddressId())
                .orElseThrow(() -> {
                    log.warn("create order request failed: user address not found, addressId={}", request.shippingAddressId());
                    return new ResourceNotFoundException("User Address not found");
                });

        Cart cart = cartRepository.findByUserId(user.getId()).orElseThrow(() -> {
            log.warn("create order request failed: user cart not found, userId={}", userId);
            return new ResourceNotFoundException("User Cart not found");
        });

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            log.warn("create order request failed: user cart items is empty, cartIt={}", cart);
            throw new ResourceNotFoundException("User Cart Item is empty");
        }
        
    }
}
