package com.nithish.ratelimiter.service.implementation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.nithish.ratelimiter.config.RateLimitConfig;
import com.nithish.ratelimiter.dto.ApiRequestDTO;
import com.nithish.ratelimiter.dto.RateLimitResult;
import com.nithish.ratelimiter.service.RateLimiterService;

@Service
public class RateLimiterServiceImpl implements RateLimiterService {
	
	private final RateLimitConfig config;
	
	public RateLimiterServiceImpl(RateLimitConfig config) {
		this.config = config;
	}

	private static final Logger logger = LoggerFactory.getLogger(RateLimiterServiceImpl.class);
	
	private final Map<String, List<Long>> requestMap = new ConcurrentHashMap<>();
	
	@Override
	public RateLimitResult allow(ApiRequestDTO request) {
		
		String key = request.getEmail() + ":" + request.getEndpoint() + ":" + request.getIp();
		
		long now = System.currentTimeMillis();
		long windowMillis = config.getWindowMinutes() * 60 * 1000;
		
		requestMap.putIfAbsent(key, Collections.synchronizedList(new ArrayList<>()));
		
		List<Long> timestamps = requestMap.get(key);
		
		synchronized (timestamps) {
			
			timestamps.removeIf(time -> now - time > windowMillis);
			
			if(timestamps.isEmpty()) {
				requestMap.remove(key);
				requestMap.putIfAbsent(key, Collections.synchronizedList(new ArrayList<>()));
				timestamps = requestMap.get(key);
			}
			
			long requestCount = timestamps.size();
			
			logger.info("Rate check -> User: {}, Count: {}, Limit: {}", key, requestCount, config.getLimit());
			
			long resetTime;
			if(!timestamps.isEmpty()) {
				Long oldest = timestamps.get(0);
				resetTime = (windowMillis - (now - oldest)) / 1000;
				resetTime = Math.max(resetTime, 0);
			}else {
				resetTime = windowMillis / 1000;
			}
			
			if(requestCount >= config.getLimit()) {
				logger.warn("Blocked -> User: {}, Limit exceeded", key);
				return new RateLimitResult(false, 0, config.getLimit(), resetTime);
			}

			timestamps.add(now);
			
			long updatedCount = timestamps.size();
			long remaining = Math.max(0, config.getLimit() - updatedCount);
			
			logger.info("Allowed -> User: {}, Remaining: {}", key, remaining);
			
			return new RateLimitResult(true, remaining, config.getLimit(), resetTime);
		}
	}
	
	public int getActiveUsers() {
	    return requestMap.size();
	}

	public long getTotalRequests() {
	    return requestMap.values()
	            .stream()
	            .mapToLong(List::size)
	            .sum();
	}
}
