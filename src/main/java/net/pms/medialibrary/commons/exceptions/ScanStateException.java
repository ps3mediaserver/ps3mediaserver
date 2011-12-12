package net.pms.medialibrary.commons.exceptions;

import net.pms.medialibrary.commons.enumarations.ScanState;

public class ScanStateException extends Exception{
	private static final long serialVersionUID = 1L;
	private ScanState expectedState;
	private ScanState currentState;

	public ScanStateException(ScanState expectedState, ScanState currentState){
		this(expectedState, currentState, "");
	}
	
	public ScanStateException(ScanState expectedState, ScanState currentState, String message){
		this(expectedState, currentState, "", null);
	}

	public ScanStateException(ScanState expectedState, ScanState currentState, String message, Exception innerException){
		super(message, innerException);
		this.expectedState = expectedState;
		this.currentState = currentState;
	}

	public ScanState getExpectedState() {
	    return expectedState;
	}

	public ScanState getCurrentState() {
	    return currentState;
    }
}
