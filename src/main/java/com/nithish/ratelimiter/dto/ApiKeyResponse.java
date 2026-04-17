package com.nithish.ratelimiter.dto;

public class ApiKeyResponse {
	
	private boolean success;
    private String apiKey;

    public ApiKeyResponse(boolean success, String apiKey) {
        this.success = success;
        this.apiKey = apiKey;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getApiKey() {
        return apiKey;
    }
}
