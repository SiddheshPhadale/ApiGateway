package com.Siddhesh.ApiGateway.Services.MapperService;

import com.Siddhesh.ApiGateway.Dto.RegisterApiConsumerDto;
import com.Siddhesh.ApiGateway.Entities.ApiConsumer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ConsumerMapper {
    ApiConsumer mapToApiConsumer(RegisterApiConsumerDto consumerDto);
}
