package com.Siddhesh.ApiGateway.Services.MapperService;

import com.Siddhesh.ApiGateway.Dto.RegisterationRequestDto;
import com.Siddhesh.ApiGateway.Entities.ApiProvider;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ApiProviderMapper {
    ApiProvider mapToApiProvider(RegisterationRequestDto userDto);
}
