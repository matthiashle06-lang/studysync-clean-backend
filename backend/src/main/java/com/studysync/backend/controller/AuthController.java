package com.studysync.backend.controller;

import com.studysync.backend.model.UserToken;
import com.studysync.backend.repository.UserTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
//capstone
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*") // Allows calls from mobile client
public class AuthController {

    @Autowired
    private UserTokenRepository userTokenRepository;

    @PostMapping("/token")
    public ResponseEntity<String> saveUserToken(@RequestBody UserToken requestToken) {
        try {
            // 1. Search the database to see if this user already exists
            Optional<UserToken> existingTokenOpt = userTokenRepository.findByUserId(requestToken.getUserId());

            if (existingTokenOpt.isPresent()) {
                // 2. User exists! Update their tokens so they stay fresh
                UserToken existingToken = existingTokenOpt.get();
                existingToken.setAccessToken(requestToken.getAccessToken());
                
                if (requestToken.getRefreshToken() != null) {
                    existingToken.setRefreshToken(requestToken.getRefreshToken());
                }
                
                userTokenRepository.save(existingToken);
                return ResponseEntity.ok("Token updated successfully for user: " + requestToken.getUserId());
            } else {
                // 3. User does not exist! Save them as a brand new document
                userTokenRepository.save(requestToken);
                return ResponseEntity.ok("New token saved successfully for user: " + requestToken.getUserId());
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to save token: " + e.getMessage());
        }
    }
}