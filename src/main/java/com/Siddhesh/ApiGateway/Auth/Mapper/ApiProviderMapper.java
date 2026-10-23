package com.Siddhesh.ApiGateway.Auth.Mapper;

import com.Siddhesh.ApiGateway.Auth.Dto.RegisterationRequestDto;
import com.Siddhesh.ApiGateway.Auth.Entity.ApiProvider;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ApiProviderMapper {
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "apiList", ignore = true)
    ApiProvider mapToApiProvider(RegisterationRequestDto userDto);
}
