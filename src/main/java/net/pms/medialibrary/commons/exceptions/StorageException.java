package net.pms.medialibrary.commons.exceptions;

public class StorageException extends Exception {
	private static final long serialVersionUID = 1061125846490377072L;

	public StorageException() {
		super();
	}

	public StorageException(String message) {
		super(message);
	}

	public StorageException(String message, Throwable cause) {
		super(message, cause);
	}
}
