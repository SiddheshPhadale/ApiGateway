package com.Siddhesh.ApiGateway.Subscriptions.Dto;

import lombok.Data;

import java.util.UUID;

@Data
public class SubscriptionRequestDto {
    private UUID apiId;
    private String apiName;
    private String userName;
}