package com.Siddhesh.ApiGateway.Payments.Repositories;

import com.Siddhesh.ApiGateway.Api.Entity.Status;
import com.Siddhesh.ApiGateway.Payments.Entities.ApiPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApiPlanRepo extends JpaRepository<ApiPlan, UUID> {
    List<ApiPlan> findByApiApiIdAndStatus(UUID apiId, Status status);
}
