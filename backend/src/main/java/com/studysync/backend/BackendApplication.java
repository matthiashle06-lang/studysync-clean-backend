package com.studysync.backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    // This Bean runs automatically the moment the application starts
    @Bean
    public CommandLineRunner testDatabaseConnection(MongoTemplate mongoTemplate) {
        return args -> {
            System.out.println("\n\n====== DATABASE CONNECTION TEST ======");
            try {
                // This forces a strict network call to Atlas
                mongoTemplate.executeCommand("{ ping: 1 }");
                System.out.println("SUCCESS: Connected to MongoDB Atlas!");
            } catch (Exception e) {
                System.out.println("FAILED: Database connection refused.");
                e.printStackTrace();
            }
            System.out.println("======================================\n\n");
        };
    }
}