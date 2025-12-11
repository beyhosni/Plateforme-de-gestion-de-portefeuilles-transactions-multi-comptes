package com.fintech.transaction.repository;

import com.fintech.transaction.entity.Transaction;
import com.fintech.transaction.entity.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findBySourceWalletIdOrderByTransactionDateDesc(Long sourceWalletId);

    List<Transaction> findByDestinationWalletIdOrderByTransactionDateDesc(Long destinationWalletId);

    List<Transaction> findByStatus(TransactionStatus status);

    List<Transaction> findByTransactionDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    Optional<Transaction> findByReference(String reference);
}
