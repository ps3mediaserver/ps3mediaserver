package net.pms.medialibrary.commons.events;

import java.util.EventObject;

import net.pms.medialibrary.commons.dataobjects.DOMediaLibraryFolder;
import net.pms.medialibrary.commons.enumarations.DialogActionType;

public class FolderDialogFolderUpdateEvent extends EventObject{
	
	private static final long serialVersionUID = 1L;
	private DialogActionType actionType;
	private DOMediaLibraryFolder mediaLibraryFolder;
	private boolean isNewFilter;

	public FolderDialogFolderUpdateEvent(Object source, DOMediaLibraryFolder mediaLibraryFolder, DialogActionType actionType, boolean isNewFilter) {
		super(source);
		this.actionType = actionType;
		this.mediaLibraryFolder = mediaLibraryFolder;
		this.isNewFilter = isNewFilter;
	}

	public DialogActionType getActionType() {
		return actionType;
	}

	public DOMediaLibraryFolder getMediaLibraryFolder() {
		return mediaLibraryFolder;
	}

	public boolean isNewFolder() {
		return isNewFilter;
	}
}
