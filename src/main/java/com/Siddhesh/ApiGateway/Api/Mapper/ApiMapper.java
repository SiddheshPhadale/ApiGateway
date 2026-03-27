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

    ApiBody mapToApiBody(ApiRegistrationBodyDto apiDto);
}
