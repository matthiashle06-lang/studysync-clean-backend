package com.studysync.backend;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;

@SpringBootApplication
public class BackendApplication {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    // 1. Force the creation of the MongoClient using the verified string
    @Bean
    public MongoClient mongoClient() {
        System.out.println("\n====== OVERRIDING SPRING AUTO-CONFIG ======");
        System.out.println("Injecting URI directly into MongoClient: " + mongoUri);
        return MongoClients.create(mongoUri);
    }

    // 2. Force the creation of the MongoTemplate using our custom client
    @Bean
    public MongoTemplate mongoTemplate(MongoClient mongoClient) {
        return new MongoTemplate(mongoClient, "studysync");
    }

    // 3. Execute the Ping
    @Bean
    public CommandLineRunner testDatabaseConnection(MongoTemplate mongoTemplate) {
        return args -> {
            System.out.println("\n\n====== DATABASE CONNECTION TEST ======");
            try {
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