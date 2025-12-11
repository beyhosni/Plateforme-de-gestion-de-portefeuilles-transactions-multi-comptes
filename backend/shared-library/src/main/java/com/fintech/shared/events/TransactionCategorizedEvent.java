package com.fintech.shared.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCategorizedEvent {
    private Long transactionId;
    private Long userId;
    private String category;
    private String subCategory;
    private Double confidenceScore; // ML confidence (0.0 - 1.0)
    private String categorizationMethod; // RULE_BASED, ML_BASED, MANUAL
    private LocalDateTime categorizedAt;

    public static final String ROUTING_KEY = "transaction.categorized";
    public static final String EXCHANGE = "categorization.events";
}
