package com.Siddhesh.ApiGateway.Subscriptions.Mapper;

import com.Siddhesh.ApiGateway.Api.Entity.ApiBody;
import com.Siddhesh.ApiGateway.Auth.Entity.ApiConsumer;
import com.Siddhesh.ApiGateway.Subscriptions.Dto.SubscriptionResponseDto;
import com.Siddhesh.ApiGateway.Subscriptions.Entity.Subscription;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-17T19:03:06+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.7 (Oracle Corporation)"
)
@Component
public class SubscriptionMapperImpl implements SubscriptionMapper {

    @Override
    public SubscriptionResponseDto mapToSubscriptionDto(Subscription subscription) {
        if ( subscription == null ) {
            return null;
        }

        SubscriptionResponseDto subscriptionResponseDto = new SubscriptionResponseDto();

        subscriptionResponseDto.setApiName( subscriptionApiApiName( subscription ) );
        subscriptionResponseDto.setUserName( subscriptionConsumerUserName( subscription ) );
        subscriptionResponseDto.setApiId( subscriptionApiApiId( subscription ) );
        subscriptionResponseDto.setSubId( subscription.getSubId() );
        subscriptionResponseDto.setApiKey( subscription.getApiKey() );

        return subscriptionResponseDto;
    }

    @Override
    public SubscriptionResponseDto mapToGetSubscriptionDto(Subscription subscription) {
        if ( subscription == null ) {
            return null;
        }

        SubscriptionResponseDto subscriptionResponseDto = new SubscriptionResponseDto();

        subscriptionResponseDto.setApiName( subscriptionApiApiName( subscription ) );
        subscriptionResponseDto.setUserName( subscriptionConsumerUserName( subscription ) );
        subscriptionResponseDto.setApiId( subscriptionApiApiId( subscription ) );
        subscriptionResponseDto.setSubId( subscription.getSubId() );

        return subscriptionResponseDto;
    }

    private String subscriptionApiApiName(Subscription subscription) {
        ApiBody api = subscription.getApi();
        if ( api == null ) {
            return null;
        }
        return api.getApiName();
    }

    private String subscriptionConsumerUserName(Subscription subscription) {
        ApiConsumer consumer = subscription.getConsumer();
        if ( consumer == null ) {
            return null;
        }
        return consumer.getUserName();
    }

    private Long subscriptionApiApiId(Subscription subscription) {
        ApiBody api = subscription.getApi();
        if ( api == null ) {
            return null;
        }
        return api.getApiId();
    }
}
