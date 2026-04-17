package com.nithish.ratelimiter.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class ApiKey {

	@Id
	private String key;
	@Column(name = "username")
	private String user;
	private LocalDateTime expiry;
	
	public ApiKey() {}

	public ApiKey(String key, String user, LocalDateTime expiry) {
		this.key = key;
		this.user = user;
		this.expiry = expiry;
	}
	
	public String getKey() {
        return key;
    }
    public String getUser() {
        return user;
    }
    public LocalDateTime getExpiry() {
        return expiry;
    }
}
