package com.nithish.ratelimiter.apikey;

import java.security.SecureRandom;
import java.util.Base64;

public class ApiKeyGenerator {

	private static final SecureRandom SECURE_RANDOM = new SecureRandom();
	private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
	
	public static String generateApiKey() {
		byte[] randomBytes = new byte[32];
		SECURE_RANDOM.nextBytes(randomBytes);
		return ENCODER.encodeToString(randomBytes);
	}
}
