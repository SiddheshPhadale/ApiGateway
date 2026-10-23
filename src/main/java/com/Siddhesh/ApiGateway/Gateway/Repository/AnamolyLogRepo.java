package com.Siddhesh.ApiGateway.Gateway.Repository;

import com.Siddhesh.ApiGateway.Gateway.Entity.AnomalyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnomalyLogRepo extends JpaRepository<AnomalyLog, Long> {
}