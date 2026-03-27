package com.Siddhesh.ApiGateway.Api.Repository;

import com.Siddhesh.ApiGateway.Api.Entity.ApiBody;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiRepo extends JpaRepository<ApiBody, Integer> {
    ApiBody findByApiName(String name);
}
