package com.Siddhesh.ApiGateway.Auth.Controller;

import com.Siddhesh.ApiGateway.Auth.Dto.LoginResponseDto;
import com.Siddhesh.ApiGateway.Auth.Dto.RegisterationRequestDto;
import com.Siddhesh.ApiGateway.Auth.Dto.LoginDto;
import com.Siddhesh.ApiGateway.Auth.Dto.RegistrationResponseDto;
import com.Siddhesh.ApiGateway.Auth.Service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService service;

    private final AuthenticationManager manager;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginDto loginDto){
        manager.authenticate(new UsernamePasswordAuthenticationToken(loginDto.getUserName(), loginDto.getPassword()));
        return new ResponseEntity<>(service.login(loginDto), HttpStatus.OK);
    }

    @PostMapping("/provider/register")
    public ResponseEntity<RegistrationResponseDto> registerProvider(@Valid @RequestBody RegisterationRequestDto user){
        return new ResponseEntity<>(service.registerProvider(user), HttpStatus.CREATED);
    }

    @PostMapping("/consumer/register")
    public ResponseEntity<RegistrationResponseDto> registerConsumer(@Valid @RequestBody RegisterationRequestDto consumerDto){
        return new ResponseEntity<>(service.registerConsumer(consumerDto), HttpStatus.CREATED);
    }
}
