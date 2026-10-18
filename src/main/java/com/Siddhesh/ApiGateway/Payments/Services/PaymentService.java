package com.Siddhesh.ApiGateway.Payments.Services;

import com.Siddhesh.ApiGateway.Api.Entity.Status;
import com.Siddhesh.ApiGateway.Auth.Entity.ApiConsumer;
import com.Siddhesh.ApiGateway.Auth.Repository.ApiConsumerRepo;
import com.Siddhesh.ApiGateway.Config.PaymentConfig;
import com.Siddhesh.ApiGateway.Exceptions.ResourceNotFoundException;
import com.Siddhesh.ApiGateway.Payments.Dtos.CreateOrderRequest;
import com.Siddhesh.ApiGateway.Payments.Dtos.CreateOrderResponse;
import com.Siddhesh.ApiGateway.Payments.Dtos.VerifyPaymentRequest;
import com.Siddhesh.ApiGateway.Payments.Dtos.VerifyPaymentResponse;
import com.Siddhesh.ApiGateway.Payments.Entities.ApiPlan;
import com.Siddhesh.ApiGateway.Payments.Entities.Transaction;
import com.Siddhesh.ApiGateway.Payments.Repositories.ApiPlanRepo;
import com.Siddhesh.ApiGateway.Payments.Repositories.TransactionRepo;
import com.Siddhesh.ApiGateway.Subscriptions.Entity.Subscription;
import com.Siddhesh.ApiGateway.Subscriptions.Repository.SubscriptionRepo;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final RazorpayClient razorpayClient;
    private final ApiPlanRepo planRepo;
    private final ApiConsumerRepo consumerRepo;
    private final SubscriptionRepo subscriptionRepo;
    private final TransactionRepo transactionRepo;
    private final PaymentConfig paymentConfig;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    // ── Step 1: Create Razorpay order ────────────────────────────────
    public CreateOrderResponse createOrder(CreateOrderRequest request) throws RazorpayException {
        ApiPlan plan = planRepo.findById(request.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));

        if (plan.getStatus() != Status.ACTIVE)
            throw new IllegalStateException("This plan is no longer available");

        // Check if already subscribed to this API
        ApiConsumer consumer = consumerRepo.findByUserName(request.getUserName());
        if (consumer == null)
            throw new ResourceNotFoundException("Consumer not found");

        boolean alreadySubscribed = subscriptionRepo
                .existsByConsumerAndApi(consumer, plan.getApi());
        if (alreadySubscribed)
            throw new IllegalStateException("Already subscribed to this API");

        // Convert price to paise (Razorpay uses smallest currency unit)
        long amountInPaise = plan.getPrice()
                .multiply(BigDecimal.valueOf(100))
                .longValue();

        // Create Razorpay order
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "sub_" + System.currentTimeMillis());
        orderRequest.put("payment_capture", 1);

        Order order = razorpayClient.orders.create(orderRequest);
        log.info("Razorpay order created: " + order.get("id"));

        CreateOrderResponse response = new CreateOrderResponse();
        response.setRazorpayOrderId(order.get("id"));
        response.setCurrency("INR");
        response.setAmount(amountInPaise);
        response.setKeyId(razorpayKeyId);
        response.setApiName(plan.getApi().getApiName());
        response.setPlanName(plan.getPlanName());

        return response;
    }

    // ── Step 2: Verify payment + activate subscription ────────────────
    @Transactional
    public VerifyPaymentResponse verifyPayment(VerifyPaymentRequest request) throws Exception {
        // Verify Razorpay signature
        if (!verifySignature(request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature())) {
            throw new SecurityException("Payment verification failed — invalid signature");
        }

        ApiPlan plan = planRepo.findById(request.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));

        ApiConsumer consumer = consumerRepo.findByUserName(request.getUserName());
        if (consumer == null)
            throw new ResourceNotFoundException("Consumer not found");

        // Generate API key
        String apiKey = "key_" + generateApiKey();

        // Create subscription
        Subscription subscription = new Subscription();
        subscription.setApi(plan.getApi());
        subscription.setConsumer(consumer);
        subscription.setPlan(plan);
        subscription.setApiKey(apiKey);
        subscription.setStatus(Status.ACTIVE);
        Subscription saved = subscriptionRepo.save(subscription);

        // Add to consumer's list
        consumer.getSubscriptions().add(saved);
        consumerRepo.save(consumer);

        // Calculate commission
        BigDecimal amountPaid = plan.getPrice();
        BigDecimal commission = amountPaid
                .multiply(BigDecimal.valueOf(paymentConfig.getCommissionRate()))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal providerAmount = amountPaid.subtract(commission);

        // Record transaction
        Transaction transaction = new Transaction();
        transaction.setSubscription(saved);
        transaction.setAmountPaid(amountPaid);
        transaction.setCommission(commission);
        transaction.setProviderAmount(providerAmount);
        transaction.setStatus(Status.ACTIVE);
        transaction.setRazorpayOrderId(request.getRazorpayOrderId());
        transaction.setRazorpayPaymentId(request.getRazorpayPaymentId());
        Transaction savedTx = transactionRepo.save(transaction);

        log.info("Subscription activated for consumer={} api={} plan={}",
                consumer.getUserName(), plan.getApi().getApiName(), plan.getPlanName());

        VerifyPaymentResponse response = new VerifyPaymentResponse();
        response.setMessage("Subscription activated! Save your API key — it won't be shown again.");
        response.setApiKey(apiKey);
        response.setPlanName(plan.getPlanName());
        response.setApiName(plan.getApi().getApiName());
        response.setRateLimitPerMinute(plan.getRateLimitPerMinute());
        response.setTransactionId(savedTx.getTransactionId().toString());

        return response;
    }

    // ── Signature verification ────────────────────────────────────────
    private boolean verifySignature(String orderId, String paymentId, String signature)
            throws Exception {
        String payload = orderId + "|" + paymentId;
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(
                razorpayKeySecret.getBytes(), "HmacSHA256");
        mac.init(secretKey);
        byte[] hash = mac.doFinal(payload.getBytes());
        String generated = HexFormat.of().formatHex(hash);
        return generated.equals(signature);
    }

    // ── API key generation ────────────────────────────────────────────
    private String generateApiKey() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
