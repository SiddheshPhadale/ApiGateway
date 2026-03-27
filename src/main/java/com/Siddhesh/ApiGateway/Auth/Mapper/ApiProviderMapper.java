package com.Siddhesh.ApiGateway.Auth.Mapper;

import com.Siddhesh.ApiGateway.Auth.Dto.RegisterationRequestDto;
import com.Siddhesh.ApiGateway.Auth.Entity.ApiProvider;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ApiProviderMapper {
    ApiProvider mapToApiProvider(RegisterationRequestDto userDto);
}
