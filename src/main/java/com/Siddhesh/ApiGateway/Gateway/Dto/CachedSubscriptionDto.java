package com.Siddhesh.ApiGateway.Gateway.Dto;

import com.Siddhesh.ApiGateway.Api.Entity.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CachedSubscriptionDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private String targetUrl;
    private Status subscriptionStatus;
    private Status apiStatus;
    private int rateLimitPerMinute;
    private String apiKey;
    private UUID apiId;
    private UUID planId;
    private String planName;
    private UUID subId;
}
