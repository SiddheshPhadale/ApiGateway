package com.Siddhesh.ApiGateway.Api.Entity;

import com.Siddhesh.ApiGateway.Payments.Entities.ApiPlan;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name = "Api_Details")
public class ApiBody {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "api_id")
    private Long apiId;
    @Column(nullable = false)
    private String apiName;
    @Column(nullable = false)
    private String link;
    @Size(max = 500)
    private String apiDescription;
    private Status status;
    private int rateLimitPerMinute;
    @OneToMany(mappedBy = "api", cascade = CascadeType.ALL)
    private List<ApiPlan> plans;
}
