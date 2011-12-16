package net.pms.medialibrary.commons.interfaces;

import net.pms.medialibrary.commons.dataobjects.DOManagedFile;
import net.pms.medialibrary.commons.dataobjects.DOScanReport;
import net.pms.medialibrary.commons.exceptions.ScanStateException;

public interface ILibraryManager {
	DOScanReport getScanState();
	void scanFolder(DOManagedFile mf);
	void cleanStorage();
	void resetStorage();
	void clearVideo();
	void clearPictures();
	void clearAudio();
	int getVideoCount();
	int getAudioCount();
	int getPictureCount();
	void pauseScan() throws ScanStateException;
	void stopScan();
	void unPauseScan() throws ScanStateException;
	void addFileScannerEventListener(IFileScannerEventListener listener);
	void addLibraryManagerEventListener(ILibraryManagerEventListener l);
}
