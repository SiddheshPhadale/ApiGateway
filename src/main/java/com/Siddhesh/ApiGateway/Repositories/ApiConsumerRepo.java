package com.Siddhesh.ApiGateway.Repositories;

import com.Siddhesh.ApiGateway.Dto.SubscriptionResponseDto;
import com.Siddhesh.ApiGateway.Entities.ApiConsumer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApiConsumerRepo extends JpaRepository<ApiConsumer, Long> {
    ApiConsumer findByUserName(String name);
}
