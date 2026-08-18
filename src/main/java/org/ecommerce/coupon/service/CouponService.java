package org.ecommerce.coupon.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ecommerce.auth.entities.User;
import org.ecommerce.auth.repository.UserRepository;
import org.ecommerce.cart.entities.Cart;
import org.ecommerce.cart.entities.CartItem;
import org.ecommerce.cart.repository.CartItemRepository;
import org.ecommerce.cart.repository.CartRepository;
import org.ecommerce.common.enums.DiscountType;
import org.ecommerce.common.exception.BadRequestException;
import org.ecommerce.common.exception.ResourceNotFoundException;
import org.ecommerce.coupon.dtos.response.ApplyCouponResponse;
import org.ecommerce.coupon.entities.Coupon;
import org.ecommerce.coupon.repository.CouponRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CouponService {
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CouponRepository couponRepository;

    public ApplyCouponResponse applyCouponCode(String code, Authentication authentication) {
        UUID userId = ((User) authentication.getPrincipal()).getId();
        User user = userRepository.findById(userId).orElseThrow(() -> {
                    log.warn("Apply coupon failed: user not found. userId={}", userId);
                    return new ResourceNotFoundException("User not found");
                }
        );

        Cart cart = cartRepository.findByUserId(userId).orElseThrow(() -> {
            log.warn("Apply coupon failed: cart not found. userId={}", userId);
            return new ResourceNotFoundException("Cart not found");
        });

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());

        if (cartItems.isEmpty()) {
            log.warn("Apply coupon rejected: cart is empty. cartId={}, userId={}, couponCode={}",
                    cart.getId(), userId, code);
            throw new BadRequestException("Cannot apply coupon to an empty cart");
        }

        BigDecimal cartSubTotal = cartItems.stream().map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Coupon coupon = couponRepository.findByCodeIgnoreCaseAndActiveTrue(code.trim()).orElseThrow(() -> {
            log.warn("Apply coupon failed: coupon not found. code={}, userId={}", code, userId);
            return new ResourceNotFoundException("Coupon not found");
        });

        Instant now = Instant.now();

        if (now.isBefore(coupon.getValidFrom()) || now.isAfter(coupon.getValidUntil())) {
            log.warn("Apply coupon rejected: coupon is outside validity period. couponCode={}, userId={}, validFrom={}, validUntil={}",
                    coupon.getCode(), userId, coupon.getValidFrom(), coupon.getValidUntil());
            throw new BadRequestException("Coupon is expired or not yet active");
        }

        if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            log.warn("Apply coupon rejected: usage limit reached. couponCode={}, userId={}, usedCount={}, usageLimit={}",
                    coupon.getCode(), userId, coupon.getUsedCount(), coupon.getUsageLimit());
            throw new BadRequestException("Coupon usage limit has been reached");
        }

        if (coupon.getMinimumOrderAmount() != null && cartSubTotal.compareTo(coupon.getMinimumOrderAmount()) < 0) {
            log.warn("Apply coupon rejected: minimum order amount not met. couponCode={}, userId={}, subtotal={}, minimumOrderAmount={}",
                    coupon.getCode(), userId, cartSubTotal, coupon.getMinimumOrderAmount());
            throw new BadRequestException("Minimum order amount for this coupon is " + coupon.getMinimumOrderAmount());
        }

        BigDecimal discountAmount;
        if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
            discountAmount = cartSubTotal.multiply(coupon.getDiscountValue()).divide(BigDecimal.valueOf(100));

            if (coupon.getMaximumDiscountAmount() != null && discountAmount.compareTo(coupon.getMaximumDiscountAmount()) > 0) {
                discountAmount = coupon.getMaximumDiscountAmount();
            }
        } else {
            discountAmount = coupon.getDiscountValue();

            if (discountAmount.compareTo(cartSubTotal) > 0) {
                discountAmount = cartSubTotal;
            }
        }

        BigDecimal finalAmount = cartSubTotal.subtract(discountAmount);
        log.info("Coupon applied successfully. userId={}, couponCode={}, " + "subtotal={}, discount={}, finalAmount={}",
                userId, coupon.getCode(), cartSubTotal, discountAmount, finalAmount
        );

        return new ApplyCouponResponse(cartSubTotal, discountAmount, finalAmount, coupon.getCode().toUpperCase());
    }
}
