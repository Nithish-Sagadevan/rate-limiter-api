package com.nithish.ratelimiter.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nithish.ratelimiter.apikey.ApiKeyStore;
import com.nithish.ratelimiter.dto.ApiKeyResponse;

@RestController
@RequestMapping("/apikey")
public class ApiKeyController {

	private final ApiKeyStore store;

	public ApiKeyController(ApiKeyStore store) {
		this.store = store;
	}
	
	@PostMapping("/generate")
	public ApiKeyResponse generate(@RequestParam String email) {
		String key = store.createApiKey(email);
		return new ApiKeyResponse(true, key);
	}
	
	@DeleteMapping("/revoke")
	public String revoke(@RequestParam String key) {
		store.revoke(key);
		return "API key revoked";
	}
}
