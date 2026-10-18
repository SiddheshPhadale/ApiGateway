package com.Siddhesh.ApiGateway.Payments.Dtos;

import lombok.Data;

@Data
public class VerifyPaymentResponse {
    private String message;
    private String apiKey;        // shown once
    private String planName;
    private String apiName;
    private int rateLimitPerMinute;
    private String transactionId;
}
