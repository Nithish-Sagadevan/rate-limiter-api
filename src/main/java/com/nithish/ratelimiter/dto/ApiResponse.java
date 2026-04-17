package com.nithish.ratelimiter.dto;

public class ApiResponse {

	private boolean allowed;
	private String message;
	
	private long remaining;
	private long limit;
	private long resetTime;
	
	public ApiResponse(boolean allowed, String message) {
		this.allowed = allowed;
		this.message = message;
	}
	
	public ApiResponse(boolean allowed, String message, long remaining, long limit, long resetTime) {
		this.allowed = allowed;
		this.message = message;
		this.remaining = remaining;
		this.limit = limit;
		this.resetTime = resetTime;
	}
	
	public boolean isAllowed() {
		return allowed;
	}
	public void setAllowed(boolean allowed) {
		this.allowed = allowed;
	}
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	public long getRemaining() {
		return remaining;
	}

	public long getLimit() {
		return limit;
	}

	public long getResetTime() {
		return resetTime;
	}

}
