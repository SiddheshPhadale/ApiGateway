package com.Siddhesh.ApiGateway.Auth.Mapper;

import com.Siddhesh.ApiGateway.Auth.Dto.RegisterationRequestDto;
import com.Siddhesh.ApiGateway.Auth.Entity.ApiConsumer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ConsumerMapper {
    ApiConsumer mapToApiConsumer(RegisterationRequestDto consumerDto);
}
