package com.Siddhesh.ApiGateway.Subscriptions.Repository;

import com.Siddhesh.ApiGateway.Api.Entity.ApiBody;
import com.Siddhesh.ApiGateway.Auth.Entity.ApiConsumer;
import com.Siddhesh.ApiGateway.Subscriptions.Entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionRepo extends JpaRepository<Subscription, Long> {
    Subscription findByApiKey(String apiKey);

    boolean existsByConsumerAndApi(
            ApiConsumer consumer,
            ApiBody apiBody
    );
}
