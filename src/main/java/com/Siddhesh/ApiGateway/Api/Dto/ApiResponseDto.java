package com.Siddhesh.ApiGateway.Api.Dto;

import lombok.Data;

import java.util.UUID;

@Data
public class ApiResponseDto {
    private UUID apiId;
    private String apiName;
    private String apiDescription;
    private String apiLink;
}
