package net.pms.medialibrary.commons.events;

import net.pms.medialibrary.commons.dataobjects.DOFilter;
import net.pms.medialibrary.commons.enumarations.FileType;

public interface LibraryShowListener {
	void show(DOFilter filter, FileType fileType);
}
