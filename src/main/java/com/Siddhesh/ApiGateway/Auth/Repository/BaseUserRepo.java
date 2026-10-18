package com.Siddhesh.ApiGateway.Auth.Repository;

import com.Siddhesh.ApiGateway.Auth.Entity.BaseUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BaseUserRepo extends JpaRepository<BaseUser, Long> {
    BaseUser findByUserName(String userName);
    Optional<BaseUser> findOptionalByUserName(String userName);
    boolean existsByUserName(String userName);
    boolean existsByUserEmail(String userEmail);
}
