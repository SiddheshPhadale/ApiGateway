package com.Siddhesh.ApiGateway.Auth.Mapper;

import com.Siddhesh.ApiGateway.Auth.Dto.LoginResponseDto;
import com.Siddhesh.ApiGateway.Auth.Entity.BaseUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoginMapper {
    @Mapping(target = "jwtToken", ignore = true)
    LoginResponseDto mapToLoginResponseDto(BaseUser baseUser);
}
