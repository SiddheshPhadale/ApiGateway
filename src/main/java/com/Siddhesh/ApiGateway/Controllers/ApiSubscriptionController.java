package com.Siddhesh.ApiGateway.Controllers;

import com.Siddhesh.ApiGateway.Dto.SubscriptionRequestDto;
import com.Siddhesh.ApiGateway.Dto.SubscriptionResponseDto;
import com.Siddhesh.ApiGateway.Services.SubscriptionService;
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
@RequestMapping("/api/v1/subscription")
public class ApiSubscriptionController {

    private final SubscriptionService service;

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
}

