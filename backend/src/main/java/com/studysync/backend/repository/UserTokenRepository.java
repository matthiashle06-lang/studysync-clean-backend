package com.studysync.backend.repository;

import com.studysync.backend.model.UserToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface UserTokenRepository extends MongoRepository<UserToken, String> {
    Optional<UserToken> findByUserId(String userId);
}