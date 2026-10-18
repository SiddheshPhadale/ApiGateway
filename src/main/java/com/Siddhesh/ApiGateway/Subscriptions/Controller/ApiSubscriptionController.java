package com.Siddhesh.ApiGateway.Subscriptions.Controller;

import com.Siddhesh.ApiGateway.Subscriptions.Dto.SubscriptionRequestDto;
import com.Siddhesh.ApiGateway.Subscriptions.Dto.SubscriptionResponseDto;
import com.Siddhesh.ApiGateway.Subscriptions.Service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.Siddhesh.ApiGateway.Api.Service.ApiServices;
import com.Siddhesh.ApiGateway.Api.Dto.ApiResponseDto;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/subscription")
public class ApiSubscriptionController {

    private final SubscriptionService service;

    private final ApiServices ApiServices;

    @PostMapping
    public ResponseEntity<SubscriptionResponseDto> subscribe(@Valid @RequestBody SubscriptionRequestDto subscriptionRequestDto){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return new ResponseEntity<>(service.subscribe(subscriptionRequestDto), HttpStatus.CREATED);
    }

    @GetMapping()
    public ResponseEntity<List<SubscriptionResponseDto>> getAllSubscriptions(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return new ResponseEntity<>(service.getAllSubscriptions(authentication.getName()), HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ApiResponseDto>> getAllApi(){
        return ResponseEntity.ok(ApiServices.getAllApi());
    }
}

