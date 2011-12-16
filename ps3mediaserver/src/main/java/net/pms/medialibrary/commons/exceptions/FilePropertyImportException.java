package net.pms.medialibrary.commons.exceptions;

import net.pms.medialibrary.commons.enumarations.FileProperty;

public class FilePropertyImportException extends Exception {
	private static final long serialVersionUID = 9188879550302128317L;
	
	private FileProperty fileProperty;
	private Class<?> receivedType;
	private Class<?> expectedType;
	private ExceptionType exceptionType;

	public FilePropertyImportException(FileProperty fileProperty, Class<?> receivedType, Class<?> expectedType, ExceptionType exceptionType) {
		this.fileProperty = fileProperty;
		this.receivedType = receivedType;
		this.expectedType = expectedType;
		this.exceptionType = exceptionType;
	}
	
	public FileProperty getFileProperty(){
		return fileProperty;
	}

	public Class<?> getReceivedType(){
		return receivedType;
	}

	public Class<?> getExpectedType(){
		return expectedType;
	}
	
	public ExceptionType getExceptionType(){
		return exceptionType;
	}
	
	public enum ExceptionType{
		NoResult, 
		WrongType,
		ProcessingFailed		
	}
}
