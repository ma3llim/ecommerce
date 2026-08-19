package org.ecommerce.order.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ecommerce.auth.entities.User;
import org.ecommerce.auth.repository.UserRepository;
import org.ecommerce.cart.entities.Cart;
import org.ecommerce.cart.entities.CartItem;
import org.ecommerce.cart.repository.CartItemRepository;
import org.ecommerce.cart.repository.CartRepository;
import org.ecommerce.cart.repository.projection.CartOrderItemProjection;
import org.ecommerce.common.enums.DiscountType;
import org.ecommerce.common.exception.BadRequestException;
import org.ecommerce.common.exception.ResourceNotFoundException;
import org.ecommerce.coupon.entities.Coupon;
import org.ecommerce.coupon.repository.CouponRepository;
import org.ecommerce.order.dtos.request.CreateOrderRequest;
import org.ecommerce.order.dtos.response.OrderResponse;
import org.ecommerce.order.dtos.response.PaymentResponse;
import org.ecommerce.order.entities.Order;
import org.ecommerce.order.entities.OrderItem;
import org.ecommerce.order.entities.Payment;
import org.ecommerce.order.enums.OrderStatus;
import org.ecommerce.order.enums.PaymentMethod;
import org.ecommerce.order.enums.PaymentStatus;
import org.ecommerce.order.repository.OrderItemRepository;
import org.ecommerce.order.repository.OrderRepository;
import org.ecommerce.order.repository.PaymentRepository;
import org.ecommerce.user.entity.UserAddress;
import org.ecommerce.user.repository.UserAddressRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
    private final OrderRepository orderRepository;
    private final CouponRepository couponRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper;
    private final PaymentService paymentService;
    private final OrderFinalizationService orderFinalizationService;


    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, Authentication authentication) throws RazorpayException {
        UUID userId = ((User) authentication.getPrincipal()).getId();
        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.warn("Create order failed: user not found, userId={}", userId);
            return new ResourceNotFoundException("User not found");
        });

        UserAddress address = userAddressRepository.findByUserIdAndId(user.getId(), request.shippingAddressId()).orElseThrow(() -> {
            log.warn("Create order failed: address not found, addressId={}, userId={}", request.shippingAddressId(), userId);
            return new ResourceNotFoundException("User address not found");
        });

        Cart cart = cartRepository.findByUserId(user.getId()).orElseThrow(() -> {
            log.warn("Create order failed: cart not found, userId={}", userId);
            return new ResourceNotFoundException("User cart not found");
        });

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            log.warn("Create order rejected: cart is empty. userId={}, cartId={}", userId, cart.getId());
            throw new BadRequestException("Cannot create order from an empty cart");
        }

        List<CartOrderItemProjection> items = cartItemRepository.findOrderItemsByCartId(cart.getId());

        if (items.size() != cartItems.size()) {
            log.warn("Create order rejected: one or more cart items are unavailable. userId={}, cartId={}, " +
                    "cartItems={}, availableItems={}", userId, cart.getId(), cartItems.size(), items.size());
            throw new BadRequestException("One or more products in your cart are no longer available");
        }

        BigDecimal subTotal = BigDecimal.ZERO;
        for (CartOrderItemProjection item : items) {
            if (item.getQuantity() > item.getStockQuantity()) {
                log.warn(
                        "Create order rejected: insufficient stock. userId={}, productId={}, variantId={}, " +
                                "productName={}, requestedQuantity={}, availableStock={}",
                        userId, item.getProductId(), item.getProductVariantId(), item.getProductName(),
                        item.getQuantity(), item.getStockQuantity());

                throw new BadRequestException("Insufficient stock for product: " + item.getProductName());
            }

            BigDecimal itemTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            subTotal = subTotal.add(itemTotal);
        }

        Coupon coupon = null;
        BigDecimal discountAmount = BigDecimal.ZERO;

        if (request.couponCode() != null && !request.couponCode().isBlank()) {
            String couponCode = request.couponCode().trim();

            coupon = couponRepository.findByCodeIgnoreCaseAndActiveTrue(couponCode).orElseThrow(() -> {
                log.warn("Create order rejected: coupon not found or inactive. couponCode={}, userId={}",
                        couponCode, userId);
                return new ResourceNotFoundException("Coupon not found or inactive");
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

            if (coupon.getMinimumOrderAmount() != null && subTotal.compareTo(coupon.getMinimumOrderAmount()) < 0) {
                log.warn("Apply coupon rejected: minimum order amount not met. couponCode={}, userId={}, subtotal={}, minimumOrderAmount={}",
                        coupon.getCode(), userId, subTotal, coupon.getMinimumOrderAmount());
                throw new BadRequestException("Minimum order amount for this coupon is " + coupon.getMinimumOrderAmount());
            }

            if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
                discountAmount = subTotal.multiply(coupon.getDiscountValue()).divide(BigDecimal.valueOf(100));

                if (coupon.getMaximumDiscountAmount() != null && discountAmount.compareTo(coupon.getMaximumDiscountAmount()) > 0) {
                    discountAmount = coupon.getMaximumDiscountAmount();
                }
            } else {
                discountAmount = coupon.getDiscountValue();

                if (discountAmount.compareTo(subTotal) > 0) {
                    discountAmount = subTotal;
                }
            }
        }

        BigDecimal shippingAmount = new BigDecimal("40");

        BigDecimal taxableAmount = subTotal.subtract(discountAmount);
        BigDecimal taxAmount = taxableAmount.multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalAmount = taxableAmount.add(shippingAmount).add(taxAmount);

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .userId(userId)
                .shippingAddressId(address.getId())
                .totalAmount(totalAmount)
                .shippingAmount(shippingAmount)
                .discountAmount(discountAmount)
                .taxAmount(taxAmount)
                .couponId(coupon != null ? coupon.getId() : null)
                .couponCode(coupon != null ? coupon.getCode() : null)
                .paymentStatus(PaymentStatus.PENDING)
                .orderStatus(OrderStatus.PENDING).build();

        order = orderRepository.save(order);

        List<OrderItem> orderItems = new ArrayList<>();

        for (CartOrderItemProjection item : items) {
            BigDecimal itemTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

            OrderItem orderItem = OrderItem.builder()
                    .orderId(order.getId())
                    .productId(item.getProductId())
                    .productVariantId(item.getProductVariantId())
                    .productName(item.getProductName())
                    .variantName(item.getSku())
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .totalPrice(itemTotal)
                    .build();

            orderItems.add(orderItem);
        }

        orderItemRepository.saveAll(orderItems);

        Payment payment = Payment.builder()
                .orderId(order.getId())
                .paymentMethod(request.paymentMethod())
                .amount(totalAmount)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        if (request.paymentMethod() != PaymentMethod.COD) {
            String razorpayOrderId = paymentService.createRazorpayOrder(order.getId(), totalAmount);
            payment.setRazorpayOrderId(razorpayOrderId);

            log.info("Razorpay order created successfully. orderId={}, razorpayOrderId={}, amount={}",
                    order.getId(), razorpayOrderId, totalAmount);
        }
        payment = paymentRepository.save(payment);

        if (request.paymentMethod() == PaymentMethod.COD) {
            orderFinalizationService.finalizeOrder(order);

            log.info("COD order finalized successfully. orderId={}, orderNumber={}",
                    order.getId(), order.getOrderNumber());
        }

        PaymentResponse paymentResponse = objectMapper.convertValue(payment, PaymentResponse.class);
        
        log.info("Order created successfully. orderId={}, orderNumber={}, userId={}, paymentMethod={}, subtotal={}, " +
                        "discount={}, shipping={}, tax={}, total={}",
                order.getId(), order.getOrderNumber(), userId, request.paymentMethod(), subTotal, discountAmount,
                shippingAmount, taxAmount, totalAmount);

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .subtotal(subTotal)
                .shippingAmount(order.getShippingAmount())
                .discountAmount(order.getDiscountAmount())
                .taxAmount(order.getTaxAmount())
                .totalAmount(order.getTotalAmount())
                .couponId(order.getCouponId())
                .couponCode(order.getCouponCode())
                .paymentStatus(order.getPaymentStatus())
                .orderStatus(order.getOrderStatus())
                .payment(paymentResponse)
                .build();
    }


    private String generateOrderNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);

        String random = UUID.randomUUID()
                .toString()
                .substring(0, 6)
                .toUpperCase();

        return "ORD-" + date + "-" + random;
    }
}
