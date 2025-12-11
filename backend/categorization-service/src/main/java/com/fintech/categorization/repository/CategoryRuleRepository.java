package com.fintech.categorization.repository;

import com.fintech.categorization.document.CategoryRule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRuleRepository extends MongoRepository<CategoryRule, String> {
    List<CategoryRule> findByActiveTrueOrderByPriorityDesc();
}
