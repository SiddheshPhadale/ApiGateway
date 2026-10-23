package com.Siddhesh.ApiGateway.Payments.Controllers;

import com.Siddhesh.ApiGateway.Payments.Dtos.*;
import com.Siddhesh.ApiGateway.Payments.Services.PaymentService;
import com.Siddhesh.ApiGateway.Payments.Services.PlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payment")
public class PaymentController {

    private final PaymentService paymentService;
    private final PlanService planService;

    // ── Consumer: create Razorpay order ──────────────────────────────
    @PostMapping("/create-order")
    public ResponseEntity<CreateOrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request) throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(paymentService.createOrder(request, auth.getName()));
    }

    // ── Consumer: verify payment + activate subscription ──────────────
    @PostMapping("/verify")
    public ResponseEntity<VerifyPaymentResponse> verifyPayment(
            @Valid @RequestBody VerifyPaymentRequest request) throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(paymentService.verifyPayment(request, auth.getName()));
    }

//     ── Razorpay Webhook ──────────────────────────────────────────────
//    @PostMapping("/webhook")
//    public ResponseEntity<String> handleWebhook(
//            @RequestBody String payload,
//            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {
//        paymentService.handleWebhook(payload, signature);
//        return ResponseEntity.ok("Webhook processed");
//    }

    // ── Consumer / Provider: browse plans for an API ─────────────────
    @GetMapping("/plans/{apiId}")
    public ResponseEntity<List<ApiPlanResponse>> getPlans(@PathVariable Long apiId) {
        return ResponseEntity.ok(planService.getPlansForApi(apiId));
    }

    // ── Provider: create a plan for their API ────────────────────────
    @PostMapping("/plans/{apiId}")
    public ResponseEntity<ApiPlanResponse> createPlan(
            @PathVariable Long apiId,
            @Valid @RequestBody CreatePlanRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(planService.createPlan(apiId, request, auth.getName()));
    }

    // ── Provider: deactivate a plan ───────────────────────────────────
    @DeleteMapping("/plans/{planId}")
    public ResponseEntity<Void> deactivatePlan(@PathVariable Long planId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        planService.deactivatePlan(planId, auth.getName());
        return ResponseEntity.noContent().build();
    }

    // Provider: update an existing plan
    @PutMapping("/plans/{planId}")
    public ResponseEntity<ApiPlanResponse> updatePlan(
            @PathVariable Long planId,
            @Valid @RequestBody CreatePlanRequest request) {

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        return ResponseEntity.ok(
                planService.updatePlan(planId, request, auth.getName())
        );
    }
}
