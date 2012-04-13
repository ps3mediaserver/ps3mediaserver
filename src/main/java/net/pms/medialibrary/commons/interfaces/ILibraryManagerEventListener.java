package net.pms.medialibrary.commons.interfaces;

import net.pms.medialibrary.commons.enumarations.FileType;

public interface ILibraryManagerEventListener {
	void itemCountChanged(int itemCount, FileType type);
}
