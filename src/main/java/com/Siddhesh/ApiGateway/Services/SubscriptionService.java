package com.Siddhesh.ApiGateway.Services;

import com.Siddhesh.ApiGateway.Dto.SubscriptionRequestDto;
import com.Siddhesh.ApiGateway.Dto.SubscriptionResponseDto;
import com.Siddhesh.ApiGateway.Entities.ApiBody;
import com.Siddhesh.ApiGateway.Entities.ApiConsumer;
import com.Siddhesh.ApiGateway.Entities.Status;
import com.Siddhesh.ApiGateway.Entities.Subscription;
import com.Siddhesh.ApiGateway.Exceptions.ApiNotFound;
import com.Siddhesh.ApiGateway.Exceptions.DuplicateSubscriptionException;
import com.Siddhesh.ApiGateway.Exceptions.UserNotFoundException;
import com.Siddhesh.ApiGateway.Repositories.ApiConsumerRepo;
import com.Siddhesh.ApiGateway.Repositories.ApiRepo;
import com.Siddhesh.ApiGateway.Repositories.SubscriptionRepo;
import com.Siddhesh.ApiGateway.Services.MapperService.SubscriptionMapper;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepo subRepo;
    private final ApiRepo apiRepo;
    private final ApiConsumerRepo consumerRepo;
    private final SubscriptionMapper mapper;
    private static final SecureRandom random = new SecureRandom();

    @Transactional
    public SubscriptionResponseDto subscribe(SubscriptionRequestDto subscriptionRequestDto){
        ApiBody api = apiRepo.findByApiName(subscriptionRequestDto.getApiName());
        ApiConsumer consumer = consumerRepo.findByUserName(subscriptionRequestDto.getUserName());

        if (api == null || api.getStatus() != Status.ACTIVE) throw new ApiNotFound("Requested Api does not exist or is inactive!");

        if (consumer == null) throw new UserNotFoundException("Requested user is not found!");

        boolean isDuplicate = consumer.getSubscriptions()
                                .stream()
                                .anyMatch(subscription ->
                                    subscription.getApi().getApiName().equalsIgnoreCase(subscriptionRequestDto.getApiName())
                                );
        if (isDuplicate) throw new DuplicateSubscriptionException("Subscription already exists !");

        Subscription subscription = new Subscription();
        subscription.setApi(api);
        subscription.setConsumer(consumer);
        subscription.setStatus(Status.ACTIVE);
        subscription.setApiKey("key_" + generateApiKey());
        Subscription saveSubscription = subRepo.save(subscription);
        consumer.getSubscriptions().add(saveSubscription);
        consumerRepo.save(consumer);
        return mapper.mapToSubscriptionDto(saveSubscription);

    }
    @Transactional
    public List<SubscriptionResponseDto> getAllSubscriptions(String userName){
        ApiConsumer consumer = consumerRepo.findByUserNameWithSubscriptions(userName);
        System.out.println("Subscriptions size: " + consumer.getSubscriptions().size());

        if (consumer == null) throw new UserNotFoundException("Requested user is not found!");

        List<Subscription> originalList = consumer.getSubscriptions();

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
