package com.Siddhesh.ApiGateway.Payments.Dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateOrderRequest {
    @NotNull(message = "Plan ID is required")
    @Positive(message = "Plan ID must be positive")
    private UUID planId;

    // Optional field retained for backwards-compatibility; server uses authenticated user
    private String userName;
}
