package com.Siddhesh.ApiGateway.Payments.Dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreatePlanRequest {
    @NotBlank
    private String planName;

    @NotNull
    private double price;

    @NotNull
    private int rateLimitPerMinute;

    private String description;

}
