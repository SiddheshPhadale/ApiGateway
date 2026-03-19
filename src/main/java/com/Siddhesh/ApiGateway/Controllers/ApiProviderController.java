package com.Siddhesh.ApiGateway.Controllers;

import com.Siddhesh.ApiGateway.Dto.ApiRegistrationBodyDto;
import com.Siddhesh.ApiGateway.Dto.ApiProviderDto;
import com.Siddhesh.ApiGateway.Dto.ResponceApiDto;
import com.Siddhesh.ApiGateway.Services.ApiProviderService;
import com.Siddhesh.ApiGateway.Services.ApiServices;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/provider")
@RequiredArgsConstructor
public class ApiProviderController {

    private final ApiProviderService service;

    @PostMapping("/register")
    public ResponseEntity<HttpStatus> registerUser(@Valid @RequestBody ApiProviderDto user){
        HttpStatus status = service.registerUser(user);
        return new ResponseEntity<>(status);
    }
}
