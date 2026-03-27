package com.Siddhesh.ApiGateway.Auth.Repository;

import com.Siddhesh.ApiGateway.Auth.Entity.BaseUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BaseUserRepo extends JpaRepository<BaseUser, Long> {
    BaseUser findByUserName(String name);
}
