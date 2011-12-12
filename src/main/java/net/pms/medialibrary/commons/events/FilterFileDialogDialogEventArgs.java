package net.pms.medialibrary.commons.events;

import java.util.EventObject;

import net.pms.medialibrary.commons.dataobjects.DOFileEntryBase;
import net.pms.medialibrary.commons.enumarations.DialogActionType;

public class FilterFileDialogDialogEventArgs extends EventObject {
    private static final long serialVersionUID = -7548148885762691765L;
    private DialogActionType actionType;
    private DOFileEntryBase entry;

	public FilterFileDialogDialogEventArgs(Object source, DialogActionType actionType, DOFileEntryBase entry) {
		super(source);
	    this.setActionType(actionType);
	    this.setEntry(entry);
    }

	public void setActionType(DialogActionType actionType) {
	    this.actionType = actionType;
    }

	public DialogActionType getActionType() {
	    return actionType;
    }

	public void setEntry(DOFileEntryBase entry) {
	    this.entry = entry;
    }

	public DOFileEntryBase getEntry() {
	    return entry;
    }
}
