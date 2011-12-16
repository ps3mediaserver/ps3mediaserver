package net.pms.medialibrary.commons.exceptions;

public class FileImportException extends Exception {
	private static final long serialVersionUID = 4380277780932928258L;

	public FileImportException() {
		
	}
	
	public FileImportException(String message){
		super(message);
	}
	
	public FileImportException(String message, Throwable cause){
		super(message, cause);
	}
}
