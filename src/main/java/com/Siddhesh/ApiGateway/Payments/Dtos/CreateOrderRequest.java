package com.Siddhesh.ApiGateway.Payments.Dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderRequest {
    @NotNull(message = "Plan ID is required")
    private Long planId;

    @NotBlank(message = "Consumer username is required")
    private String userName;
}
