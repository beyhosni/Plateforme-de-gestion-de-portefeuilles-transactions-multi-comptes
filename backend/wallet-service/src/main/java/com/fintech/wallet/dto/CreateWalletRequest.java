package com.fintech.wallet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWalletRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Wallet name is required")
    private String name;

    @NotBlank(message = "Currency is required")
    private String currency;

    @NotNull(message = "Initial balance is required")
    private BigDecimal initialBalance;

    @NotBlank(message = "Wallet type is required")
    private String walletType;
}
