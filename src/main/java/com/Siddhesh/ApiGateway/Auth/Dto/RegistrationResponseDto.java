package com.Siddhesh.ApiGateway.Auth.Dto;

import com.Siddhesh.ApiGateway.Auth.Entity.Role;
import lombok.Data;

@Data
public class RegistrationResponseDto {
    private String userName;
    private Role role;
    private String msg;
}
