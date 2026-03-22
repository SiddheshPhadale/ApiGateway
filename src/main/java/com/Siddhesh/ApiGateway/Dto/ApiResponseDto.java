package com.Siddhesh.ApiGateway.Dto;

import lombok.Data;

@Data
public class ApiResponseDto {
    private long apiId;
    private String apiName;
    private String apiDescription;
    private String apiLink;
}
