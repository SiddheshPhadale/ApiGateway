package com.Siddhesh.ApiGateway.Gateway.Service;

import com.Siddhesh.ApiGateway.Api.Entity.ApiBody;
import com.Siddhesh.ApiGateway.Api.Entity.Status;
import com.Siddhesh.ApiGateway.Gateway.Dto.CachedSubscriptionDto;
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

    private static final Set<String> DISALLOWED_HEADERS = Set.of(
            "host",
            "api-key",
            "connection",
            "keep-alive",
            "proxy-authenticate",
            "proxy-authorization",
            "te",
            "trailer",
            "transfer-encoding",
            "upgrade",
            "content-length"
    );

    public ResponseEntity<byte[]> handleRequest(String apiKey, HttpServletRequest request, byte[] body) {

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

            // Determine effective rate limit from purchased plan or fallback to API default
            int effectiveRateLimit = api.getRateLimitPerMinute();
            ApiPlan plan = subscription.getPlan();
            Long planId = null;
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
                    .build();

            cacheService.saveSubscription(apiKey, cachedSubscription);
        }

        // Even with cache hit, verify active status
        if (cachedSubscription.getSubscriptionStatus() != Status.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Subscription is inactive or suspended");
        }
        if (cachedSubscription.getApiStatus() != Status.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "API is suspended");
        }

        // Enforce rate limiting with effective plan limit
        int effectiveLimit = cachedSubscription.getRateLimitPerMinute();
        if (!rateLimitingService.isAllowed(cachedSubscription.getApiKey(), effectiveLimit)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded (" + effectiveLimit + " req/min)");
        }

        // Construct destination URL
        String destinationUrl = buildDestinationUrl(cachedSubscription.getTargetUrl(), request);

        // SSRF Validation on destination URL
        try {
            urlSecurityValidator.validateUrl(destinationUrl);
        } catch (IllegalArgumentException e) {
            log.warn("Blocked SSRF attempt for destination URL: {}", destinationUrl);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Destination URL blocked: " + e.getMessage());
        }

        // Prepare forwarded headers
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
                    destinationUrl,
                    httpMethod,
                    entity,
                    byte[].class
            );

            HttpHeaders responseHeaders = new HttpHeaders();
            if (upstreamResponse.getHeaders() != null) {
                upstreamResponse.getHeaders().forEach((name, values) -> {
                    if (!name.equalsIgnoreCase("transfer-encoding")) {
                        responseHeaders.put(name, values);
                    }
                });
            }

            return new ResponseEntity<>(upstreamResponse.getBody(), responseHeaders, upstreamResponse.getStatusCode());
        } catch (HttpStatusCodeException ex) {
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
            log.error("Gateway upstream communication error for destination {}: {}", destinationUrl, ex.getMessage());
            return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"status\":504,\"error\":\"Gateway Timeout\",\"message\":\"Upstream service timed out or was unreachable.\"}".getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            log.error("Gateway proxy failure for destination {}: {}", destinationUrl, ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(("{\"status\":502,\"error\":\"Bad Gateway\",\"message\":\"Upstream error: " + ex.getMessage() + "\"}").getBytes(StandardCharsets.UTF_8));
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
