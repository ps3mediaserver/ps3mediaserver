package net.pms.medialibrary.commons.exceptions;

import net.pms.medialibrary.commons.enumarations.ConditionType;

public class ConditionTypeException extends Exception {
	private static final long serialVersionUID = -3410282407248145911L;
	private ConditionType ct;
	private Object value;

	public ConditionTypeException(ConditionType ct, Object value) {
		this.ct = ct;
		this.value = value;
	}

	public ConditionType getConditionType() {
		return ct;
	}

	public Object getValue() {
		return value;
	}
}
