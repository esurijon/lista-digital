package com.surix.ld.model;

import com.surix.ld.exceptions.LdException;

@SuppressWarnings("serial")
public class EntityException extends LdException {

	public EntityException(String message, Throwable cause) {
		super(message, cause);
	}

	public EntityException(Throwable cause) {
		super(cause);
	}

	public EntityException(String message) {
		super(message);
	}
}
