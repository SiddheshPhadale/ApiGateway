package com.Siddhesh.ApiGateway.Analytics.Dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiAnomalyEvent {

    private String eventId;
    private Instant detectedAt;

    private Long apiId;
    private Long subscriptionId;

    private Double anomalyScore;
    private String severity;
    private String anomalyType;

    private Map<String, Object> featureSnapshot;

    private String modelVersion;
    private String status;
}
