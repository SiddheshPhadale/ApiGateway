package com.Siddhesh.ApiGateway;

import com.Siddhesh.ApiGateway.Api.Dto.ApiRegistrationBodyDto;
import com.Siddhesh.ApiGateway.Api.Dto.ApiResponseDto;
import com.Siddhesh.ApiGateway.Api.Entity.Status;
import com.Siddhesh.ApiGateway.Api.Service.ApiServices;
import com.Siddhesh.ApiGateway.Auth.Dto.RegisterationRequestDto;
import com.Siddhesh.ApiGateway.Auth.Service.AuthService;
import com.Siddhesh.ApiGateway.Exceptions.DuplicateSubscriptionException;
import com.Siddhesh.ApiGateway.Payments.Dtos.CreatePlanRequest;
import com.Siddhesh.ApiGateway.Payments.Services.PlanService;
import com.Siddhesh.ApiGateway.Subscriptions.Dto.SubscriptionRequestDto;
import com.Siddhesh.ApiGateway.Subscriptions.Dto.SubscriptionResponseDto;
import com.Siddhesh.ApiGateway.Subscriptions.Entity.Subscription;
import com.Siddhesh.ApiGateway.Subscriptions.Repository.SubscriptionRepo;
import com.Siddhesh.ApiGateway.Subscriptions.Service.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class SubscriptionTests {

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private SubscriptionRepo subscriptionRepo;

    @Autowired
    private ApiServices apiServices;

    @Autowired
    private AuthService authService;

    @Autowired
    private PlanService planService;

    private Long freeApiId;
    private Long paidApiId;

    @BeforeEach
    void setup() {
        RegisterationRequestDto provider = new RegisterationRequestDto();
        provider.setUserName("subProvider");
        provider.setUserEmail("subprovider@example.com");
        provider.setPassword("Password123!");
        authService.registerProvider(provider);

        RegisterationRequestDto consumer = new RegisterationRequestDto();
        consumer.setUserName("subConsumer");
        consumer.setUserEmail("subconsumer@example.com");
        consumer.setPassword("Password123!");
        authService.registerConsumer(consumer);

        // Register Free API (no paid plans)
        ApiRegistrationBodyDto freeDto = new ApiRegistrationBodyDto();
        freeDto.setApiName("FreePublicApi");
        freeDto.setLink("https://api.free.org");
        freeDto.setRateLimitPerMinute(30);
        ApiResponseDto freeApi = apiServices.registerApi(freeDto, "subProvider");
        freeApiId = freeApi.getApiId();

        // Register Paid API with a paid plan
        ApiRegistrationBodyDto paidDto = new ApiRegistrationBodyDto();
        paidDto.setApiName("PremiumEnterpriseApi");
        paidDto.setLink("https://api.enterprise.com");
        paidDto.setRateLimitPerMinute(100);
        ApiResponseDto paidApi = apiServices.registerApi(paidDto, "subProvider");
        paidApiId = paidApi.getApiId();

        CreatePlanRequest planReq = new CreatePlanRequest();
        planReq.setPlanName("Enterprise Tier");
        planReq.setPrice(BigDecimal.valueOf(99.00));
        planReq.setRateLimitPerMinute(500);
        planReq.setDescription("Enterprise paid plan");
        planService.createPlan(paidApiId, planReq, "subProvider");
    }

    @Test
    void testFreeSubscription_Success() {
        SubscriptionRequestDto request = new SubscriptionRequestDto();
        request.setApiId(freeApiId);

        SubscriptionResponseDto response = subscriptionService.subscribe(request, "subConsumer");
        assertNotNull(response);
        assertNotNull(response.getApiKey());
        assertTrue(response.getApiKey().startsWith("key_"));
        assertEquals("subConsumer", response.getUserName());
        assertEquals("FreePublicApi", response.getApiName());

        // Verify API key is NOT exposed in general listings
        List<SubscriptionResponseDto> list = subscriptionService.getAllSubscriptions("subConsumer");
        assertEquals(1, list.size());
        assertNull(list.get(0).getApiKey(), "API key must not be exposed in subscription listings");
    }

    @Test
    void testDuplicateSubscription_Rejected() {
        SubscriptionRequestDto request = new SubscriptionRequestDto();
        request.setApiId(freeApiId);
        subscriptionService.subscribe(request, "subConsumer");

        // Attempting to subscribe again must fail
        assertThrows(DuplicateSubscriptionException.class, () ->
                subscriptionService.subscribe(request, "subConsumer"));
    }

    @Test
    void testPaidApiDirectSubscription_RejectedAsPaymentBypass() {
        SubscriptionRequestDto request = new SubscriptionRequestDto();
        request.setApiId(paidApiId);

        // Calling direct subscribe on an API with paid plans must be rejected
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                subscriptionService.subscribe(request, "subConsumer"));
        assertTrue(ex.getMessage().contains("paid plan"));
    }

    @Test
    void testCancelSubscription_UpdatesStatus() {
        SubscriptionRequestDto request = new SubscriptionRequestDto();
        request.setApiId(freeApiId);
        SubscriptionResponseDto response = subscriptionService.subscribe(request, "subConsumer");

        subscriptionService.cancelSubscription(response.getSubId(), "subConsumer");

        Subscription updated = subscriptionRepo.findById(response.getSubId()).orElseThrow();
        assertEquals(Status.SUSPENDED, updated.getStatus());
    }
}
