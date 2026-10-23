package com.Siddhesh.ApiGateway.Auth.Repository;

import com.Siddhesh.ApiGateway.Auth.Entity.ApiConsumer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ApiConsumerRepo extends JpaRepository<ApiConsumer, UUID> {
    ApiConsumer findByUserName(String name);
}
