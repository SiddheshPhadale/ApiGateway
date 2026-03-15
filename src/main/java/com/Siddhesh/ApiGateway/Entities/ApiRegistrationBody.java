package com.Siddhesh.ApiGateway.Entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "Api_Details")
public class ApiRegistrationBody {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "api_id")
    private int apiId;
    private String apiName;
    private String link;
    private String apiKey;
    private String apiDescription;
}
