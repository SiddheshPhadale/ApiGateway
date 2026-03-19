package com.Siddhesh.ApiGateway.Repositories;

import com.Siddhesh.ApiGateway.Entities.ApiProvider;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiOwnerRepo extends JpaRepository<ApiProvider, Long> {
    ApiProvider findByUserName(String username);
}
