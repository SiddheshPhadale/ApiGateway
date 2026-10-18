package com.Siddhesh.ApiGateway.Gateway.Service;

import com.Siddhesh.ApiGateway.Api.Entity.ApiBody;
import com.Siddhesh.ApiGateway.Api.Entity.Status;
import com.Siddhesh.ApiGateway.Gateway.Dto.CachedSubscriptionDto;
import com.Siddhesh.ApiGateway.Subscriptions.Entity.Subscription;
import com.Siddhesh.ApiGateway.Subscriptions.Repository.SubscriptionRepo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import java.util.Collections;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProxyService {

    private final RestTemplate restTemplate;

    private final SubscriptionRepo subscriptionRepo;

    private final CacheService cacheService;

    private final RateLimitingService rateLimitingService;

    public ResponseEntity<String> handleRequest(String apiKey, HttpServletRequest request){

        CachedSubscriptionDto cachedSubscription = cacheService.getSubscription(apiKey);

        if(cachedSubscription == null) {
            Subscription subscription = subscriptionRepo.findByApiKey(apiKey);
            if (subscription == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid API KEY");
            if (subscription.getStatus() != Status.ACTIVE) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Subscription not valid");

            ApiBody api = subscription.getApi();
            if (api.getStatus() != Status.ACTIVE) throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "API is suspended");


            cachedSubscription = new CachedSubscriptionDto();

            cachedSubscription.setApiKey(apiKey);
            cachedSubscription.setSubscriptionStatus(subscription.getStatus());
            cachedSubscription.setApiStatus(api.getStatus());
            cachedSubscription.setTargetUrl(api.getLink());
            cachedSubscription.setRateLimitPerMinute(api.getRateLimitPerMinute());

            cacheService.saveSubscription(apiKey, cachedSubscription);
        }

        if (!rateLimitingService.isAllowed(cachedSubscription.getApiKey(),
                cachedSubscription.getRateLimitPerMinute())){
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded");
        }

        String path = request.getServletPath().substring("/proxy".length());
        String query = request.getQueryString();
        String baseUrl = cachedSubscription.getTargetUrl();
        String method = request.getMethod();
        String requestUrl = baseUrl + path +(query != null ? "?" + query : "");
        HttpHeaders headers = new HttpHeaders();
        Collections.list(request.getHeaderNames())
                .forEach(headerName -> {
                    if (!headerName.equalsIgnoreCase("host") &&
                            !headerName.equalsIgnoreCase("api-key")) {
                        headers.set(headerName, request.getHeader(headerName));
                    }
                });
        HttpEntity<String> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(requestUrl, HttpMethod.valueOf(method), entity, String.class);
    }
}
