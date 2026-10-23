package com.Siddhesh.ApiGateway.Subscriptions.Dto;

import com.Siddhesh.ApiGateway.Api.Entity.Status;
import lombok.Data;

import java.util.UUID;

@Data
public class SubscriptionResponseDto {
    private UUID subId;
    private String userName;
    private UUID apiId;
    private String apiName;
    private String apiKey;
    private Status status;
    private String planName;
}
