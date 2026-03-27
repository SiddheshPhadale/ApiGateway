package com.Siddhesh.ApiGateway.Api.Dto;

import lombok.Data;

@Data
public class ApiResponseDto {
    private long apiId;
    private String apiName;
    private String apiDescription;
    private String apiLink;
}
