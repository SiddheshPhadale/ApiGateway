package com.Siddhesh.ApiGateway.Services;

import com.Siddhesh.ApiGateway.Entities.RegisteredUser;
import com.Siddhesh.ApiGateway.Repositories.ApiOwnerRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ApiOwnerService {
    @Autowired
    private ApiOwnerRepo repo;

    public ResponseEntity<?> registerUser(RegisteredUser user){
        try {
            repo.save(user);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }
}
