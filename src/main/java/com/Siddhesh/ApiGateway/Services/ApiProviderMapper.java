package com.Siddhesh.ApiGateway.Services;

import com.Siddhesh.ApiGateway.Dto.ApiProviderDto;
import com.Siddhesh.ApiGateway.Entities.ApiProvider;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ApiProviderMapper {
    ApiProvider mapToUser(ApiProviderDto userDto);
}
