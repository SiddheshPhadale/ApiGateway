package com.Siddhesh.ApiGateway.Auth.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterationRequestDto {
    @NotBlank
    private String userName;
    @NotBlank
    @Email
    private String userEmail;
    @NotBlank
    @Min(8)
    private String password;
}
