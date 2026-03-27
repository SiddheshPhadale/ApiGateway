package com.Siddhesh.ApiGateway.Auth.Repository;

import com.Siddhesh.ApiGateway.Auth.Entity.ApiProvider;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiOwnerRepo extends JpaRepository<ApiProvider, Long> {
    ApiProvider findByUserName(String username);
}
