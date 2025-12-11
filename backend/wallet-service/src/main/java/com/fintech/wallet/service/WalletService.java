package com.fintech.wallet.service;

import com.fintech.shared.dto.WalletDTO;
import com.fintech.shared.events.TransactionCompletedEvent;
import com.fintech.wallet.entity.Wallet;
import com.fintech.wallet.entity.WalletType;
import com.fintech.wallet.dto.CreateWalletRequest;
import com.fintech.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    private final WalletRepository walletRepository;

    @Transactional
    public WalletDTO createWallet(CreateWalletRequest request) {
        log.info("Creating wallet for user: {}", request.getUserId());

        Wallet wallet = Wallet.builder()
                .userId(request.getUserId())
                .name(request.getName())
                .currency(request.getCurrency())
                .balance(request.getInitialBalance())
                .walletType(WalletType.valueOf(request.getWalletType()))
                .active(true)
                .build();

        wallet = walletRepository.save(wallet);
        log.info("Wallet created successfully: {}", wallet.getId());

        return convertToDTO(wallet);
    }

    public List<WalletDTO> getWalletsByUserId(Long userId) {
        log.info("Fetching wallets for user: {}", userId);
        return walletRepository.findByUserIdAndActiveTrue(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public WalletDTO getWalletById(Long id) {
        Wallet wallet = walletRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        return convertToDTO(wallet);
    }

    @Transactional
    public WalletDTO debitWallet(Long walletId, BigDecimal amount) {
        log.info("Debiting wallet {} with amount: {}", walletId, amount);

        Wallet wallet = walletRepository.findByIdWithLock(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet = walletRepository.save(wallet);

        log.info("Wallet debited successfully. New balance: {}", wallet.getBalance());
        return convertToDTO(wallet);
    }

    @Transactional
    public WalletDTO creditWallet(Long walletId, BigDecimal amount) {
        log.info("Crediting wallet {} with amount: {}", walletId, amount);

        Wallet wallet = walletRepository.findByIdWithLock(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        wallet.setBalance(wallet.getBalance().add(amount));
        wallet = walletRepository.save(wallet);

        log.info("Wallet credited successfully. New balance: {}", wallet.getBalance());
        return convertToDTO(wallet);
    }

    @RabbitListener(queues = "transaction.completed.queue")
    public void handleTransactionCompleted(TransactionCompletedEvent event) {
        log.info("Received transaction completed event: {}", event.getTransactionId());

        try {
            // Credit destination wallet if exists
            if (event.getDestinationWalletId() != null) {
                creditWallet(event.getDestinationWalletId(), event.getAmount());
                log.info("Credited destination wallet: {}", event.getDestinationWalletId());
            }
        } catch (Exception e) {
            log.error("Error processing transaction completed event", e);
            // In production, send to DLQ or retry
        }
    }

    private WalletDTO convertToDTO(Wallet wallet) {
        return WalletDTO.builder()
                .id(wallet.getId())
                .userId(wallet.getUserId())
                .name(wallet.getName())
                .currency(wallet.getCurrency())
                .balance(wallet.getBalance())
                .walletType(wallet.getWalletType().name())
                .createdAt(wallet.getCreatedAt())
                .updatedAt(wallet.getUpdatedAt())
                .active(wallet.isActive())
                .build();
    }
}
