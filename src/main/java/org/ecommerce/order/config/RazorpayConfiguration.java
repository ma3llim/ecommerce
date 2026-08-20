package org.ecommerce.order.config;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RazorpayConfiguration {
    private final RazorpayProperties razorpayProperties;

    @Bean
    public RazorpayClient razorpayClient(RazorpayProperties razorpayProperties) throws RazorpayException {
        return new RazorpayClient(razorpayProperties.keyId(), razorpayProperties.keySecret());
    }
}
