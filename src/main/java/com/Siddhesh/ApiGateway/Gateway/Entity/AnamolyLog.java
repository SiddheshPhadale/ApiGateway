package com.Siddhesh.ApiGateway.Gateway.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "anomaly_logs")
public class AnomalyLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long consumerId;
    private String type;
    private Integer riskScore;
    
    @Column(length = 500)
    private String message;
    
    private LocalDateTime detectedAt = LocalDateTime.now();
}