package com.fintech.transaction.service;

import com.fintech.shared.dto.TransactionDTO;
import com.fintech.shared.dto.WalletDTO;
import com.fintech.shared.events.TransactionCompletedEvent;
import com.fintech.shared.events.TransactionFailedEvent;
import com.fintech.transaction.dto.CreateTransactionRequest;
import com.fintech.transaction.entity.Transaction;
import com.fintech.transaction.entity.TransactionStatus;
import com.fintech.transaction.entity.TransactionType;
import com.fintech.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final RabbitTemplate rabbitTemplate;
    private final WebClient.Builder webClientBuilder;

    @Value("${wallet.service.url}")
    private String walletServiceUrl;

    @Transactional
    public TransactionDTO createTransaction(CreateTransactionRequest request) {
        log.info("Creating transaction from wallet {} to wallet {}",
                request.getSourceWalletId(), request.getDestinationWalletId());

        Transaction transaction = Transaction.builder()
                .sourceWalletId(request.getSourceWalletId())
                .destinationWalletId(request.getDestinationWalletId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .transactionType(TransactionType.valueOf(request.getTransactionType()))
                .status(TransactionStatus.PENDING)
                .description(request.getDescription())
                .reference(UUID.randomUUID().toString())
                .transactionDate(LocalDateTime.now())
                .build();

        transaction = transactionRepository.save(transaction);

        // Process transaction asynchronously
        processTransaction(transaction);

        return convertToDTO(transaction);
    }

    private void processTransaction(Transaction transaction) {
        try {
            // Get wallet details to get userId
            WalletDTO sourceWallet = getWallet(transaction.getSourceWalletId());

            // Validate and debit source wallet
            debitWallet(transaction.getSourceWalletId(), transaction.getAmount());

            // Update transaction status
            transaction.setStatus(TransactionStatus.COMPLETED);
            transactionRepository.save(transaction);

            // Publish transaction completed event
            TransactionCompletedEvent event = TransactionCompletedEvent.builder()
                    .transactionId(transaction.getId())
                    .userId(sourceWallet.getUserId())
                    .sourceWalletId(transaction.getSourceWalletId())
                    .destinationWalletId(transaction.getDestinationWalletId())
                    .amount(transaction.getAmount())
                    .currency(transaction.getCurrency())
                    .transactionType(transaction.getTransactionType().name())
                    .description(transaction.getDescription())
                    .reference(transaction.getReference())
                    .completedAt(LocalDateTime.now())
                    .build();

            rabbitTemplate.convertAndSend(
                    TransactionCompletedEvent.EXCHANGE,
                    TransactionCompletedEvent.ROUTING_KEY,
                    event);

            log.info("Transaction completed successfully: {}", transaction.getId());

        } catch (Exception e) {
            log.error("Transaction failed: {}", transaction.getId(), e);

            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setFailureReason(e.getMessage());
            transactionRepository.save(transaction);

            // Publish transaction failed event
            TransactionFailedEvent event = TransactionFailedEvent.builder()
                    .transactionId(transaction.getId())
                    .userId(null) // We might not have user ID if wallet call failed
                    .sourceWalletId(transaction.getSourceWalletId())
                    .destinationWalletId(transaction.getDestinationWalletId())
                    .amount(transaction.getAmount())
                    .currency(transaction.getCurrency())
                    .transactionType(transaction.getTransactionType().name())
                    .description(transaction.getDescription())
                    .failureReason(e.getMessage())
                    .errorCode("TRANSACTION_FAILED")
                    .failedAt(LocalDateTime.now())
                    .build();

            rabbitTemplate.convertAndSend(
                    TransactionFailedEvent.EXCHANGE,
                    TransactionFailedEvent.ROUTING_KEY,
                    event);
        }
    }

    private WalletDTO getWallet(Long walletId) {
        WebClient client = webClientBuilder.baseUrl(walletServiceUrl).build();

        return client.get()
                .uri("/api/wallets/" + walletId)
                .retrieve()
                .bodyToMono(WalletDTO.class)
                .block();
    }

    private void debitWallet(Long walletId, java.math.BigDecimal amount) {
        WebClient client = webClientBuilder.baseUrl(walletServiceUrl).build();

        client.post()
                .uri("/api/wallets/" + walletId + "/debit")
                .bodyValue(amount)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    public List<TransactionDTO> getTransactionsByWallet(Long walletId) {
        log.info("Fetching transactions for wallet: {}", walletId);

        List<Transaction> sourceTransactions = transactionRepository
                .findBySourceWalletIdOrderByTransactionDateDesc(walletId);

        List<Transaction> destTransactions = transactionRepository
                .findByDestinationWalletIdOrderByTransactionDateDesc(walletId);

        // Combine and sort
        sourceTransactions.addAll(destTransactions);

        return sourceTransactions.stream()
                .distinct()
                .sorted((t1, t2) -> t2.getTransactionDate().compareTo(t1.getTransactionDate()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public TransactionDTO getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
        return convertToDTO(transaction);
    }

    private TransactionDTO convertToDTO(Transaction transaction) {
        return TransactionDTO.builder()
                .id(transaction.getId())
                .sourceWalletId(transaction.getSourceWalletId())
                .destinationWalletId(transaction.getDestinationWalletId())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .transactionType(transaction.getTransactionType().name())
                .status(transaction.getStatus().name())
                .description(transaction.getDescription())
                .category(transaction.getCategory())
                .reference(transaction.getReference())
                .transactionDate(transaction.getTransactionDate())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }
}
