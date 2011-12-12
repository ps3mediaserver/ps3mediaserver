package net.pms.medialibrary.commons.events;

import java.util.EventObject;

import net.pms.medialibrary.commons.dataobjects.DOFileEntryPlugin;
import net.pms.medialibrary.commons.enumarations.DialogActionType;

public class FileEntryPluginDialogActionEvent extends EventObject {
    private static final long serialVersionUID = -8764169334010907911L;
	private DOFileEntryPlugin fileEntryPlugin;
	private DialogActionType actionType;
	private boolean isNew;
	
	public FileEntryPluginDialogActionEvent(Object source, DOFileEntryPlugin fileEntryPlugin, DialogActionType actionType, boolean isNew){
		super(source);
		this.fileEntryPlugin = fileEntryPlugin;
		this.actionType = actionType;
		this.isNew = isNew;
	}

	public DialogActionType getActionType() {
		return actionType;
	}

	public DOFileEntryPlugin getFileEntryPlugin() {
		return fileEntryPlugin;
	}
	
	public boolean isNew(){
		return isNew;
	}
}
