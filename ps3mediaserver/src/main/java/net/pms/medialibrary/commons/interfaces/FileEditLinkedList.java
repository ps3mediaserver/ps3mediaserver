package net.pms.medialibrary.commons.interfaces;

import net.pms.medialibrary.commons.dataobjects.DOFileInfo;

public interface FileEditLinkedList {
	DOFileInfo getSelected();
	DOFileInfo selectNextFile();
	boolean hasNextFile();
	DOFileInfo selectPreviousFile();
	boolean hasPreviousFile();
}
