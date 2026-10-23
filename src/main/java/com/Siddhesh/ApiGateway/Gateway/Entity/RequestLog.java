package com.Siddhesh.ApiGateway.Gateway.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "request_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long requestId;

    @Column(name = "api_id")
    private Long apiId;

    @Column(name = "sub_id")
    private Long subId;

    @Column(name = "occurred_at")
    private LocalDateTime occurredAt;

    @Column(name = "http_method")
    private String httpMethod;

    private String route;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "latency_ms")
    private Long latencyMs;

    private String outcome;

    @Column(name = "rate_limited")
    private Boolean rateLimited;
}