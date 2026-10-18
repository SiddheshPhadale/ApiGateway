package com.Siddhesh.ApiGateway.Gateway.Dto;

import com.Siddhesh.ApiGateway.Api.Entity.Status;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CachedSubscriptionDto {
    private String targetUrl;
    private Status subscriptionStatus;
    private Status apiStatus;
    private int rateLimitPerMinute;
    private String apiKey;
}
