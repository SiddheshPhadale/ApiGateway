package com.Siddhesh.ApiGateway.Repositories;

import com.Siddhesh.ApiGateway.Entities.ApiBody;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiRepo extends JpaRepository<ApiBody, Integer> {
}
