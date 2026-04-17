package com.nithish.ratelimiter.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nithish.ratelimiter.service.implementation.RateLimiterServiceImpl;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final RateLimiterServiceImpl service;

    public AdminController(RateLimiterServiceImpl service) {
        this.service = service;
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {

        Map<String, Object> stats = new HashMap<>();

        stats.put("activeUsers", service.getActiveUsers());
        stats.put("totalRequests", service.getTotalRequests());

        return stats;
    }
}