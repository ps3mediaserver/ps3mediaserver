package net.pms.medialibrary.commons.exceptions;

public class ConditionException extends Exception {
	private static final long serialVersionUID = 3761494339733479109L;

	public ConditionException() {
		super();
	}

	public ConditionException(String message) {
		super(message);
	}

	public ConditionException(String message, Throwable cause) {
		super(message, cause);
	}
}
