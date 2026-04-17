package com.nithish.ratelimiter.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RateLimitConfig {
	
	@Value("${ratelimiter.limit}")
	private int limit;
	
	@Value("${ratelimiter.window-minutes}")
	private int windowMinutes;
	
	public int getLimit() {
        return limit;
    }

    public int getWindowMinutes() {
        return windowMinutes;
    }
}
