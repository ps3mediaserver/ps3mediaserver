package net.pms.plugin.webservice.configuration;

public class SaveException extends Exception {
	private static final long serialVersionUID = -4615157497145995177L;

	public SaveException(String message, Throwable cause) {
		super(message, cause);
	}

	public SaveException(String message) {
		super(message);
	}

}
