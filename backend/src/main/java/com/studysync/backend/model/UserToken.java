package com.studysync.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "user_tokens")
public class UserToken {
    @Id
    private String id;
    private String userId;
    private String accessToken;
    private String refreshToken;
    private String syncToken; // Used for incremental Sync Tokens to prevent rate limits

    public UserToken() {}

    public UserToken(String userId, String accessToken, String refreshToken) {
        this.userId = userId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public String getSyncToken() { return syncToken; }
    public void setSyncToken(String syncToken) { this.syncToken = syncToken; }
}