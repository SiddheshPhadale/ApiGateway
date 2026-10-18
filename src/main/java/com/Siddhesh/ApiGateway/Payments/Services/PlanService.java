package com.Siddhesh.ApiGateway.Payments.Services;

import com.Siddhesh.ApiGateway.Api.Entity.ApiBody;
import com.Siddhesh.ApiGateway.Api.Entity.Status;
import com.Siddhesh.ApiGateway.Api.Repository.ApiRepo;
import com.Siddhesh.ApiGateway.Auth.Entity.ApiProvider;
import com.Siddhesh.ApiGateway.Auth.Repository.ApiOwnerRepo;
import com.Siddhesh.ApiGateway.Exceptions.ResourceNotFoundException;
import com.Siddhesh.ApiGateway.Payments.Dtos.ApiPlanResponse;
import com.Siddhesh.ApiGateway.Payments.Dtos.CreatePlanRequest;
import com.Siddhesh.ApiGateway.Payments.Entities.ApiPlan;
import com.Siddhesh.ApiGateway.Payments.Repositories.ApiPlanRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final ApiPlanRepo planRepo;
    private final ApiRepo apiRepo;
    private final ApiOwnerRepo providerRepo;

    // ── Provider creates a plan ───────────────────────────────────────
    @Transactional
    public ApiPlanResponse createPlan(Long apiId, CreatePlanRequest request, String providerUserName) {
        ApiBody api = apiRepo.findById(Math.toIntExact(apiId))
                .orElseThrow(() -> new ResourceNotFoundException("API not found"));

        ApiProvider provider = providerRepo.findByUserName(providerUserName);
        if (provider == null)
            throw new ResourceNotFoundException("Provider not found");

        ApiPlan plan = new ApiPlan();
        plan.setApi(api);
        plan.setPlanName(request.getPlanName());
        plan.setPrice(BigDecimal.valueOf(request.getPrice()));
        plan.setRateLimitPerMinute(request.getRateLimitPerMinute());
        plan.setDescription(request.getDescription());
        plan.setStatus(Status.ACTIVE);

        ApiPlan saved = planRepo.save(plan);
        return toResponse(saved);
    }

    // ── Get all plans for an API ──────────────────────────────────────
    @Transactional(readOnly = true)
    public List<ApiPlanResponse> getPlansForApi(Long apiId) {
        return planRepo.findByApiApiIdAndStatus(Math.toIntExact(apiId), Status.ACTIVE)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ── Provider deactivates a plan ───────────────────────────────────
    @Transactional
    public void deactivatePlan(Long planId, String providerUserName) {
        ApiPlan plan = planRepo.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));
        plan.setStatus(Status.SUSPENDED);
        planRepo.save(plan);
    }

    // ── Helper ────────────────────────────────────────────────────────
    private ApiPlanResponse toResponse(ApiPlan plan) {
        ApiPlanResponse response = new ApiPlanResponse();
        response.setPlanId(plan.getPlanId());
        response.setPlanName(plan.getPlanName());
        response.setDescription(plan.getDescription());
        response.setPrice(plan.getPrice().doubleValue());
        response.setRateLimitPerMinute(plan.getRateLimitPerMinute());
        response.setStatus(plan.getStatus().name());
        return response;
    }
}
