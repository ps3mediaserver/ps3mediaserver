package net.pms.medialibrary.commons.interfaces;

import net.pms.medialibrary.commons.dataobjects.DOFileInfo;
import net.pms.medialibrary.commons.dataobjects.DOManagedFile;

public interface IDataCollector {
	DOFileInfo get(DOManagedFile scanParams);
}
