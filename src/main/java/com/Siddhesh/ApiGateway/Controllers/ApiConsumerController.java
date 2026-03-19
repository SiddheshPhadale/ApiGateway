package com.Siddhesh.ApiGateway.Controllers;

import com.Siddhesh.ApiGateway.Dto.RegisterApiConsumerDto;
import com.Siddhesh.ApiGateway.Services.ApiConsumerService;
import com.Siddhesh.ApiGateway.Services.ApiProviderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/consumer")
public class ApiConsumerController {

    private final ApiConsumerService service;

    @PostMapping("/register")
    public ResponseEntity<HttpStatus> registerConsumer(@Valid @RequestBody RegisterApiConsumerDto consumerDto){
        return new ResponseEntity<>(service.registerUser(consumerDto));
    }
}
