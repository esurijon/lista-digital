package com.surix.ld.exceptions;

@SuppressWarnings("serial")
public class EnvironmentException extends RuntimeException {

	public EnvironmentException(Throwable t) {
		super(t);
	}

	public EnvironmentException(String message) {
		super(message);
	}

}
