package com.Siddhesh.ApiGateway.Controllers;

import com.Siddhesh.ApiGateway.Entities.RegisteredUser;
import com.Siddhesh.ApiGateway.Repositories.ApiOwnerRepo;
import com.Siddhesh.ApiGateway.Services.ApiOwnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/owner/")
public class ApiOwnerController {
    @Autowired
    private ApiOwnerService service;


    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisteredUser user){
        return service.registerUser(user);
    }
}
