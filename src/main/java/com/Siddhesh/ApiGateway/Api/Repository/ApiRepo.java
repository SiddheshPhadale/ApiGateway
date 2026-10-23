package com.Siddhesh.ApiGateway.Api.Repository;

import com.Siddhesh.ApiGateway.Api.Entity.ApiBody;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiRepo extends JpaRepository<ApiBody, Long> {
    java.util.List<ApiBody> findByProvider_UserId(Long userId);
    ApiBody findByApiName(String name);
    boolean existsByApiName(String name);
}
