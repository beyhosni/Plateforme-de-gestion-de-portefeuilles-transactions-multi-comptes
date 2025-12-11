package com.fintech.categorization.service;

import com.fintech.categorization.document.CategoryRule;
import com.fintech.categorization.repository.CategoryRuleRepository;
import com.fintech.shared.events.TransactionCategorizedEvent;
import com.fintech.shared.events.TransactionCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategorizationService {

    private final CategoryRuleRepository categoryRuleRepository;
    private final RabbitTemplate rabbitTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeDefaultRules() {
        if (categoryRuleRepository.count() == 0) {
            log.info("Initializing default categorization rules");

            List<CategoryRule> defaultRules = Arrays.asList(
                    CategoryRule.builder()
                            .category("Food & Dining")
                            .subCategory("Restaurants")
                            .keywords(Arrays.asList("restaurant", "cafe", "pizza", "burger", "food"))
                            .priority(10.0)
                            .active(true)
                            .createdAt(LocalDateTime.now())
                            .build(),
                    CategoryRule.builder()
                            .category("Transport")
                            .subCategory("Taxi & Rideshare")
                            .keywords(Arrays.asList("uber", "lyft", "taxi", "cab"))
                            .priority(10.0)
                            .active(true)
                            .createdAt(LocalDateTime.now())
                            .build(),
                    CategoryRule.builder()
                            .category("Shopping")
                            .subCategory("Online Shopping")
                            .keywords(Arrays.asList("amazon", "ebay", "shop", "store"))
                            .priority(8.0)
                            .active(true)
                            .createdAt(LocalDateTime.now())
                            .build(),
                    CategoryRule.builder()
                            .category("Bills & Utilities")
                            .subCategory("Internet")
                            .keywords(Arrays.asList("internet", "broadband", "wifi"))
                            .priority(9.0)
                            .active(true)
                            .createdAt(LocalDateTime.now())
                            .build(),
                    CategoryRule.builder()
                            .category("Entertainment")
                            .subCategory("Streaming")
                            .keywords(Arrays.asList("netflix", "spotify", "hulu", "streaming"))
                            .priority(9.0)
                            .active(true)
                            .createdAt(LocalDateTime.now())
                            .build());

            categoryRuleRepository.saveAll(defaultRules);
            log.info("Default rules initialized successfully");
        }
    }

    @RabbitListener(queues = "transaction.completed.queue")
    public void handleTransactionCompleted(TransactionCompletedEvent event) {
        log.info("Received transaction for categorization: {}", event.getTransactionId());

        try {
            String description = event.getDescription() != null ? event.getDescription().toLowerCase() : "";

            List<CategoryRule> rules = categoryRuleRepository.findByActiveTrueOrderByPriorityDesc();

            String category = "Uncategorized";
            String subCategory = "Other";
            double confidenceScore = 0.5;

            for (CategoryRule rule : rules) {
                for (String keyword : rule.getKeywords()) {
                    if (description.contains(keyword.toLowerCase())) {
                        category = rule.getCategory();
                        subCategory = rule.getSubCategory();
                        confidenceScore = 0.9;
                        break;
                    }
                }
                if (confidenceScore > 0.8)
                    break;
            }

            // Publish categorized event
            TransactionCategorizedEvent categorizedEvent = TransactionCategorizedEvent.builder()
                    .transactionId(event.getTransactionId())
                    .userId(event.getUserId())
                    .category(category)
                    .subCategory(subCategory)
                    .confidenceScore(confidenceScore)
                    .categorizationMethod("RULE_BASED")
                    .categorizedAt(LocalDateTime.now())
                    .build();

            rabbitTemplate.convertAndSend(
                    TransactionCategorizedEvent.EXCHANGE,
                    TransactionCategorizedEvent.ROUTING_KEY,
                    categorizedEvent);

            log.info("Transaction categorized: {} -> {}/{}", event.getTransactionId(), category, subCategory);

        } catch (Exception e) {
            log.error("Error categorizing transaction", e);
        }
    }
}
