package com.Siddhesh.ApiGateway.Auth.Repository;

import com.Siddhesh.ApiGateway.Auth.Entity.ApiProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ApiOwnerRepo extends JpaRepository<ApiProvider, UUID> {
    ApiProvider findByUserName(String username);
}
