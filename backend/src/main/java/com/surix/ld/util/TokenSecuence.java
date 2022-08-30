package com.surix.ld.util;

import java.util.UUID;

public class TokenSecuence {

	private String lastToken;

	public TokenSecuence() {
		nextToken();
	}
	
	public String nextToken() {
		lastToken = UUID.randomUUID().toString();
		return lastToken;
	}

	public String lastToken() {
		return lastToken;
	}
}
