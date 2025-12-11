package com.fintech.shared.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMQConfig {

    // Exchange names
    public static final String TRANSACTION_EXCHANGE = "transaction.events";
    public static final String CATEGORIZATION_EXCHANGE = "categorization.events";
    public static final String NOTIFICATION_EXCHANGE = "notification.events";

    // Queue names
    public static final String TRANSACTION_COMPLETED_QUEUE = "transaction.completed.queue";
    public static final String TRANSACTION_FAILED_QUEUE = "transaction.failed.queue";
    public static final String TRANSACTION_CATEGORIZED_QUEUE = "transaction.categorized.queue";

    // Routing keys
    public static final String TRANSACTION_COMPLETED_ROUTING_KEY = "transaction.completed";
    public static final String TRANSACTION_FAILED_ROUTING_KEY = "transaction.failed";
    public static final String TRANSACTION_CATEGORIZED_ROUTING_KEY = "transaction.categorized";

    // Dead Letter Queues
    public static final String TRANSACTION_DLQ = "transaction.dlq";
    public static final String CATEGORIZATION_DLQ = "categorization.dlq";
    public static final String NOTIFICATION_DLQ = "notification.dlq";

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    // ==================== EXCHANGES ====================
    @Bean
    public TopicExchange transactionExchange() {
        return new TopicExchange(TRANSACTION_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange categorizationExchange() {
        return new TopicExchange(CATEGORIZATION_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE, true, false);
    }

    // ==================== QUEUES ====================
    @Bean
    public Queue transactionCompletedQueue() {
        return QueueBuilder.durable(TRANSACTION_COMPLETED_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", TRANSACTION_DLQ)
                .build();
    }

    @Bean
    public Queue transactionFailedQueue() {
        return QueueBuilder.durable(TRANSACTION_FAILED_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", TRANSACTION_DLQ)
                .build();
    }

    @Bean
    public Queue transactionCategorizedQueue() {
        return QueueBuilder.durable(TRANSACTION_CATEGORIZED_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", CATEGORIZATION_DLQ)
                .build();
    }

    // Dead Letter Queues
    @Bean
    public Queue transactionDLQ() {
        return QueueBuilder.durable(TRANSACTION_DLQ).build();
    }

    @Bean
    public Queue categorizationDLQ() {
        return QueueBuilder.durable(CATEGORIZATION_DLQ).build();
    }

    @Bean
    public Queue notificationDLQ() {
        return QueueBuilder.durable(NOTIFICATION_DLQ).build();
    }

    // ==================== BINDINGS ====================
    @Bean
    public Binding transactionCompletedBinding() {
        return BindingBuilder
                .bind(transactionCompletedQueue())
                .to(transactionExchange())
                .with(TRANSACTION_COMPLETED_ROUTING_KEY);
    }

    @Bean
    public Binding transactionFailedBinding() {
        return BindingBuilder
                .bind(transactionFailedQueue())
                .to(transactionExchange())
                .with(TRANSACTION_FAILED_ROUTING_KEY);
    }

    @Bean
    public Binding transactionCategorizedBinding() {
        return BindingBuilder
                .bind(transactionCategorizedQueue())
                .to(categorizationExchange())
                .with(TRANSACTION_CATEGORIZED_ROUTING_KEY);
    }
}
