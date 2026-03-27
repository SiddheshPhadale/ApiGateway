package com.Siddhesh.ApiGateway.Subscriptions.Dto;

import lombok.Data;

@Data
public class SubscriptionResponseDto {
    private Long subId;
    private String userName;
    private Long apiId;
    private String apiName;
    private String apiKey;
}
