package com.Siddhesh.ApiGateway.Auth.Repository;

import com.Siddhesh.ApiGateway.Auth.Entity.BaseUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BaseUserRepo extends JpaRepository<BaseUser, Long> {
    <optional>BaseUser findByUserName(String name);
}
