package com.Siddhesh.ApiGateway.Repositories;

import com.Siddhesh.ApiGateway.Entities.RegisteredUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiOwnerRepo extends JpaRepository<RegisteredUser, Long> {
    public RegisteredUser findByUserName(String username);
}
