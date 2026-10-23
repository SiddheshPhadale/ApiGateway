package com.Siddhesh.ApiGateway.Gateway.Controller;

import com.Siddhesh.ApiGateway.Gateway.Service.ProxyService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/proxy")
public class ProxyController {

    private final ProxyService service;

    @RequestMapping("/**")
    public ResponseEntity<byte[]> handleProxyRequest(
            @RequestHeader(value = "api-key", required = false) String apiKey,
            @RequestBody(required = false) byte[] body,
            HttpServletRequest request) throws IOException {

        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing 'api-key' header");
        }

        return service.handleRequest(apiKey.trim(), request, body);
    }
}
