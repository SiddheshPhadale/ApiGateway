package com.Siddhesh.ApiGateway.Payments.Dtos;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ApiPlanResponse {
    private UUID planId;
    private String planName;
    private String description;
    private double price;
    private int rateLimitPerMinute;
    private String status;
}
