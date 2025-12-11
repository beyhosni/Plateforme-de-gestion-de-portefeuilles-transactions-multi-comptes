package com.fintech.shared.events;

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
public class TransactionFailedEvent {
    private Long transactionId;
    private Long userId;
    private Long sourceWalletId;
    private Long destinationWalletId;
    private BigDecimal amount;
    private String currency;
    private String transactionType;
    private String description;
    private String failureReason;
    private String errorCode;
    private LocalDateTime failedAt;

    public static final String ROUTING_KEY = "transaction.failed";
    public static final String EXCHANGE = "transaction.events";
}
