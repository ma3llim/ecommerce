package org.ecommerce.order.service;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ecommerce.common.exception.ExternalServiceException;
import org.ecommerce.common.exception.ResourceNotFoundException;
import org.ecommerce.order.config.RazorpayProperties;
import org.ecommerce.order.entities.Order;
import org.ecommerce.order.entities.Payment;
import org.ecommerce.order.enums.PaymentStatus;
import org.ecommerce.order.repository.OrderRepository;
import org.ecommerce.order.repository.PaymentRepository;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {
    private final RazorpayClient razorpayClient;
    private final RazorpayProperties razorpayProperties;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderFinalizationService orderFinalizationService;

    public String createRazorpayOrder(UUID orderId, BigDecimal amount) throws RazorpayException {
        JSONObject options = new JSONObject();

        options.put("amount", amount.setScale(2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)));
        options.put("currency", "INR");
        options.put("receipt", orderId.toString());

        log.info("options: {}", options);
        com.razorpay.Order razorpayOrder = razorpayClient.orders.create(options);

        log.info("razorpayOrder: {}", razorpayOrder);
        return razorpayOrder.get("id");
    }

    @Transactional
    public void handleWebhook(String payload, String signature) {
        try {
            Utils.verifyWebhookSignature(payload, signature, razorpayProperties.webhookSecret());
        } catch (RazorpayException e) {
            log.warn("Invalid Razorpay webhook signature");
            throw new ExternalServiceException("Invalid webhook signature");
        }

        JSONObject webhook = new JSONObject(payload);
        log.info("webhook: {}", webhook);

        String event = webhook.getString("event");
        log.info("event: {}", event);

        if (!event.equals("payment.captured") && !event.equals("payment.failed")) {
            log.info("Ignoring unsupported Razorpay event: {}", event);
            return;
        }

        // Razorpay order ID
        JSONObject paymentEntity = webhook.getJSONObject("payload").getJSONObject("payment")
                .getJSONObject("entity");
        log.info("paymentEntity: {}", paymentEntity);

        String razorpayPaymentId = paymentEntity.getString("id");
        String razorpayOrderId = paymentEntity.getString("order_id");
        log.info("razorpayPaymentId: {}", razorpayPaymentId);
        log.info("razorpayOrderId :{}", razorpayOrderId);

        Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId).orElseThrow(() ->
                new ResourceNotFoundException("Payment not found"));

        if (payment.getTransactionId() != null) {
            log.info("Payment already processed. transactionId={}", payment.getTransactionId());
            return;
        }

        if (event.equals("payment.captured")) {
            payment.setTransactionId(razorpayPaymentId);
            payment.setPaymentStatus(PaymentStatus.CAPTURED);

            paymentRepository.save(payment);

            Order order = orderRepository.findById(payment.getOrderId()).orElseThrow(() ->
                    new ResourceNotFoundException("Order not found")
            );

            order.setPaymentStatus(PaymentStatus.SUCCESS);
            orderRepository.save(order);

            orderFinalizationService.finalizeOrder(order);
        } else {
            payment.setPaymentStatus(PaymentStatus.FAILED);

            paymentRepository.save(payment);
        }
        log.info("Razorpay webhook processed successfully. event={}, razorpayOrderId={}", event, razorpayOrderId);
    }

    public void refundPayment(Payment payment) {
    }
}
