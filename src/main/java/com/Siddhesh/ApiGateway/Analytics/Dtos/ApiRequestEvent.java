package com.Siddhesh.ApiGateway.Analytics.Dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiRequestEvent {

    private String eventId;
    private Instant timestamp;

    private UUID apiId;
    private UUID subscriptionId;

    private String endpoint;
    private String httpMethod;

    private Integer statusCode;
    private Long responseTimeMs;

    private Boolean rateLimitHit;
    private String errorType;
}
