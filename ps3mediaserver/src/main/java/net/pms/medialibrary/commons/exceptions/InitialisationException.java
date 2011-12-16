package net.pms.medialibrary.commons.exceptions;

public class InitialisationException extends Exception {
	private static final long serialVersionUID = 1L;

	public InitialisationException(){
		super();
	}
	
	public InitialisationException(String message){
		super(message);
	}
	
	public InitialisationException(String message, Throwable cause){
		super(message, cause);
	}
}
