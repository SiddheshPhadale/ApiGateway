package com.Siddhesh.ApiGateway.Payments.Repositories;

import com.Siddhesh.ApiGateway.Payments.Entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionRepo extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByRazorpayOrderId(String razorpayOrderId);
    boolean existsByRazorpayOrderId(String razorpayOrderId);
}
