package com.nithish.ratelimiter.filter;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.nithish.ratelimiter.apikey.ApiKeyStore;
import com.nithish.ratelimiter.dto.ApiRequestDTO;
import com.nithish.ratelimiter.dto.ApiResponse;
import com.nithish.ratelimiter.dto.RateLimitResult;
import com.nithish.ratelimiter.service.RateLimiterService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimiterFilter extends OncePerRequestFilter {
	
	private static final Logger logger = LoggerFactory.getLogger(RateLimiterFilter.class);
	
	private final RateLimiterService rateLimiterService;
	private final ApiKeyStore apiKeyStore;
	
	public RateLimiterFilter(RateLimiterService rateLimiterService, ApiKeyStore apiKeyStore) {
		this.rateLimiterService = rateLimiterService;
		this.apiKeyStore = apiKeyStore;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		String path = request.getRequestURI();
		return path.startsWith("/apikey") ||
				path.startsWith("/swagger-ui") ||
				path.startsWith("/v3/api-docs") ||
				path.startsWith("/admin");
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		logger.info("Incoming request -> URI: {}, Method: {}", request.getRequestURI(), request.getMethod());
		
		String apiKey = request.getHeader("X-API-KEY");
		
		if (apiKey == null || apiKey.isBlank()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing API Key");
            return;
		}
		
		logger.info("API key received: {}", apiKey);
		
		if (!apiKeyStore.isValid(apiKey)) {
			logger.warn("Request blocked: Invalid API key");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid API Key");
            return;
        }
		
		String user = apiKeyStore.getUser(apiKey);
		String endpoint = request.getRequestURI();
		String ip = request.getRemoteAddr();
		
		ApiRequestDTO dto = new ApiRequestDTO();
		dto.setEmail(user);
		dto.setEndpoint(endpoint);
		dto.setIp(ip);
		
		RateLimitResult result = rateLimiterService.allow(dto);
		
		response.setHeader("X-Rate-Limit-Limit", String.valueOf(result.getLimit()));
        response.setHeader("X-Rate-Limit-Remaining", String.valueOf(result.getRemaining()));
        response.setHeader("X-Rate-Limit-Reset", String.valueOf(result.getResetTime()));
        
        if(!result.isAllowed()) {
        		logger.warn("Rate limit exceeded for User: {}", user);
        		response.setStatus(429);
        		response.setContentType("application/json");
        		ApiResponse apiResponse = new ApiResponse(
        	            false,
        	            "Rate limit exceeded",
        	            result.getRemaining(),
        	            result.getLimit(),
        	            result.getResetTime()
        	    );
        		response.getWriter().write(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(apiResponse));
        		return;
        }
        
        request.setAttribute("rateLimitResult", result);
        
        logger.info("Request allowed for User: {}", user);
        
        filterChain.doFilter(request, response);
	}

}
