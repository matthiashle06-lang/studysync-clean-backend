package com.studysync.backend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;

@SpringBootApplication
public class BackendApplication {

    // Ask Spring to find the property, default to "MISSING_PROPERTY" if it fails
    @Value("${spring.data.mongodb.uri:MISSING_PROPERTY}")
    private String mongoUri;

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Bean
    public CommandLineRunner testDatabaseConnection(MongoTemplate mongoTemplate) {
        return args -> {
            System.out.println("\n\n====== DIAGNOSTIC CHECK ======");
            System.out.println("URI read by Spring Boot: " + mongoUri);
            System.out.println("==============================\n");

            System.out.println("\n====== DATABASE CONNECTION TEST ======");
            try {
                mongoTemplate.executeCommand("{ ping: 1 }");
                System.out.println("SUCCESS: Connected to MongoDB Atlas!");
            } catch (Exception e) {
                System.out.println("FAILED: Database connection refused.");
            }
            System.out.println("======================================\n\n");
        };
    }
}