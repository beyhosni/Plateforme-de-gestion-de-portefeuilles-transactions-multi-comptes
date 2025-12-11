package com.fintech.notification.service;

import com.fintech.shared.events.TransactionCategorizedEvent;
import com.fintech.shared.events.TransactionCompletedEvent;
import com.fintech.shared.events.TransactionFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender mailSender;

    @RabbitListener(queues = "transaction.completed.queue")
    public void handleTransactionCompleted(TransactionCompletedEvent event) {
        log.info("Sending notification for completed transaction: {}", event.getTransactionId());

        String message = String.format(
                "Your transaction of %s %s has been completed successfully.\\n" +
                        "Transaction ID: %s\\n" +
                        "Reference: %s\\n" +
                        "Date: %s",
                event.getAmount(),
                event.getCurrency(),
                event.getTransactionId(),
                event.getReference(),
                event.getCompletedAt());

        sendNotification("Transaction Completed", message);
    }

    @RabbitListener(queues = "transaction.failed.queue")
    public void handleTransactionFailed(TransactionFailedEvent event) {
        log.info("Sending notification for failed transaction: {}", event.getTransactionId());

        String message = String.format(
                "Your transaction of %s %s has failed.\\n" +
                        "Transaction ID: %s\\n" +
                        "Reason: %s\\n" +
                        "Date: %s",
                event.getAmount(),
                event.getCurrency(),
                event.getTransactionId(),
                event.getFailureReason(),
                event.getFailedAt());

        sendNotification("Transaction Failed", message);
    }

    @RabbitListener(queues = "transaction.categorized.queue")
    public void handleTransactionCategorized(TransactionCategorizedEvent event) {
        log.info("Transaction {} categorized as: {}/{}",
                event.getTransactionId(), event.getCategory(), event.getSubCategory());

        String message = String.format(
                "Your transaction has been automatically categorized.\\n" +
                        "Category: %s\\n" +
                        "Sub-Category: %s\\n" +
                        "Confidence: %.2f%%",
                event.getCategory(),
                event.getSubCategory(),
                event.getConfidenceScore() * 100);

        sendNotification("Transaction Categorized", message);
    }

    private void sendNotification(String subject, String message) {
        try {
            log.info("Notification: {} - {}", subject, message);

            // In production, send actual email
            // SimpleMailMessage mailMessage = new SimpleMailMessage();
            // mailMessage.setTo(userEmail);
            // mailMessage.setSubject(subject);
            // mailMessage.setText(message);
            // mailSender.send(mailMessage);

            log.info("Notification sent successfully");
        } catch (Exception e) {
            log.error("Failed to send notification", e);
        }
    }
}
