package com.Siddhesh.ApiGateway;

import com.Siddhesh.ApiGateway.Api.Dto.ApiRegistrationBodyDto;
import com.Siddhesh.ApiGateway.Api.Dto.ApiResponseDto;
import com.Siddhesh.ApiGateway.Api.Entity.Status;
import com.Siddhesh.ApiGateway.Api.Service.ApiServices;
import com.Siddhesh.ApiGateway.Auth.Dto.RegisterationRequestDto;
import com.Siddhesh.ApiGateway.Auth.Entity.ApiConsumer;
import com.Siddhesh.ApiGateway.Auth.Repository.ApiConsumerRepo;
import com.Siddhesh.ApiGateway.Auth.Service.AuthService;
import com.Siddhesh.ApiGateway.Payments.Dtos.CreateOrderRequest;
import com.Siddhesh.ApiGateway.Payments.Dtos.CreateOrderResponse;
import com.Siddhesh.ApiGateway.Payments.Dtos.CreatePlanRequest;
import com.Siddhesh.ApiGateway.Payments.Dtos.VerifyPaymentRequest;
import com.Siddhesh.ApiGateway.Payments.Dtos.VerifyPaymentResponse;
import com.Siddhesh.ApiGateway.Payments.Entities.ApiPlan;
import com.Siddhesh.ApiGateway.Payments.Entities.Transaction;
import com.Siddhesh.ApiGateway.Payments.Repositories.ApiPlanRepo;
import com.Siddhesh.ApiGateway.Payments.Repositories.TransactionRepo;
import com.Siddhesh.ApiGateway.Payments.Services.PaymentService;
import com.Siddhesh.ApiGateway.Payments.Services.PlanService;
import com.Siddhesh.ApiGateway.Subscriptions.Repository.SubscriptionRepo;
import com.razorpay.Order;
import com.razorpay.OrderClient;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class PaymentLifecycleTests {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private TransactionRepo transactionRepo;

    @Autowired
    private ApiPlanRepo planRepo;

    @Autowired
    private SubscriptionRepo subscriptionRepo;

    @Autowired
    private ApiConsumerRepo consumerRepo;

    @Autowired
    private AuthService authService;

    @Autowired
    private ApiServices apiServices;

    @Autowired
    private PlanService planService;

    @MockitoBean
    private RazorpayClient mockRazorpayClient;

    private Long planId;
    private final String secret = "mockSecretKey1234567890abcdef";

    @BeforeEach
    void setup() throws Exception {
        RegisterationRequestDto provider = new RegisterationRequestDto();
        provider.setUserName("payProvider");
        provider.setUserEmail("payprovider@example.com");
        provider.setPassword("Password123!");
        authService.registerProvider(provider);

        RegisterationRequestDto consumer = new RegisterationRequestDto();
        consumer.setUserName("payConsumer1");
        consumer.setUserEmail("payconsumer1@example.com");
        consumer.setPassword("Password123!");
        authService.registerConsumer(consumer);

        RegisterationRequestDto consumer2 = new RegisterationRequestDto();
        consumer2.setUserName("payConsumer2");
        consumer2.setUserEmail("payconsumer2@example.com");
        consumer2.setPassword("Password123!");
        authService.registerConsumer(consumer2);

        ApiRegistrationBodyDto apiDto = new ApiRegistrationBodyDto();
        apiDto.setApiName("PaymentTestApi");
        apiDto.setLink("https://api.paymenttest.com");
        apiDto.setRateLimitPerMinute(100);
        ApiResponseDto api = apiServices.registerApi(apiDto, "payProvider");

        CreatePlanRequest planReq = new CreatePlanRequest();
        planReq.setPlanName("Pro Plan");
        planReq.setPrice(BigDecimal.valueOf(49.00));
        planReq.setRateLimitPerMinute(200);
        planReq.setDescription("Pro plan");
        var planResp = planService.createPlan(api.getApiId(), planReq, "payProvider");
        planId = planResp.getPlanId();

        // Mock Razorpay orders.create
        OrderClient mockOrders = Mockito.mock(OrderClient.class);
        mockRazorpayClient.orders = mockOrders;
        JSONObject jsonOrder = new JSONObject();
        jsonOrder.put("id", "order_test_99999");
        jsonOrder.put("amount", 4900);
        jsonOrder.put("currency", "INR");
        Order dummyOrder = new Order(jsonOrder);
        when(mockOrders.create(any(JSONObject.class))).thenReturn(dummyOrder);
    }

    private String calculateSignature(String orderId, String paymentId, String secretKey) throws Exception {
        String payload = orderId + "|" + paymentId;
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(keySpec);
        return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void testCreateOrder_CalculatesAmountServerSideAndPersistsPendingTransaction() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setPlanId(planId);

        CreateOrderResponse response = paymentService.createOrder(request, "payConsumer1");
        assertNotNull(response);
        assertEquals("order_test_99999", response.getRazorpayOrderId());
        assertEquals(4900L, response.getAmount()); // ₹49.00 = 4900 paise computed server-side
        assertEquals("INR", response.getCurrency());

        // Verify local transaction record was persisted as PENDING
        Transaction tx = transactionRepo.findByRazorpayOrderId("order_test_99999").orElseThrow();
        assertEquals(Status.PENDING, tx.getStatus());
        assertEquals("payConsumer1", tx.getConsumer().getUserName());
        assertEquals(0, new BigDecimal("49.00").compareTo(tx.getAmountPaid()));
        assertNotNull(tx.getReceipt());
    }

    @Test
    void testVerifyPayment_InvalidSignature_Rejected() throws Exception {
        CreateOrderRequest orderReq = new CreateOrderRequest();
        orderReq.setPlanId(planId);
        paymentService.createOrder(orderReq, "payConsumer1");

        VerifyPaymentRequest verifyReq = new VerifyPaymentRequest();
        verifyReq.setRazorpayOrderId("order_test_99999");
        verifyReq.setRazorpayPaymentId("pay_12345");
        verifyReq.setRazorpaySignature("invalid_signature_hash");

        assertThrows(SecurityException.class, () ->
                paymentService.verifyPayment(verifyReq, "payConsumer1"));
    }

    @Test
    void testVerifyPayment_AnotherConsumerCannotVerifyOrder() throws Exception {
        CreateOrderRequest orderReq = new CreateOrderRequest();
        orderReq.setPlanId(planId);
        paymentService.createOrder(orderReq, "payConsumer1");

        String validSig = calculateSignature("order_test_99999", "pay_12345", secret);

        VerifyPaymentRequest verifyReq = new VerifyPaymentRequest();
        verifyReq.setRazorpayOrderId("order_test_99999");
        verifyReq.setRazorpayPaymentId("pay_12345");
        verifyReq.setRazorpaySignature(validSig);

        // payConsumer2 tries to verify payConsumer1's order -> must be rejected
        SecurityException ex = assertThrows(SecurityException.class, () ->
                paymentService.verifyPayment(verifyReq, "payConsumer2"));
        assertTrue(ex.getMessage().contains("do not own this order"));
    }

    @Test
    void testVerifyPayment_SuccessAndIdempotency() throws Exception {
        CreateOrderRequest orderReq = new CreateOrderRequest();
        orderReq.setPlanId(planId);
        paymentService.createOrder(orderReq, "payConsumer1");

        String validSig = calculateSignature("order_test_99999", "pay_12345", secret);

        VerifyPaymentRequest verifyReq = new VerifyPaymentRequest();
        verifyReq.setRazorpayOrderId("order_test_99999");
        verifyReq.setRazorpayPaymentId("pay_12345");
        verifyReq.setRazorpaySignature(validSig);

        VerifyPaymentResponse response = paymentService.verifyPayment(verifyReq, "payConsumer1");
        assertNotNull(response);
        assertNotNull(response.getApiKey());
        assertTrue(response.getApiKey().startsWith("key_"));
        assertEquals("Pro Plan", response.getPlanName());
        assertEquals(200, response.getRateLimitPerMinute());

        // Verify status is ACTIVE in DB
        Transaction tx = transactionRepo.findByRazorpayOrderId("order_test_99999").orElseThrow();
        assertEquals(Status.ACTIVE, tx.getStatus());
        assertNotNull(tx.getSubscription());

        // IDEMPOTENCY: Repeat the same verification request
        VerifyPaymentResponse idempotentResp = paymentService.verifyPayment(verifyReq, "payConsumer1");
        assertNotNull(idempotentResp);
        assertEquals(response.getApiKey(), idempotentResp.getApiKey());
        assertTrue(idempotentResp.getMessage().contains("already verified"));
    }
}
