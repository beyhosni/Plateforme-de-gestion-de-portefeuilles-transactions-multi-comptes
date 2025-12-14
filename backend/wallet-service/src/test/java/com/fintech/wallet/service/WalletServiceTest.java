package com.fintech.wallet.service;

import com.fintech.shared.dto.WalletDTO;
import com.fintech.wallet.dto.CreateWalletRequest;
import com.fintech.wallet.entity.Wallet;
import com.fintech.wallet.entity.WalletType;
import com.fintech.wallet.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletService walletService;

    @Test
    void createWallet_ShouldReturnWalletDTO() {
        // Arrange
        CreateWalletRequest request = new CreateWalletRequest();
        request.setUserId(1L);
        request.setName("My Wallet");
        request.setCurrency("USD");
        request.setInitialBalance(new BigDecimal("100.00"));
        request.setWalletType("CHECKING");

        Wallet savedWallet = Wallet.builder()
                .id(1L)
                .userId(1L)
                .name("My Wallet")
                .currency("USD")
                .balance(new BigDecimal("100.00"))
                .walletType(WalletType.CHECKING)
                .active(true)
                .build();

        when(walletRepository.save(any(Wallet.class))).thenReturn(savedWallet);

        // Act
        WalletDTO result = walletService.createWallet(request);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(new BigDecimal("100.00"), result.getBalance());
        assertEquals("CHECKING", result.getWalletType());
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void debitWallet_ShouldDecreaseBalance_WhenFundsSufficient() {
        // Arrange
        Long walletId = 1L;
        BigDecimal debitAmount = new BigDecimal("50.00");

        Wallet existingWallet = Wallet.builder()
                .id(walletId)
                .balance(new BigDecimal("100.00"))
                .walletType(WalletType.CHECKING)
                .build();

        when(walletRepository.findByIdWithLock(walletId)).thenReturn(Optional.of(existingWallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        WalletDTO result = walletService.debitWallet(walletId, debitAmount);

        // Assert
        assertEquals(new BigDecimal("50.00"), result.getBalance());
        verify(walletRepository).findByIdWithLock(walletId);
        verify(walletRepository).save(existingWallet);
    }

    @Test
    void debitWallet_ShouldThrowException_WhenFundsInsufficient() {
        // Arrange
        Long walletId = 1L;
        BigDecimal debitAmount = new BigDecimal("150.00");

        Wallet existingWallet = Wallet.builder()
                .id(walletId)
                .balance(new BigDecimal("100.00"))
                .walletType(WalletType.CHECKING)
                .build();

        when(walletRepository.findByIdWithLock(walletId)).thenReturn(Optional.of(existingWallet));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> walletService.debitWallet(walletId, debitAmount));
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void creditWallet_ShouldIncreaseBalance() {
        // Arrange
        Long walletId = 1L;
        BigDecimal creditAmount = new BigDecimal("50.00");

        Wallet existingWallet = Wallet.builder()
                .id(walletId)
                .balance(new BigDecimal("100.00"))
                .walletType(WalletType.CHECKING)
                .build();

        when(walletRepository.findByIdWithLock(walletId)).thenReturn(Optional.of(existingWallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        WalletDTO result = walletService.creditWallet(walletId, creditAmount);

        // Assert
        assertEquals(new BigDecimal("150.00"), result.getBalance());
        verify(walletRepository).findByIdWithLock(walletId);
        verify(walletRepository).save(existingWallet);
    }
}
