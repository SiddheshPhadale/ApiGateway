package com.Siddhesh.ApiGateway.Services.MapperService;

import com.Siddhesh.ApiGateway.Dto.SubscriptionResponseDto;
import com.Siddhesh.ApiGateway.Entities.Subscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {
    @Mapping(source = "api.apiName", target = "apiName")
    @Mapping(source = "consumer.userName", target = "userName")
    @Mapping(source = "api.apiId" , target = "apiId")
    SubscriptionResponseDto mapToSubscriptionDto(Subscription subscription);

    @Mapping(source = "api.apiName", target = "apiName")
    @Mapping(source = "consumer.userName", target = "userName")
    @Mapping(source = "api.apiId" , target = "apiId")
    @Mapping(target = "apiKey", ignore = true)
    SubscriptionResponseDto mapToGetSubscriptionDto(Subscription subscription);

}
