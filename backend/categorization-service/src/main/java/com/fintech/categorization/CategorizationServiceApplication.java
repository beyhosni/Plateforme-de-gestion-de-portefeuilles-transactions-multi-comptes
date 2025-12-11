package com.fintech.categorization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = { "com.fintech.categorization", "com.fintech.shared" })
public class CategorizationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CategorizationServiceApplication.class, args);
    }
}
