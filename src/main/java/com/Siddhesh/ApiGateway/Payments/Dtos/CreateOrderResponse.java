package com.Siddhesh.ApiGateway.Payments.Dtos;

import lombok.Data;

@Data
public class CreateOrderResponse {
    private String razorpayOrderId;
    private String currency;
    private Long amount;          // in paise (₹99 = 9900 paise)
    private String keyId;         // Razorpay public key for frontend
    private String apiName;
    private String planName;
}
