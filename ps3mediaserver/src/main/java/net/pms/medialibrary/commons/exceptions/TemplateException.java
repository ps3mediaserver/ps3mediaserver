package net.pms.medialibrary.commons.exceptions;

public class TemplateException extends Exception{
    private static final long serialVersionUID = 3761494339733479109L;

	public TemplateException(){
		super();
	}
	
	public TemplateException(String message){
		super(message);
	}
	
	public TemplateException(String message, Throwable cause){
		super(message, cause);
	}

}
