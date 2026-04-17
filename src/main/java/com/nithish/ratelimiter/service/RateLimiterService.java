package com.nithish.ratelimiter.service;

import com.nithish.ratelimiter.dto.ApiRequestDTO;
import com.nithish.ratelimiter.dto.RateLimitResult;

public interface RateLimiterService {

	RateLimitResult allow(ApiRequestDTO request);
}
