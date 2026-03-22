package com.Siddhesh.ApiGateway.Services.MapperService;

import com.Siddhesh.ApiGateway.Dto.ApiRegistrationBodyDto;
import com.Siddhesh.ApiGateway.Dto.ApiResponseDto;
import com.Siddhesh.ApiGateway.Entities.ApiBody;
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
