package com.nithish.ratelimiter.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ApiRequestDTO {

	@NotBlank(message = "mail id cannot be empty")
	@Email(message = "invalid email format")
	private String email;
	
	@NotBlank(message = "endpoint cannot be empty")
	private String endpoint;
	
	private String ip;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}
	
}
