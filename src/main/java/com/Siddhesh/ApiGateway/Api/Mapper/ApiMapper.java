package com.Siddhesh.ApiGateway.Api.Mapper;

import com.Siddhesh.ApiGateway.Api.Dto.ApiRegistrationBodyDto;
import com.Siddhesh.ApiGateway.Api.Dto.ApiResponseDto;
import com.Siddhesh.ApiGateway.Api.Entity.ApiBody;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ApiMapper {

    @Mapping(source = "link", target = "apiLink")
    ApiResponseDto mapToResponceApiDto(ApiBody apiBody);

    @Mapping(source = "link", target = "apiLink")
    ApiResponseDto mapToApiRegistrationResponse(ApiBody apiBody);

    @Mapping(target = "apiId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "provider", ignore = true)
    @Mapping(target = "plans", ignore = true)
    ApiBody mapToApiBody(ApiRegistrationBodyDto apiDto);
}
