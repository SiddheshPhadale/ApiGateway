package com.Siddhesh.ApiGateway.Api.Repository;

import com.Siddhesh.ApiGateway.Api.Entity.ApiBody;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ApiRepo extends JpaRepository<ApiBody, UUID> {
    java.util.List<ApiBody> findByProvider_UserId(UUID userId);
    ApiBody findByApiName(String name);
    boolean existsByApiName(String name);
}
