package com.Siddhesh.ApiGateway.Payments.Dtos;

import lombok.Data;

import java.util.UUID;

@Data
public class VerifyPaymentResponse {
    private String message;
    private String apiKey;        // shown once
    private String planName;
    private String apiName;
    private int rateLimitPerMinute;
    private UUID transactionId;
}
