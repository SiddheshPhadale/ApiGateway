package com.Siddhesh.ApiGateway.Payments.Repositories;

import com.Siddhesh.ApiGateway.Payments.Entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepo extends JpaRepository<Transaction, Long> {
}
