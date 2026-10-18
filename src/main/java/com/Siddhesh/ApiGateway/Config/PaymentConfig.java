package com.Siddhesh.ApiGateway.Config;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentConfig {

    @Value("${razorpay.key.id:}")
    private String keyId;

    @Value("${razorpay.key.secret:}")
    private String keySecret;

    @Getter
    @Value("${platform.commission-rate:0.10}")
    private double commissionRate;

    @Bean
    public RazorpayClient razorpayClient() {
        try {
            if (keyId != null && !keyId.trim().isEmpty() && keySecret != null && !keySecret.trim().isEmpty()) {
                return new RazorpayClient(keyId, keySecret);
            }
            return new RazorpayClient("dummy_key", "dummy_secret");
        } catch (RazorpayException e) {
            throw new IllegalStateException("Failed to initialize RazorpayClient: " + e.getMessage(), e);
        }
    }

}
