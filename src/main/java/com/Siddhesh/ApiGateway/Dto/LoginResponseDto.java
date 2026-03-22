package com.Siddhesh.ApiGateway.Dto;

import com.Siddhesh.ApiGateway.Entities.Role;
import lombok.Data;

@Data
public class LoginResponseDto {
    private String userName;
    private Role role;
    private String jwtToken;

}
