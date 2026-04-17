package com.nithish.ratelimiter.dto;

public class RateLimitResult {

	private boolean allowed;
	private long remaining;
	private long limit;
	private long resetTime;
	
	public RateLimitResult(boolean allowed, long remaining, long limit, long resetTime) {
		this.allowed = allowed;
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

	public long getRemaining() {
		return remaining;
	}
	public void setRemaining(long remaining) {
		this.remaining = remaining;
	}

	public long getLimit() {
		return limit;
	}
	public void setLimit(long limit) {
		this.limit = limit;
	}

	public long getResetTime() {
		return resetTime;
	}
	public void setResetTime(long resetTime) {
		this.resetTime = resetTime;
	}
	
}
