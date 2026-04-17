package com.nithish.ratelimiter.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nithish.ratelimiter.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
public class RateLimiterController {
	
	@GetMapping("/test")
	public ApiResponse test(HttpServletRequest request) {
		return new ApiResponse(true, "Request successful");
	}
}
