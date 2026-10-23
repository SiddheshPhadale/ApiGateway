package com.Siddhesh.ApiGateway.Subscriptions.Dto;

import lombok.Data;

@Data
public class SubscriptionRequestDto {
    private Long apiId;
    private String apiName;
    private String userName;
}