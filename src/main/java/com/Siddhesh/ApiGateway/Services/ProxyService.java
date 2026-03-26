package com.Siddhesh.ApiGateway.Services;

import com.Siddhesh.ApiGateway.Entities.ApiBody;
import com.Siddhesh.ApiGateway.Entities.Status;
import com.Siddhesh.ApiGateway.Entities.Subscription;
import com.Siddhesh.ApiGateway.Repositories.SubscriptionRepo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class ProxyService {

    private final RestTemplate restTemplate;

    private final SubscriptionRepo subscriptionRepoRepo;

    public ResponseEntity<String> handleRequest(String apiKey, HttpServletRequest request){
        System.out.println(apiKey);
        Subscription subscription = subscriptionRepoRepo.findByApiKey(apiKey);
        if (subscription == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid API KEY");
        if (subscription.getStatus() != Status.ACTIVE) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Subscription not valid");

        ApiBody api = subscription.getApi();

        if (api.getStatus() != Status.ACTIVE) throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "API is suspended");

        String path = request.getServletPath().substring("/proxy".length());
        String query = request.getQueryString();
        String baseUrl = api.getLink();
        String method = request.getMethod();
        String requestUrl = baseUrl + path +(query != null ? "?" + query : "");
        HttpHeaders headers = new HttpHeaders();
        Collections.list(request.getHeaderNames())
                .forEach(headerName -> {
                    if (!headerName.equalsIgnoreCase("host")) {
                        headers.set(headerName, request.getHeader(headerName));
                    }
                });
        HttpEntity<String> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(requestUrl, HttpMethod.valueOf(method), entity, String.class);
    }
}
