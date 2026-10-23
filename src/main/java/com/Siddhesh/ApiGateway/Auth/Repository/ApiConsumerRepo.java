package com.Siddhesh.ApiGateway.Auth.Repository;

import com.Siddhesh.ApiGateway.Auth.Entity.ApiConsumer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiConsumerRepo extends JpaRepository<ApiConsumer, Long> {
    ApiConsumer findByUserName(String name);
}
