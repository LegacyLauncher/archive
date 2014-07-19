package ru.turikhay.exceptions;

public class ParseException extends RuntimeException {
	public ParseException(String string) {
		super(string);
	}

	public ParseException(String message, Throwable cause) {
		super(message, cause);
	}

	public ParseException(Throwable cause) {
		super(cause);
	}

	private static final long serialVersionUID = -3231272464953548141L;
}
