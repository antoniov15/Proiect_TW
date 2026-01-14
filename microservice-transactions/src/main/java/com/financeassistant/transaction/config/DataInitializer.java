package com.financeassistant.transaction.config;

import com.financeassistant.transaction.entity.Category;
import com.financeassistant.transaction.entity.TransactionType;
import com.financeassistant.transaction.repository.CategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Autowired
    public DataInitializer(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (categoryRepository.count() == 0) {
            log.info("Populating categories table...");

            createCategoryIfNotExists("Salariu", TransactionType.INCOME);
            createCategoryIfNotExists("Cumparaturi", TransactionType.EXPENSE);
            createCategoryIfNotExists("Facturi", TransactionType.EXPENSE);
            createCategoryIfNotExists("Mancare", TransactionType.EXPENSE);
            createCategoryIfNotExists("Food", TransactionType.EXPENSE);      // Pentru AI
            createCategoryIfNotExists("Transport", TransactionType.EXPENSE); // Pentru AI
            createCategoryIfNotExists("Entertainment", TransactionType.EXPENSE); // Pentru AI
            createCategoryIfNotExists("Health", TransactionType.EXPENSE);    // Pentru AI
            createCategoryIfNotExists("Utilities", TransactionType.EXPENSE); // Pentru AI
            createCategoryIfNotExists("Income", TransactionType.INCOME);     // Pentru AI

            log.info("Finished populating categories.");
        }
    }

    private void createCategoryIfNotExists(String name, TransactionType type) {
        try {
            if (categoryRepository.findByName(name) == null) {
                Category category = new Category();
                category.setName(name);
                category.setType(type);
                categoryRepository.save(category);
                log.info("Inserted category: {}", name);
            }
        } catch (DataIntegrityViolationException | org.springframework.orm.jpa.JpaSystemException e) {
            log.info("Category '{}' already exists (skipped).", name);
        } catch (Exception e) {
            log.error("Error creating category '{}': {}", name, e.getMessage());
        }
    }
}
