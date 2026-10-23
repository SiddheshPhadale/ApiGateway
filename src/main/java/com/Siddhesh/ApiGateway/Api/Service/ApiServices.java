package com.Siddhesh.ApiGateway.Api.Service;

import com.Siddhesh.ApiGateway.Api.Dto.ApiRegistrationBodyDto;
import com.Siddhesh.ApiGateway.Api.Dto.ApiResponseDto;
import com.Siddhesh.ApiGateway.Api.Entity.ApiBody;
import com.Siddhesh.ApiGateway.Auth.Entity.ApiProvider;
import com.Siddhesh.ApiGateway.Api.Entity.Status;
import com.Siddhesh.ApiGateway.Exceptions.ApiNotFound;
import com.Siddhesh.ApiGateway.Exceptions.DuplicateApiNameException;
import com.Siddhesh.ApiGateway.Exceptions.UserNotFoundException;
import com.Siddhesh.ApiGateway.Auth.Repository.ApiOwnerRepo;
import com.Siddhesh.ApiGateway.Api.Repository.ApiRepo;
import com.Siddhesh.ApiGateway.Api.Mapper.ApiMapper;
import com.Siddhesh.ApiGateway.Gateway.Service.CacheService;
import com.Siddhesh.ApiGateway.Gateway.Service.UrlSecurityValidator;
import com.Siddhesh.ApiGateway.Subscriptions.Entity.Subscription;
import com.Siddhesh.ApiGateway.Subscriptions.Repository.SubscriptionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApiServices {

    private final ApiRepo apiRepo;
    private final ApiOwnerRepo userRepo;
    private final ApiMapper apiMapper;
    private final UrlSecurityValidator urlValidator;
    private final CacheService cacheService;
    private final SubscriptionRepo subscriptionRepo;

    @Transactional
    public ApiResponseDto registerApi(ApiRegistrationBodyDto apiDto, String userName){
        ApiProvider apiProvider = userRepo.findByUserName(userName);
        if (apiProvider == null) {
            throw new UserNotFoundException("User with username : " + userName + " not found.");
        }

        if (apiRepo.existsByApiName(apiDto.getApiName())) {
            throw new DuplicateApiNameException("Api with the name : " + apiDto.getApiName() + " already exists.");
        }

        urlValidator.validateUrl(apiDto.getLink());

        ApiBody body = apiMapper.mapToApiBody(apiDto);

// Keep a fallback for subscriptions that do not have an assigned plan.
// Normal subscriptions should use their plan's rate limit.
        if (apiDto.getRateLimitPerMinute() == null) {
            body.setRateLimitPerMinute(60);
        }

        body.setStatus(Status.ACTIVE);
        body.setProvider(apiProvider);
        ApiBody apiBody = apiRepo.save(body);

        if (apiProvider.getApiList() == null) {
            apiProvider.setApiList(new ArrayList<>());
        }
        apiProvider.getApiList().add(apiBody);
        userRepo.save(apiProvider);
        return apiMapper.mapToApiRegistrationResponse(apiBody);
    }

    @Transactional(readOnly = true)
    public List<ApiResponseDto> getAllApiByUsername(String name){
        ApiProvider apiProvider = userRepo.findByUserName(name);
        if (apiProvider == null) {
            throw new UserNotFoundException("User is not present!!");
        }
        List<ApiBody> originalList = apiProvider.getApiList();
        if (originalList == null) {
            return List.of();
        }
        return originalList.stream().map(apiMapper::mapToResponceApiDto).toList();
    }

    @Transactional(readOnly = true)
    public List<ApiResponseDto> getAllApi(){
        List<ApiBody> allApis = apiRepo.findAll();
        return allApis.stream().map(apiMapper::mapToResponceApiDto).toList();
    }

    @Transactional
    public void updateApiStatus(Long apiId, Status status, String providerUserName) {
        ApiBody api = apiRepo.findById(apiId)
                .orElseThrow(() -> new ApiNotFound("API not found with ID: " + apiId));

        if (api.getProvider() == null || !api.getProvider().getUserName().equals(providerUserName)) {
            throw new SecurityException("You do not own this API.");
        }

        api.setStatus(status);
        apiRepo.save(api);

        // Invalidate Redis caches for all subscriptions to this API
        List<Subscription> subs = subscriptionRepo.findByApiApiId(apiId);
        if (subs != null) {
            for (Subscription sub : subs) {
                cacheService.deleteSubscription(sub.getApiKey());
            }
        }
    }
}
