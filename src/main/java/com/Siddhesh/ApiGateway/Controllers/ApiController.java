package com.Siddhesh.ApiGateway.Controllers;

import com.Siddhesh.ApiGateway.Dto.ApiRegistrationBodyDto;
import com.Siddhesh.ApiGateway.Dto.ApiResponseDto;
import com.Siddhesh.ApiGateway.Services.ApiServices;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.parser.Authorization;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/apis")
public class ApiController {

    private final AuthenticationManager manager;

    private final ApiServices service;

    @GetMapping("/myapi")
    public ResponseEntity<List<ApiResponseDto>> getAllApiByUsername(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println(authentication.getAuthorities());
        List list = service.getAllApiByUsername(authentication.getName());
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponseDto> registerApi(@Valid @RequestBody ApiRegistrationBodyDto body){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return new ResponseEntity<>(service.registerApi(body, authentication.getName()), HttpStatus.CREATED);
    }
}
