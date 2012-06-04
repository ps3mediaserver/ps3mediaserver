package net.pms.plugin.webservice;

public class InvalidParameterException extends Exception {
	private static final long serialVersionUID = -4615157497145995177L;

	public InvalidParameterException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidParameterException(String message) {
		super(message);
	}

}
