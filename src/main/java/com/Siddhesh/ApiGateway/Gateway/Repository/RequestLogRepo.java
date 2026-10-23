package com.Siddhesh.ApiGateway.Gateway.Repository;

import com.Siddhesh.ApiGateway.Gateway.Entity.RequestLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RequestLogRepo extends JpaRepository<RequestLog, Long> {
    
    // --- PROVIDER QUERIES (Filters traffic by the Provider's specific APIs) ---
    List<RequestLog> findByApiIdInAndOccurredAtAfter(List<Long> apiIds, LocalDateTime occurredAt);
    
    List<RequestLog> findTop5ByApiIdInOrderByOccurredAtDesc(List<Long> apiIds);

    long countByApiIdIn(List<Long> apiIds);
    
    long countByApiIdInAndStatusCodeGreaterThanEqual(List<Long> apiIds, Integer statusCode);

    @Query("SELECT r.subId, COUNT(r) as reqCount FROM RequestLog r WHERE r.apiId IN :apiIds AND r.subId IS NOT NULL GROUP BY r.subId ORDER BY reqCount DESC")
    List<Object[]> findTopConsumersForProvider(@Param("apiIds") List<Long> apiIds);

    @Query("SELECT r.route, r.httpMethod, AVG(r.latencyMs) as avgLatency, COUNT(r) as totalCalls " +
           "FROM RequestLog r WHERE r.apiId IN :apiIds GROUP BY r.route, r.httpMethod ORDER BY avgLatency DESC")
    List<Object[]> findSlowestEndpointsForProvider(@Param("apiIds") List<Long> apiIds);

    // --- SUBSCRIBER QUERIES (Filters by their unique Subscription) ---
    long countBySubId(Long subId);
}