package com.Siddhesh.ApiGateway.Payments.Services;

import com.Siddhesh.ApiGateway.Api.Entity.ApiBody;
import com.Siddhesh.ApiGateway.Api.Entity.Status;
import com.Siddhesh.ApiGateway.Auth.Entity.ApiConsumer;
import com.Siddhesh.ApiGateway.Auth.Repository.ApiConsumerRepo;
import com.Siddhesh.ApiGateway.Config.PaymentConfig;
import com.Siddhesh.ApiGateway.Exceptions.DuplicateSubscriptionException;
import com.Siddhesh.ApiGateway.Exceptions.ResourceNotFoundException;
import com.Siddhesh.ApiGateway.Exceptions.UserNotFoundException;
import com.Siddhesh.ApiGateway.Gateway.Dto.CachedSubscriptionDto;
import com.Siddhesh.ApiGateway.Gateway.Service.CacheService;
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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

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
    private final CacheService cacheService;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Value("${razorpay.key.id:}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret:}")
    private String razorpayKeySecret;

    @Value("${razorpay.webhook.secret:}")
    private String razorpayWebhookSecret;

    // ── Step 1: Create Razorpay order ────────────────────────────────
    @Transactional
    public CreateOrderResponse createOrder(CreateOrderRequest request, String authenticatedUsername) throws RazorpayException {
        ApiPlan plan = planRepo.findById(request.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with ID: " + request.getPlanId()));

        if (plan.getStatus() != Status.ACTIVE) {
            throw new IllegalStateException("This plan is no longer available");
        }

        ApiConsumer consumer = consumerRepo.findByUserName(authenticatedUsername);
        if (consumer == null) {
            throw new UserNotFoundException("Consumer not found: " + authenticatedUsername);
        }

        boolean alreadySubscribed = subscriptionRepo.existsByConsumerAndApi(consumer, plan.getApi());
        if (alreadySubscribed) {
            throw new DuplicateSubscriptionException("Already subscribed to this API");
        }

        // Amount calculation in integer minor units (paise)
        BigDecimal planPrice = plan.getPrice();
        long amountInPaise = planPrice.multiply(BigDecimal.valueOf(100)).longValue();
        String receipt = "rcpt_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        // Call Razorpay
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", receipt);
        orderRequest.put("payment_capture", 1);

        Order order = razorpayClient.orders.create(orderRequest);
        String razorpayOrderId = order.get("id");
        log.info("Razorpay order created: {} for user: {}", razorpayOrderId, authenticatedUsername);

        // Revenue allocation: platform commission and provider amount
        BigDecimal commissionRate = BigDecimal.valueOf(paymentConfig.getCommissionRate());
        BigDecimal commission = planPrice.multiply(commissionRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal providerAmount = planPrice.subtract(commission);

        // Persist local pending order/transaction record
        Transaction transaction = new Transaction();
        transaction.setConsumer(consumer);
        transaction.setPlan(plan);
        transaction.setAmountPaid(planPrice);
        transaction.setCommission(commission);
        transaction.setProviderAmount(providerAmount);
        transaction.setCurrency("INR");
        transaction.setReceipt(receipt);
        transaction.setRazorpayOrderId(razorpayOrderId);
        transaction.setStatus(Status.PENDING);
        transactionRepo.save(transaction);

        CreateOrderResponse response = new CreateOrderResponse();
        response.setRazorpayOrderId(razorpayOrderId);
        response.setCurrency("INR");
        response.setAmount(amountInPaise);
        response.setKeyId(razorpayKeyId);
        response.setApiName(plan.getApi().getApiName());
        response.setPlanName(plan.getPlanName());

        return response;
    }

    // ── Step 2: Verify payment + activate subscription ────────────────
    @Transactional
    public VerifyPaymentResponse verifyPayment(VerifyPaymentRequest request, String authenticatedUsername) throws Exception {
        // Find stored local order by Razorpay order ID
        Transaction transaction = transactionRepo.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("No order found for Razorpay Order ID: " + request.getRazorpayOrderId()));

        // Enforce ownership: Authenticated user must own the order
        if (!transaction.getConsumer().getUserName().equals(authenticatedUsername)) {
            throw new SecurityException("Unauthorized: You do not own this order.");
        }

        // Idempotency: Return stable result if payment was already verified
        if (transaction.getStatus() == Status.ACTIVE && transaction.getSubscription() != null) {
            Subscription existingSub = transaction.getSubscription();
            VerifyPaymentResponse response = new VerifyPaymentResponse();
            response.setMessage("Payment already verified and subscription is active.");
            response.setApiKey(existingSub.getApiKey());
            response.setPlanName(transaction.getPlan().getPlanName());
            response.setApiName(transaction.getPlan().getApi().getApiName());
            response.setRateLimitPerMinute(transaction.getPlan().getRateLimitPerMinute());
            response.setTransactionId(transaction.getTransactionId().toString());
            return response;
        }

        // Verify Razorpay signature using constant-time check
        if (!verifySignature(request.getRazorpayOrderId(), request.getRazorpayPaymentId(), request.getRazorpaySignature())) {
            throw new SecurityException("Payment verification failed — invalid signature");
        }

        ApiPlan plan = transaction.getPlan();
        ApiConsumer consumer = transaction.getConsumer();
        ApiBody api = plan.getApi();

        // Generate secure API key
        String apiKey = "key_" + generateApiKey();

        // Create or activate subscription
        Subscription subscription = subscriptionRepo.findByConsumerAndApi(consumer, api).orElse(null);
        if (subscription == null) {
            subscription = new Subscription();
            subscription.setApi(api);
            subscription.setConsumer(consumer);
            subscription.setPlan(plan);
            subscription.setApiKey(apiKey);
            subscription.setStatus(Status.ACTIVE);
            subscription = subscriptionRepo.save(subscription);

            if (consumer.getSubscriptions() == null) {
                consumer.setSubscriptions(new ArrayList<>());
            }
            consumer.getSubscriptions().add(subscription);
            consumerRepo.save(consumer);
        } else {
            subscription.setPlan(plan);
            subscription.setStatus(Status.ACTIVE);
            subscription = subscriptionRepo.save(subscription);
        }

        // Update transaction to ACTIVE and link subscription
        transaction.setSubscription(subscription);
        transaction.setRazorpayPaymentId(request.getRazorpayPaymentId());
        transaction.setStatus(Status.ACTIVE);
        Transaction savedTx = transactionRepo.save(transaction);

        // Pre-warm cache for gateway proxy
        CachedSubscriptionDto cachedDto = CachedSubscriptionDto.builder()
                .apiKey(subscription.getApiKey())
                .apiId(api.getApiId())
                .targetUrl(api.getLink())
                .subscriptionStatus(subscription.getStatus())
                .apiStatus(api.getStatus())
                .planId(plan.getPlanId())
                .planName(plan.getPlanName())
                .rateLimitPerMinute(plan.getRateLimitPerMinute())
                .build();
        cacheService.saveSubscription(subscription.getApiKey(), cachedDto);

        log.info("Subscription activated for consumer={} api={} plan={}",
                consumer.getUserName(), api.getApiName(), plan.getPlanName());

        VerifyPaymentResponse response = new VerifyPaymentResponse();
        response.setMessage("Subscription activated! Save your API key — it won't be shown again.");
        response.setApiKey(subscription.getApiKey());
        response.setPlanName(plan.getPlanName());
        response.setApiName(api.getApiName());
        response.setRateLimitPerMinute(plan.getRateLimitPerMinute());
        response.setTransactionId(savedTx.getTransactionId().toString());

        return response;
    }

    // ── Webhook processing for recovery / async confirmation ──────────
    @Transactional
    public void handleWebhook(String payload, String signature) {
        if (signature == null || signature.isBlank()) {
            throw new SecurityException("Missing webhook signature header");
        }

        String secret = (razorpayWebhookSecret != null && !razorpayWebhookSecret.isBlank())
                ? razorpayWebhookSecret : razorpayKeySecret;

        if (!verifyHmac(payload, signature, secret)) {
            throw new SecurityException("Invalid webhook signature");
        }

        JSONObject event = new JSONObject(payload);
        String eventType = event.optString("event");

        if ("order.paid".equals(eventType) || "payment.captured".equals(eventType)) {
            JSONObject paymentPayload = event.optJSONObject("payload");
            if (paymentPayload != null) {
                JSONObject paymentEntity = paymentPayload.optJSONObject("payment").optJSONObject("entity");
                String orderId = paymentEntity.optString("order_id");
                String paymentId = paymentEntity.optString("id");

                transactionRepo.findByRazorpayOrderId(orderId).ifPresent(tx -> {
                    if (tx.getStatus() == Status.PENDING) {
                        ApiPlan plan = tx.getPlan();
                        ApiConsumer consumer = tx.getConsumer();
                        ApiBody api = plan.getApi();

                        Subscription subscription = subscriptionRepo.findByConsumerAndApi(consumer, api).orElseGet(() -> {
                            Subscription newSub = new Subscription();
                            newSub.setApi(api);
                            newSub.setConsumer(consumer);
                            newSub.setPlan(plan);
                            newSub.setApiKey("key_" + generateApiKey());
                            newSub.setStatus(Status.ACTIVE);
                            return subscriptionRepo.save(newSub);
                        });

                        tx.setSubscription(subscription);
                        tx.setRazorpayPaymentId(paymentId);
                        tx.setStatus(Status.ACTIVE);
                        transactionRepo.save(tx);
                        log.info("Payment confirmed via webhook for orderId={}", orderId);
                    }
                });
            }
        }
    }

    // ── Signature verification with constant-time equality check ──────
    private boolean verifySignature(String orderId, String paymentId, String signature) {
        if (orderId == null || paymentId == null || signature == null) return false;
        try {
            String payload = orderId + "|" + paymentId;
            return verifyHmac(payload, signature, razorpayKeySecret);
        } catch (Exception e) {
            log.error("Error during signature verification: {}", e.getMessage());
            return false;
        }
    }

    private boolean verifyHmac(String data, String signature, String secret) {
        if (secret == null || secret.isBlank()) return false;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            String generated = HexFormat.of().formatHex(hash);
            return MessageDigest.isEqual(generated.getBytes(StandardCharsets.UTF_8), signature.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return false;
        }
    }

    // ── Secure API key generation ─────────────────────────────────────
    private String generateApiKey() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
