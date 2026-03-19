package com.Siddhesh.ApiGateway.Services;

import com.Siddhesh.ApiGateway.Dto.ApiRegistrationBodyDto;
import com.Siddhesh.ApiGateway.Dto.ResponceApiDto;
import com.Siddhesh.ApiGateway.Entities.ApiBody;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ApiMapper {

    @Mapping(source = "link", target = "apiLink")
    @Mapping(target = "apiKey" , ignore = true)
    ResponceApiDto mapToResponceApiDto(ApiBody apiBody);

    @Mapping(source = "link", target = "apiLink")
    ResponceApiDto mapToApiRegistrationResponse(ApiBody apiBody);

    ApiBody mapToApiBody(ApiRegistrationBodyDto apiDto);
}
