package com.Siddhesh.ApiGateway.Dto;

import com.Siddhesh.ApiGateway.Entities.Role;
import lombok.Data;

@Data
public class RegistrationResponseDto {
    private String userName;
    private Role role;
    private String msg;
}
