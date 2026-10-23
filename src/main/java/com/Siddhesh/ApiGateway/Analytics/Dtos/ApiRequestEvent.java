package com.Siddhesh.ApiGateway.Analytics.Dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiRequestEvent {

    private String eventId;
    private Instant timestamp;

    private Long apiId;
    private Long subscriptionId;

    private String endpoint;
    private String httpMethod;

    private Integer statusCode;
    private Long responseTimeMs;

    private Boolean rateLimitHit;
    private String errorType;
}
