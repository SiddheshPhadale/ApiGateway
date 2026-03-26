package com.Siddhesh.ApiGateway.Controllers;

import com.Siddhesh.ApiGateway.Services.ProxyService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/proxy")
public class ProxyController {

    private final ProxyService service;

    @RequestMapping("/**")
    public ResponseEntity<String> getRequest(@RequestHeader("api-key") String apiKey, HttpServletRequest request) throws IOException {
        return service.handleRequest(apiKey, request);
    }
}
