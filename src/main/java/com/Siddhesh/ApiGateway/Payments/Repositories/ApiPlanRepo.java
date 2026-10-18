package com.Siddhesh.ApiGateway.Payments.Repositories;

import com.Siddhesh.ApiGateway.Api.Entity.Status;
import com.Siddhesh.ApiGateway.Payments.Entities.ApiPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApiPlanRepo extends JpaRepository<ApiPlan, Long> {
    List<ApiPlan> findByApiApiIdAndStatus(Long apiId, Status status);
}
