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
public class WalletDTO {
    private Long id;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Wallet name is required")
    private String name;

    @NotBlank(message = "Currency is required")
    private String currency;

    @NotNull(message = "Balance is required")
    private BigDecimal balance;

    private String walletType; // SAVINGS, CHECKING, INVESTMENT

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private boolean active;
}
