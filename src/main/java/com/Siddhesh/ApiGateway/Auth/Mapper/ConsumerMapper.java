package com.Siddhesh.ApiGateway.Auth.Mapper;

import com.Siddhesh.ApiGateway.Auth.Dto.RegisterationRequestDto;
import com.Siddhesh.ApiGateway.Auth.Entity.ApiConsumer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConsumerMapper {
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "subscriptions", ignore = true)
    ApiConsumer mapToApiConsumer(RegisterationRequestDto consumerDto);
}
