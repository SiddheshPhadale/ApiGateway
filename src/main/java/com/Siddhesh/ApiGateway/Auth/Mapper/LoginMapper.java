package com.Siddhesh.ApiGateway.Auth.Mapper;

import com.Siddhesh.ApiGateway.Auth.Dto.LoginResponseDto;
import com.Siddhesh.ApiGateway.Auth.Entity.BaseUser;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LoginMapper {
    LoginResponseDto mapToLoginResponseDto(BaseUser baseUser);
}
