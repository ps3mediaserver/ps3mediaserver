package net.pms.medialibrary.commons.interfaces;

import net.pms.medialibrary.commons.enumarations.FileType;
import net.pms.medialibrary.commons.enumarations.ScanState;

public interface IFileScannerEventListener {
	void scanStateChanged(ScanState state);
	void itemInserted(FileType type);
}
