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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.Siddhesh.ApiGateway.Gateway.Service.CacheService;
import com.Siddhesh.ApiGateway.Subscriptions.Repository.SubscriptionRepo;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final ApiPlanRepo planRepo;
    private final ApiRepo apiRepo;
    private final ApiOwnerRepo providerRepo;
    private final CacheService cacheService;
    private final SubscriptionRepo subscriptionRepo;

    // ── Provider creates a plan ───────────────────────────────────────
    @Transactional
    public ApiPlanResponse createPlan(Long apiId, CreatePlanRequest request, String providerUserName) {
        ApiBody api = apiRepo.findById(apiId)
                .orElseThrow(() -> new ResourceNotFoundException("API not found"));

        ApiProvider provider = providerRepo.findByUserName(providerUserName);
        if (provider == null)
            throw new ResourceNotFoundException("Provider not found");

        if (api.getProvider() == null || !api.getProvider().getUserName().equals(providerUserName)) {
            throw new SecurityException("You do not own the API with ID: " + apiId);
        }

        ApiPlan plan = new ApiPlan();
        plan.setApi(api);
        plan.setPlanName(request.getPlanName());
        plan.setPrice(request.getPrice());
        plan.setRateLimitPerMinute(request.getRateLimitPerMinute());
        plan.setDescription(request.getDescription());
        plan.setStatus(Status.ACTIVE);

        ApiPlan saved = planRepo.save(plan);
        return toResponse(saved);
    }

    // ── Get all plans for an API ──────────────────────────────────────
    @Transactional(readOnly = true)
    public List<ApiPlanResponse> getPlansForApi(Long apiId) {
        return planRepo.findByApiApiIdAndStatus(apiId, Status.ACTIVE)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ── Provider deactivates a plan ───────────────────────────────────
    @Transactional
    public void deactivatePlan(Long planId, String providerUserName) {
        ApiPlan plan = planRepo.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));

        if (plan.getApi() == null || plan.getApi().getProvider() == null ||
                !plan.getApi().getProvider().getUserName().equals(providerUserName)) {
            throw new SecurityException("You do not own the API associated with this plan");
        }

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

    @Transactional
    public ApiPlanResponse updatePlan(Long planId, CreatePlanRequest request, String username) {

        // Find the existing plan
        ApiPlan plan = planRepo.findById(planId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Plan not found"));

        // Verify that the logged-in provider owns the API
        ApiBody api = plan.getApi();

        if (api.getProvider() == null ||
                !api.getProvider().getUserName().equals(username)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You are not authorized to update this plan");
        }

        // Update plan details
        plan.setPlanName(request.getPlanName());
        plan.setPrice(request.getPrice());
        plan.setRateLimitPerMinute(request.getRateLimitPerMinute());
        plan.setDescription(request.getDescription());

        // Save and return the updated plan
        ApiPlan updatedPlan = planRepo.save(plan);

// Invalidate cached subscriptions for consumers of this API.
// The next request reloads the current plan rate limit from the database.
        List<com.Siddhesh.ApiGateway.Subscriptions.Entity.Subscription> subscriptions =
                subscriptionRepo.findByApiApiId(api.getApiId());

        if (subscriptions != null) {
            for (var subscription : subscriptions) {
                cacheService.deleteSubscription(subscription.getApiKey());
            }
        }

        return toResponse(updatedPlan);
    }
}
