package com.inggarciabaldo.carburo.application.persistance;

import java.io.Serial;

public class PersistenceException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 1L;

	public PersistenceException() {
	}

	public PersistenceException(String message) {
		super(message);
	}

	public PersistenceException(Throwable cause) {
		super(cause);
	}

	public PersistenceException(String message, Throwable cause) {
		super(message, cause);
	}

	public PersistenceException(String message, Throwable cause,
								boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
