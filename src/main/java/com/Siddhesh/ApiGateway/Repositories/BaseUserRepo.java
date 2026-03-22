package com.Siddhesh.ApiGateway.Repositories;

import com.Siddhesh.ApiGateway.Entities.BaseUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BaseUserRepo extends JpaRepository<BaseUser, Long> {
    BaseUser findByUserName(String name);
}
