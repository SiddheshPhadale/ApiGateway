package com.Siddhesh.ApiGateway.Gateway.Service;

import com.Siddhesh.ApiGateway.Api.Entity.ApiBody;
import com.Siddhesh.ApiGateway.Api.Entity.Status;
import com.Siddhesh.ApiGateway.Gateway.Dto.CachedSubscriptionDto;
import com.Siddhesh.ApiGateway.Analytics.Dtos.ApiRequestEvent;
import com.Siddhesh.ApiGateway.Payments.Entities.ApiPlan;
import com.Siddhesh.ApiGateway.Subscriptions.Entity.Subscription;
import com.Siddhesh.ApiGateway.Subscriptions.Repository.SubscriptionRepo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import java.util.Collections;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProxyService {

    private final RestTemplate restTemplate;
    private final SubscriptionRepo subscriptionRepo;
    private final CacheService cacheService;
    private final RateLimitingService rateLimitingService;
    private final UrlSecurityValidator urlSecurityValidator;
    private final AnalyticsEventProducer analyticsEventProducer;

    private static final Set<String> DISALLOWED_HEADERS = Set.of(
            "host", "api-key", "connection", "keep-alive",
            "proxy-authenticate", "proxy-authorization", "te",
            "trailer", "transfer-encoding", "upgrade", "content-length"
    );

    public ResponseEntity<byte[]> handleRequest(String apiKey, HttpServletRequest request, byte[] body) {
        long startTime = System.currentTimeMillis(); // START STOPWATCH

        CachedSubscriptionDto cachedSubscription = cacheService.getSubscription(apiKey);

        if (cachedSubscription == null) {
            Subscription subscription = subscriptionRepo.findByApiKey(apiKey);
            if (subscription == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid API KEY");
            }
            if (subscription.getStatus() != Status.ACTIVE) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Subscription not active");
            }

            ApiBody api = subscription.getApi();
            if (api == null || api.getStatus() != Status.ACTIVE) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "API is suspended or unavailable");
            }

            int effectiveRateLimit = api.getRateLimitPerMinute();
            ApiPlan plan = subscription.getPlan();
            UUID planId = null;
            String planName = null;
            if (plan != null) {
                planId = plan.getPlanId();
                planName = plan.getPlanName();
                if (plan.getRateLimitPerMinute() > 0) {
                    effectiveRateLimit = plan.getRateLimitPerMinute();
                }
            }

            cachedSubscription = CachedSubscriptionDto.builder()
                    .apiKey(apiKey)
                    .apiId(api.getApiId())
                    .targetUrl(api.getLink())
                    .subscriptionStatus(subscription.getStatus())
                    .apiStatus(api.getStatus())
                    .planId(planId)
                    .planName(planName)
                    .rateLimitPerMinute(effectiveRateLimit)
                    .subId(subscription.getSubId())
                    .build();

            cacheService.saveSubscription(apiKey, cachedSubscription);
        }

        if (cachedSubscription.getSubscriptionStatus() != Status.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Subscription is inactive or suspended");
        }
        if (cachedSubscription.getApiStatus() != Status.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "API is suspended");
        }

        int effectiveLimit = cachedSubscription.getRateLimitPerMinute();
        if (!rateLimitingService.isAllowed(cachedSubscription.getApiKey(), effectiveLimit)) {
            saveLog(cachedSubscription, request, 429, System.currentTimeMillis() - startTime, "RATE_LIMITED", true);
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded");
        }

        String destinationUrl = buildDestinationUrl(cachedSubscription.getTargetUrl(), request);

        try {
            urlSecurityValidator.validateUrl(destinationUrl);
        } catch (IllegalArgumentException e) {
            saveLog(cachedSubscription, request, 403, System.currentTimeMillis() - startTime, "BLOCKED_SSRF", false);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Destination URL blocked");
        }

        HttpHeaders headers = new HttpHeaders();
        Collections.list(request.getHeaderNames()).forEach(headerName -> {
            if (!DISALLOWED_HEADERS.contains(headerName.toLowerCase())) {
                headers.set(headerName, request.getHeader(headerName));
            }
        });

        HttpMethod httpMethod = HttpMethod.valueOf(request.getMethod());
        HttpEntity<byte[]> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<byte[]> upstreamResponse = restTemplate.exchange(
                    destinationUrl, httpMethod, entity, byte[].class
            );

            HttpHeaders responseHeaders = new HttpHeaders();
            if (upstreamResponse.getHeaders() != null) {
                upstreamResponse.getHeaders().forEach((name, values) -> {
                    if (!name.equalsIgnoreCase("transfer-encoding")) {
                        responseHeaders.put(name, values);
                    }
                });
            }

            // SUCCESS LOGGING
            saveLog(cachedSubscription, request, upstreamResponse.getStatusCode().value(), System.currentTimeMillis() - startTime, "SUCCESS", false);
            return new ResponseEntity<>(upstreamResponse.getBody(), responseHeaders, upstreamResponse.getStatusCode());

        } catch (HttpStatusCodeException ex) {
            // UPSTREAM ERROR LOGGING
            saveLog(cachedSubscription, request, ex.getStatusCode().value(), System.currentTimeMillis() - startTime, "UPSTREAM_ERROR", false);
            
            HttpHeaders responseHeaders = new HttpHeaders();
            if (ex.getResponseHeaders() != null) {
                ex.getResponseHeaders().forEach((name, values) -> {
                    if (!name.equalsIgnoreCase("transfer-encoding")) {
                        responseHeaders.put(name, values);
                    }
                });
            }
            return new ResponseEntity<>(ex.getResponseBodyAsByteArray(), responseHeaders, ex.getStatusCode());

        } catch (ResourceAccessException ex) {
            // TIMEOUT LOGGING
            saveLog(cachedSubscription, request, 504, System.currentTimeMillis() - startTime, "TIMEOUT", false);
            return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"status\":504,\"error\":\"Gateway Timeout\"}".getBytes(StandardCharsets.UTF_8));

        } catch (Exception ex) {
            // GATEWAY ERROR LOGGING
            saveLog(cachedSubscription, request, 502, System.currentTimeMillis() - startTime, "GATEWAY_ERROR", false);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"status\":502,\"error\":\"Bad Gateway\"}".getBytes(StandardCharsets.UTF_8));
        }
    }

    // Publish analytics asynchronously; analytics failure must not fail a proxied request.
    private void saveLog(CachedSubscriptionDto sub, HttpServletRequest request, int status, long latency, String outcome, boolean rateLimited) {
        try {
            ApiRequestEvent event = ApiRequestEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .timestamp(Instant.now())
                    .apiId(sub.getApiId())
                    .subscriptionId(sub.getSubId())
                    .endpoint(request.getRequestURI())
                    .httpMethod(request.getMethod())
                    .statusCode(status)
                    .responseTimeMs(latency)
                    .rateLimitHit(rateLimited)
                    .errorType("SUCCESS".equals(outcome) ? null : outcome)
                    .build();
            analyticsEventProducer.publishRequestEvent(event);
        } catch (Exception e) {
            // Keep analytics failures isolated from the gateway response path.
            log.error("Failed to enqueue analytics event for API {}", sub.getApiId(), e);
        }
    }

    private String buildDestinationUrl(String baseUrl, HttpServletRequest request) {
        String servletPath = request.getRequestURI();
        String prefix = request.getContextPath() + "/proxy";
        String path = "";
        if (servletPath.startsWith(prefix)) {
            path = servletPath.substring(prefix.length());
        }

        String cleanBaseUrl = baseUrl.replaceAll("/+$", "");
        if (!path.isEmpty() && !path.startsWith("/")) {
            path = "/" + path;
        }

        StringBuilder urlBuilder = new StringBuilder(cleanBaseUrl).append(path);
        String queryString = request.getQueryString();
        if (queryString != null && !queryString.isBlank()) {
            urlBuilder.append("?").append(queryString);
        }
        return urlBuilder.toString();
    }
}