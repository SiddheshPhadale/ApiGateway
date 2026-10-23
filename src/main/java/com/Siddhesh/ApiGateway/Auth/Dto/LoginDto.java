package com.Siddhesh.ApiGateway.Auth.Dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDto {
    @NotBlank
    private String userName;
    @NotBlank
    private String password;
}
