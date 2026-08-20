package org.ecommerce.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ecommerce.cart.entities.Cart;
import org.ecommerce.cart.repository.CartItemRepository;
import org.ecommerce.cart.repository.CartRepository;
import org.ecommerce.catelog.repository.ProductVariantRepository;
import org.ecommerce.common.exception.BadRequestException;
import org.ecommerce.common.exception.ResourceNotFoundException;
import org.ecommerce.coupon.entities.Coupon;
import org.ecommerce.coupon.repository.CouponRepository;
import org.ecommerce.order.entities.Order;
import org.ecommerce.order.enums.OrderStatus;
import org.ecommerce.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderFinalizationService {
    private final ProductVariantRepository productVariantRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CouponRepository couponRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public void finalizeOrder(Order order) {
        // reduce stock
        long insufficientStock = productVariantRepository.countInsufficientStock(order.getId());
        if (insufficientStock > 0) {
            log.warn("Order finalization rejected: insufficient stock. orderId={}, insufficientItems={}",
                    order.getId(), insufficientStock);
            throw new BadRequestException("Insufficient stock for one or more products");
        }

        int updatedRows = productVariantRepository.reduceStock(order.getId());

        if (updatedRows == 0) {
            log.warn("Order finalization failed: unable to reserve stock. orderId={}", order.getId());
            throw new BadRequestException("Unable to reserve product stock");
        }

        // clear cart and cart items
        Cart cart = cartRepository.findByUserId(order.getUserId()).orElseThrow(() -> {
            log.warn("Order finalization failed: cart not found. orderId={}, userId={}",
                    order.getId(), order.getUserId());
            return new ResourceNotFoundException("Cart not found");
        });

        cartItemRepository.deleteByCartId(cart.getId());

        cart.setTotalAmount(BigDecimal.ZERO);

        cartRepository.save(cart);

        log.info("Cart cleared after order finalization. cartId={}, userId={}, orderId={}",
                cart.getId(), order.getUserId(), order.getId());

        // increase coupon usage
        if (order.getCouponId() != null) {
            Coupon coupon = couponRepository.findById(order.getCouponId()).orElseThrow(() -> {
                        log.warn("Order finalization failed: coupon not found. orderId={}, couponId={}",
                                order.getId(), order.getCouponId());
                        return new ResourceNotFoundException("Coupon not found");
                    }
            );

            coupon.setUsedCount(coupon.getUsedCount() + 1);
            couponRepository.save(coupon);
            
            log.info("Coupon usage incremented. orderId={}, couponId={}, couponCode={}, usedCount={}",
                    order.getId(), coupon.getId(), coupon.getCode(), coupon.getUsedCount());
        }

        order.setOrderStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);
        log.info("Order finalized successfully. orderId={}, userId={}, status={}",
                order.getId(), order.getUserId(), order.getOrderStatus());
    }
}
