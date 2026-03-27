package com.Siddhesh.ApiGateway.Api.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class ApiRegistrationBodyDto {
    @NotBlank
    private String apiName;
    @NotBlank
    @URL
    private String link;
    @Size(max = 500)
    private String apiDescription;
}
