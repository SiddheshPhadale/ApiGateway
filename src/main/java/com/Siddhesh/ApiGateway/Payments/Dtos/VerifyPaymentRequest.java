package com.Siddhesh.ApiGateway.Payments.Dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyPaymentRequest {
    @NotBlank(message = "Razorpay order ID is required")
    private String razorpayOrderId;

    @NotBlank(message = "Razorpay payment ID is required")
    private String razorpayPaymentId;

    @NotBlank(message = "Razorpay signature is required")
    private String razorpaySignature;

    // Optional fields retained for backwards-compatibility; server uses persisted order and authenticated identity
    private Long planId;
    private String userName;
}
