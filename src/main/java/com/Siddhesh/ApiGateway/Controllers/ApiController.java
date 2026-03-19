package com.Siddhesh.ApiGateway.Controllers;

import com.Siddhesh.ApiGateway.Dto.ApiRegistrationBodyDto;
import com.Siddhesh.ApiGateway.Dto.ResponceApiDto;
import com.Siddhesh.ApiGateway.Services.ApiServices;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/apis")
public class ApiController {

    private final ApiServices service;

    @GetMapping("/myapi/{username}")
    public ResponseEntity<List<ResponceApiDto>> getAllApiByUsername(@PathVariable String username){
        List list = service.getAllApiByUsername(username);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @PostMapping("/register/{userName}")
    public ResponseEntity<ResponceApiDto> registerApi(@Valid @RequestBody ApiRegistrationBodyDto body, @PathVariable String userName){
        return new ResponseEntity<>(service.registerApi(body, userName), HttpStatus.CREATED);
    }
}
