package com.Siddhesh.ApiGateway.Auth.Repository;

import com.Siddhesh.ApiGateway.Auth.Entity.BaseUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BaseUserRepo extends JpaRepository<BaseUser, UUID> {
    BaseUser findByUserName(String userName);
    boolean existsByUserName(String userName);
    boolean existsByUserEmail(String userEmail);
}
