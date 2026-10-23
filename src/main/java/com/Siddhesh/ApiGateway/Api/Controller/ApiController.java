package com.Siddhesh.ApiGateway.Api.Controller;

import com.Siddhesh.ApiGateway.Api.Dto.ApiRegistrationBodyDto;
import com.Siddhesh.ApiGateway.Api.Dto.ApiResponseDto;
import com.Siddhesh.ApiGateway.Api.Entity.Status;
import com.Siddhesh.ApiGateway.Api.Service.ApiServices;
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
@RequestMapping("/api/v1/apis")
public class ApiController {

    private final ApiServices service;

    @GetMapping("/myapi")
    public ResponseEntity<List<ApiResponseDto>> getAllApiByUsername(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<ApiResponseDto> list = service.getAllApiByUsername(authentication.getName());
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponseDto> registerApi(@Valid @RequestBody ApiRegistrationBodyDto body){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return new ResponseEntity<>(service.registerApi(body, authentication.getName()), HttpStatus.CREATED);
    }

    @PatchMapping("/{apiId}/status")
    public ResponseEntity<Void> updateApiStatus(@PathVariable Long apiId, @RequestParam Status status){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        service.updateApiStatus(apiId, status, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
