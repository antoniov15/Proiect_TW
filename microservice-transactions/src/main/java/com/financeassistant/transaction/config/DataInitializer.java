package com.financeassistant.transaction.config;

import com.financeassistant.transaction.entity.Category;
import com.financeassistant.transaction.repository.CategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
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

            Category category1 = new Category();
            category1.setName("Salariu");
            categoryRepository.save(category1);

            Category category2 = new Category();
            category2.setName("Cumparaturi");
            categoryRepository.save(category2);

            Category category3 = new Category();
            category3.setName("Facturi");
            categoryRepository.save(category3);

            log.info("Finished populating categories.");
        }
    }
}
