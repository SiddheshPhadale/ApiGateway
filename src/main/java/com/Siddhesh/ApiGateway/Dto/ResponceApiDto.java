package com.Siddhesh.ApiGateway.Dto;

import lombok.Data;

@Data
public class ResponceApiDto {
    private long apiId;
    private String apiName;
    private String apiKey;
    private String apiDescription;
    private String apiLink;
}
