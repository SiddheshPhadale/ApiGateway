package com.Siddhesh.ApiGateway.Services.MapperService;

import com.Siddhesh.ApiGateway.Dto.LoginDto;
import com.Siddhesh.ApiGateway.Dto.LoginResponseDto;
import com.Siddhesh.ApiGateway.Entities.BaseUser;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LoginMapper {
    LoginResponseDto mapToLoginResponseDto(BaseUser baseUser);
}
