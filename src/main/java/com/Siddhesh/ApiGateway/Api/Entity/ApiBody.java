package com.Siddhesh.ApiGateway.Api.Entity;

import com.Siddhesh.ApiGateway.Auth.Entity.ApiProvider;
import com.Siddhesh.ApiGateway.Payments.Entities.ApiPlan;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "Api_Details")
public class ApiBody {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "api_id")
    private UUID apiId;

    @Column(nullable = false, unique = true)
    private String apiName;

    @Column(nullable = false)
    private String link;

    @Size(max = 500)
    private String apiDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    private int rateLimitPerMinute;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private ApiProvider provider;

    @OneToMany(mappedBy = "api", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ApiPlan> plans = new ArrayList<>();
}
