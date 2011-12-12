package net.pms.medialibrary.commons.events;

import java.util.EventObject;

import net.pms.medialibrary.commons.dataobjects.DOSpecialFolder;
import net.pms.medialibrary.commons.enumarations.DialogActionType;

public class SpecialFolderDialogActionEvent extends EventObject {
    private static final long serialVersionUID = -9016122585960682759L;
	private DOSpecialFolder specialFolder;
	private DialogActionType actionType;
	
	public SpecialFolderDialogActionEvent(Object source, DOSpecialFolder specialFolder, DialogActionType actionType){
		super(source);
		this.specialFolder = specialFolder;
		this.actionType = actionType;
	}

	public DialogActionType getActionType() {
		return actionType;
	}

	public DOSpecialFolder getSpecialFolder() {
		return specialFolder;
	}
}
