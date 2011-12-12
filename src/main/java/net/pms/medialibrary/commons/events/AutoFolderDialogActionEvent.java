package net.pms.medialibrary.commons.events;

import java.util.EventObject;

import net.pms.medialibrary.commons.enumarations.DialogActionType;
import net.pms.medialibrary.commons.enumarations.AutoFolderType;

public class AutoFolderDialogActionEvent extends EventObject{	
	private static final long serialVersionUID = 1L;
	private AutoFolderType autoFolderType;
	private boolean isAscending;
	private DialogActionType actionType;
	private Object userObject;
	private int minOccurences;

	public AutoFolderDialogActionEvent(Object source, AutoFolderType autoFolderType, boolean isAscending, int minOccurences, DialogActionType actionType, Object userObject) {
		super(source);
		this.autoFolderType = autoFolderType;
		this.isAscending = isAscending;
		this.actionType = actionType;
		this.userObject = userObject;
		this.minOccurences = minOccurences;
	}

	public AutoFolderType getAutoFolderType() {
		return autoFolderType;
	}

	public boolean isAscending() {
		return isAscending;
	}

	public DialogActionType getActionType() {
		return actionType;
	}

	public Object getUserObject() {
		return userObject;
	}

	public int getMinOccurences() {
		return minOccurences;
	}

}
