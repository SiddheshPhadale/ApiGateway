package com.Siddhesh.ApiGateway.Payments.Dtos;

import lombok.Data;

@Data
public class ApiPlanResponse {
    private Long planId;
    private String planName;
    private String description;
    private double price;
    private int rateLimitPerMinute;
    private String status;
}
