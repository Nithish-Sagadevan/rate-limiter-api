package com.nithish.ratelimiter.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nithish.ratelimiter.entity.ApiKey;

public interface ApiKeyRepository extends JpaRepository<ApiKey, String> {

}
