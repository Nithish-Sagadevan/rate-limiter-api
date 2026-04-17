package com.nithish.ratelimiter.apikey;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.nithish.ratelimiter.entity.ApiKey;
import com.nithish.ratelimiter.repository.ApiKeyRepository;

@Component
public class ApiKeyStore {

    private static final Logger logger = LoggerFactory.getLogger(ApiKeyStore.class);

    private final ApiKeyRepository repository;

    public ApiKeyStore(ApiKeyRepository repository) {
        this.repository = repository;
    }

    public String createApiKey(String user) {

        String key = ApiKeyGenerator.generateApiKey();

        ApiKey apiKey = new ApiKey(key, user, LocalDateTime.now().plusMinutes(30));

        repository.save(apiKey);

        logger.info("API key generated for user: {}", user);

        return key;
    }

    public boolean isValid(String apiKey) {

        ApiKey keyObj = repository.findById(apiKey).orElse(null);

        if (keyObj == null) {
            logger.warn("Invalid API key: {}", apiKey);
            return false;
        }

        if (keyObj.getExpiry().isBefore(LocalDateTime.now())) {
            repository.deleteById(apiKey);
            logger.warn("Expired API key: {}", apiKey);
            return false;
        }

        return true;
    }

    public String getUser(String apiKey) {
        return repository.findById(apiKey).get().getUser();
    }

    public void revoke(String apiKey) {
        repository.deleteById(apiKey);
        logger.warn("API key revoked: {}", apiKey);
    }
}