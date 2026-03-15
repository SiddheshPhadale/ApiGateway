package com.Siddhesh.ApiGateway.Repositories;

import com.Siddhesh.ApiGateway.Entities.ApiRegistrationBody;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiRepo extends JpaRepository<ApiRegistrationBody, Integer> {
}
