package com.surix.ld.exceptions;

@SuppressWarnings("serial")
public class LdException extends Exception {

	public LdException(String message, Throwable cause) {
		super(message, cause);
	}

	public LdException(String message) {
		super(message);
	}

	public LdException(Throwable cause) {
		super(cause);
	}
}
