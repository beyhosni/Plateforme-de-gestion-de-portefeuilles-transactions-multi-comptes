package com.fintech.categorization.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "category_rules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRule {

    @Id
    private String id;

    private String category;

    private String subCategory;

    private List<String> keywords; // Keywords to match in description

    private Double priority; // Higher priority rules are checked first

    private boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
