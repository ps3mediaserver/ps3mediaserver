package net.pms.medialibrary.commons.exceptions;

public class FilterFormatException extends Exception{
    private static final long serialVersionUID = 7524034719950807976L;

	public FilterFormatException(){
		super();
	}
	
	public FilterFormatException(String message){
		super(message);
	}
	
	public FilterFormatException(String message, Throwable cause){
		super(message, cause);
	}

}
