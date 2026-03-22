package com.Siddhesh.ApiGateway.Entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;

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
}
