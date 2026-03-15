package com.Siddhesh.ApiGateway.Services;

import com.Siddhesh.ApiGateway.Entities.ApiRegistrationBody;
import com.Siddhesh.ApiGateway.Entities.RegisteredUser;
import com.Siddhesh.ApiGateway.Repositories.ApiOwnerRepo;
import com.Siddhesh.ApiGateway.Repositories.ApiRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ApiProviderServices {

    @Autowired
    private ApiRepo apiRepo;

    @Autowired
    private ApiOwnerRepo userRepo;

    public ResponseEntity<?> registerApi(ApiRegistrationBody body, String userName){
        try {
            RegisteredUser user = userRepo.findByUserName(userName);
            ApiRegistrationBody apiBody = apiRepo.save(body);
            user.getApiList().add(apiBody);
            userRepo.save(user);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }
}
