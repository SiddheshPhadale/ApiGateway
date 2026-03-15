package com.Siddhesh.ApiGateway.Controllers;

import com.Siddhesh.ApiGateway.Entities.ApiRegistrationBody;
import com.Siddhesh.ApiGateway.Services.ApiProviderServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/provider")
public class ApiProviderController {

    @Autowired
    private ApiProviderServices service;

    @PostMapping("/register/{userName}")
    public ResponseEntity<?> registerApi(@RequestBody ApiRegistrationBody body, @PathVariable String userName){
        return service.registerApi(body, userName);
    }
}
