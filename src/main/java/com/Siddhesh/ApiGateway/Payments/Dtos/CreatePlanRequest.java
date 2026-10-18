package com.Siddhesh.ApiGateway.Payments.Dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePlanRequest {
    @NotBlank
    private String planName;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price cannot be negative")
    private BigDecimal price;

    @NotNull(message = "Rate limit is required")
    @Min(value = 1, message = "Rate limit must be at least 1")
    private Integer rateLimitPerMinute;

    private String description;

}
