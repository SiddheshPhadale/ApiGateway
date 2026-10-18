package com.Siddhesh.ApiGateway.Subscriptions.Service;

import com.Siddhesh.ApiGateway.Subscriptions.Dto.SubscriptionRequestDto;
import com.Siddhesh.ApiGateway.Subscriptions.Dto.SubscriptionResponseDto;
import com.Siddhesh.ApiGateway.Api.Entity.ApiBody;
import com.Siddhesh.ApiGateway.Auth.Entity.ApiConsumer;
import com.Siddhesh.ApiGateway.Api.Entity.Status;
import com.Siddhesh.ApiGateway.Subscriptions.Entity.Subscription;
import com.Siddhesh.ApiGateway.Exceptions.ApiNotFound;
import com.Siddhesh.ApiGateway.Exceptions.DuplicateSubscriptionException;
import com.Siddhesh.ApiGateway.Exceptions.ResourceNotFoundException;
import com.Siddhesh.ApiGateway.Exceptions.UserNotFoundException;
import com.Siddhesh.ApiGateway.Auth.Repository.ApiConsumerRepo;
import com.Siddhesh.ApiGateway.Api.Repository.ApiRepo;
import com.Siddhesh.ApiGateway.Gateway.Service.CacheService;
import com.Siddhesh.ApiGateway.Payments.Entities.ApiPlan;
import com.Siddhesh.ApiGateway.Payments.Repositories.ApiPlanRepo;
import com.Siddhesh.ApiGateway.Subscriptions.Repository.SubscriptionRepo;
import com.Siddhesh.ApiGateway.Subscriptions.Mapper.SubscriptionMapper;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepo subRepo;
    private final ApiRepo apiRepo;
    private final ApiPlanRepo planRepo;
    private final ApiConsumerRepo consumerRepo;
    private final SubscriptionMapper mapper;
    private final CacheService cacheService;
    private static final SecureRandom random = new SecureRandom();

    @Transactional
    public SubscriptionResponseDto subscribe(SubscriptionRequestDto subscriptionRequestDto, String authenticatedUsername){
        ApiConsumer consumer = consumerRepo.findByUserName(authenticatedUsername);
        if (consumer == null) {
            throw new UserNotFoundException("Requested consumer not found: " + authenticatedUsername);
        }

        ApiBody api = null;
        if (subscriptionRequestDto.getApiId() != null) {
            api = apiRepo.findById(subscriptionRequestDto.getApiId()).orElse(null);
        } else if (subscriptionRequestDto.getApiName() != null) {
            api = apiRepo.findByApiName(subscriptionRequestDto.getApiName());
        }

        if (api == null || api.getStatus() != Status.ACTIVE) {
            throw new ApiNotFound("Requested API does not exist or is inactive!");
        }

        // Prevent payment bypass: If API has active paid plans (price > 0), reject direct free subscription
        List<com.Siddhesh.ApiGateway.Payments.Entities.ApiPlan> activePlans = planRepo.findByApiApiIdAndStatus(api.getApiId(), Status.ACTIVE);
        boolean hasPaidPlans = activePlans != null && activePlans.stream()
                .anyMatch(plan -> plan.getPrice() != null && plan.getPrice().compareTo(BigDecimal.ZERO) > 0);
        if (hasPaidPlans) {
            throw new IllegalStateException("This API requires a paid plan. Please subscribe through the payment flow.");
        }

        if (subRepo.existsByConsumerAndApi(consumer, api)) {
            throw new DuplicateSubscriptionException("Subscription already exists for this API!");
        }

        Subscription subscription = new Subscription();
        subscription.setApi(api);
        subscription.setConsumer(consumer);
        subscription.setStatus(Status.ACTIVE);
        subscription.setApiKey("key_" + generateApiKey());
        Subscription saveSubscription = subRepo.save(subscription);

        if (consumer.getSubscriptions() == null) {
            consumer.setSubscriptions(new ArrayList<>());
        }
        consumer.getSubscriptions().add(saveSubscription);
        consumerRepo.save(consumer);

        return mapper.mapToSubscriptionDto(saveSubscription);
    }

    @Transactional
    public void cancelSubscription(Long subId, String authenticatedUsername) {
        Subscription sub = subRepo.findById(subId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with ID: " + subId));

        if (!sub.getConsumer().getUserName().equals(authenticatedUsername)) {
            throw new SecurityException("You do not have permission to cancel this subscription");
        }

        sub.setStatus(Status.SUSPENDED);
        subRepo.save(sub);

        // Invalidate Redis cache
        cacheService.deleteSubscription(sub.getApiKey());
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponseDto> getAllSubscriptions(String userName){
        ApiConsumer consumer = consumerRepo.findByUserName(userName);
        if (consumer == null) throw new UserNotFoundException("Requested user is not found!");

        List<Subscription> originalList = consumer.getSubscriptions();
        if (originalList == null) {
            return List.of();
        }

        return originalList
                .stream()
                .map(mapper::mapToGetSubscriptionDto)
                .toList();
    }

    private String generateApiKey(){
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
