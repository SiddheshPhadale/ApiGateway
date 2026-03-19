package com.Siddhesh.ApiGateway.Repositories;

import com.Siddhesh.ApiGateway.Entities.ApiConsumer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiConsumerRepo extends JpaRepository<ApiConsumer, Long> {
    ApiConsumer findByUserName(String name);
}
