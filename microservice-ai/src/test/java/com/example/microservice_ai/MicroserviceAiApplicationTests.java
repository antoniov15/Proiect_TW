package com.example.microservice_ai;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Application context tests.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@DisplayName("Application Context Tests")
class MicroserviceAiApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("Context should load successfully")
    void contextLoads() {
        assertNotNull(applicationContext);
    }

    @Test
    @DisplayName("Should have ChatRepository bean")
    void chatRepositoryBeanExists() {
        assertTrue(applicationContext.containsBean("chatRepository"));
    }

    @Test
    @DisplayName("Should have ChatServiceImpl bean")
    void chatServiceBeanExists() {
        assertTrue(applicationContext.containsBean("chatServiceImpl"));
    }
}
