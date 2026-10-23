package com.Siddhesh.ApiGateway.Payments.Repositories;

import com.Siddhesh.ApiGateway.Payments.Entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TransactionRepo extends JpaRepository<Transaction, UUID> {
    Optional<Transaction> findByRazorpayOrderId(String razorpayOrderId);
    boolean existsByRazorpayOrderId(String razorpayOrderId);
}
