package com.Siddhesh.ApiGateway.Auth.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterationRequestDto {
    @NotBlank
    private String userName;
    @NotBlank
    @Email
    private String userEmail;
    @NotBlank
    @Size(min = 8, max = 50)
    private String password;
}
