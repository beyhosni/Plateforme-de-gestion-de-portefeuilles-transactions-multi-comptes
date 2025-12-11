package com.fintech.shared.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    private Long id;

    @NotNull(message = "Source wallet ID is required")
    private Long sourceWalletId;

    private Long destinationWalletId; // Null for withdrawals/deposits

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    private String currency;

    @NotBlank(message = "Transaction type is required")
    private String transactionType; // TRANSFER, DEPOSIT, WITHDRAWAL, PAYMENT

    private String status; // PENDING, COMPLETED, FAILED, CANCELLED

    private String description;

    private String category; // Set by categorization-service

    private String reference;

    private LocalDateTime transactionDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
