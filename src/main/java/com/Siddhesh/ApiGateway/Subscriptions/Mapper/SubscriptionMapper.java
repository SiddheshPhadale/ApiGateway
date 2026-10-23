package com.Siddhesh.ApiGateway.Subscriptions.Mapper;

import com.Siddhesh.ApiGateway.Subscriptions.Dto.SubscriptionResponseDto;
import com.Siddhesh.ApiGateway.Subscriptions.Entity.Subscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {
    @Mapping(source = "api.apiName", target = "apiName")
    @Mapping(source = "consumer.userName", target = "userName")
    @Mapping(source = "api.apiId", target = "apiId")
    @Mapping(source = "plan.planName", target = "planName")
    @Mapping(source = "status", target = "status")
    SubscriptionResponseDto mapToSubscriptionDto(Subscription subscription);

    @Mapping(source = "api.apiName", target = "apiName")
    @Mapping(source = "consumer.userName", target = "userName")
    @Mapping(source = "api.apiId", target = "apiId")
    @Mapping(source = "subscription.subId", target = "subId")
    @Mapping(source = "plan.planName", target = "planName")
    @Mapping(source = "status", target = "status")
    @Mapping(target = "apiKey", ignore = true)
    SubscriptionResponseDto mapToGetSubscriptionDto(Subscription subscription);
}
