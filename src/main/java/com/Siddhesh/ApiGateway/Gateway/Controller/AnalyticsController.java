package com.Siddhesh.ApiGateway.Gateway.Controller;

import com.Siddhesh.ApiGateway.Api.Entity.ApiBody;
import com.Siddhesh.ApiGateway.Api.Repository.ApiRepo;
import com.Siddhesh.ApiGateway.Gateway.Entity.RequestLog;
import com.Siddhesh.ApiGateway.Gateway.Repository.RequestLogRepo;
import com.Siddhesh.ApiGateway.Gateway.Service.AnalyticsEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final RequestLogRepo requestLogRepo;
    private final AnalyticsEngineService analyticsEngineService;
    private final ApiRepo apiRepo;

    // Helper to get APIs owned by a specific provider
    private List<Long> getProviderApiIds(Long providerUserId) {
        List<ApiBody> apis = apiRepo.findAll().stream()
            .filter(api -> api.getUser() != null && api.getUser().getUserId().equals(providerUserId))
            .collect(Collectors.toList());
        return apis.stream().map(ApiBody::getApiId).collect(Collectors.toList());
    }

    // ==========================================
    // PROVIDER ENDPOINTS
    // ==========================================

    @GetMapping("/provider/metrics")
    public ResponseEntity<Map<String, Object>> getProviderMetrics(@RequestParam Long providerUserId) {
        List<Long> apiIds = getProviderApiIds(providerUserId);
        long totalRequests = apiIds.isEmpty() ? 0 : requestLogRepo.countByApiIdIn(apiIds);
        long totalErrors = apiIds.isEmpty() ? 0 : requestLogRepo.countByApiIdInAndStatusCodeGreaterThanEqual(apiIds, 400);
        
        double errorRate = totalRequests > 0 ? ((double) totalErrors / totalRequests) * 100 : 0.0;

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalRequests", totalRequests);
        metrics.put("totalErrors", totalErrors);
        metrics.put("errorRatePercentage", String.format("%.2f", errorRate));
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/provider/anomalies")
    public ResponseEntity<List<Map<String, Object>>> getAnomalies(@RequestParam Long providerUserId) {
        return ResponseEntity.ok(analyticsEngineService.detectAnomalies(getProviderApiIds(providerUserId)));
    }

    @GetMapping("/provider/chart-data")
    public ResponseEntity<List<Map<String, Object>>> getChartData(@RequestParam Long providerUserId) {
        return ResponseEntity.ok(analyticsEngineService.generateTrafficPrediction(getProviderApiIds(providerUserId)));
    }

    @GetMapping("/provider/stream")
    public ResponseEntity<List<RequestLog>> getLiveStream(@RequestParam Long providerUserId) {
        List<Long> apiIds = getProviderApiIds(providerUserId);
        if (apiIds.isEmpty()) return ResponseEntity.ok(List.of());
        return ResponseEntity.ok(requestLogRepo.findTop5ByApiIdInOrderByOccurredAtDesc(apiIds));
    }

    @GetMapping("/provider/leaderboard")
    public ResponseEntity<List<Map<String, Object>>> getLeaderboard(@RequestParam Long providerUserId) {
        List<Long> apiIds = getProviderApiIds(providerUserId);
        if (apiIds.isEmpty()) return ResponseEntity.ok(List.of());
        
        List<Object[]> topConsumers = requestLogRepo.findTopConsumersForProvider(apiIds);
        List<Map<String, Object>> response = new java.util.ArrayList<>();
        
        int limit = Math.min(topConsumers.size(), 5);
        for (int i = 0; i < limit; i++) {
            Object[] row = topConsumers.get(i);
            Map<String, Object> consumer = new HashMap<>();
            consumer.put("subId", row[0]);
            consumer.put("requestCount", row[1]);
            consumer.put("consumerName", "Consumer_" + row[0]); 
            response.add(consumer);
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/provider/slowest-endpoints")
    public ResponseEntity<List<Map<String, Object>>> getSlowestEndpoints(@RequestParam Long providerUserId) {
        List<Long> apiIds = getProviderApiIds(providerUserId);
        if (apiIds.isEmpty()) return ResponseEntity.ok(List.of());

        List<Object[]> slowest = requestLogRepo.findSlowestEndpointsForProvider(apiIds);
        List<Map<String, Object>> response = new java.util.ArrayList<>();
        
        int limit = Math.min(slowest.size(), 5);
        for (int i = 0; i < limit; i++) {
            Object[] row = slowest.get(i);
            Map<String, Object> endpoint = new HashMap<>();
            endpoint.put("endpoint", row[0]);
            endpoint.put("method", row[1]);
            endpoint.put("avgLatency", Math.round((Double) row[2]) + "ms");
            endpoint.put("requestCalls", row[3]);
            response.add(endpoint);
        }
        
        return ResponseEntity.ok(response);
    }

    // ==========================================
    // SUBSCRIBER ENDPOINTS
    // ==========================================

    @GetMapping("/subscriber/usage")
    public ResponseEntity<Map<String, Object>> getSubscriberUsage(@RequestParam Long subId) {
        long totalUsage = requestLogRepo.countBySubId(subId);
        
        Map<String, Object> usage = new HashMap<>();
        usage.put("subscriptionId", subId);
        usage.put("totalRequestsMade", totalUsage);
        
        return ResponseEntity.ok(usage);
    }
}