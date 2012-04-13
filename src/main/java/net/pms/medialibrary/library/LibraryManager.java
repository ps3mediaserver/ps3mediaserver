package net.pms.medialibrary.library;

import java.util.ArrayList;
import java.util.List;

import net.pms.medialibrary.commons.dataobjects.DOManagedFile;
import net.pms.medialibrary.commons.dataobjects.DOScanReport;
import net.pms.medialibrary.commons.enumarations.FileType;
import net.pms.medialibrary.commons.exceptions.InitialisationException;
import net.pms.medialibrary.commons.exceptions.ScanStateException;
import net.pms.medialibrary.commons.interfaces.IFileScannerEventListener;
import net.pms.medialibrary.commons.interfaces.ILibraryManager;
import net.pms.medialibrary.commons.interfaces.ILibraryManagerEventListener;
import net.pms.medialibrary.commons.interfaces.IMediaLibraryStorage;
import net.pms.medialibrary.scanner.FileScanner;
import net.pms.medialibrary.storage.MediaLibraryStorage;

public class LibraryManager implements ILibraryManager {
	
	private static LibraryManager instance;

	private IMediaLibraryStorage mediaLibraryStorage;
	private FileScanner fileScanner;
	private List<ILibraryManagerEventListener> libraryManagerEventListeners;
	
	private LibraryManager() throws InitialisationException{
		this.mediaLibraryStorage = MediaLibraryStorage.getInstance();
		this.fileScanner = FileScanner.getInstance();
		this.libraryManagerEventListeners = new ArrayList<ILibraryManagerEventListener>();
	}
	
	public static LibraryManager getInstance() throws InitialisationException{
		if(instance == null){
			try{
				instance = new LibraryManager();
			}catch (InitialisationException ex){
				throw new InitialisationException("Both components MediaLibraryStorage and FileScanner have to be configured before this method can be called", ex);
			}
		}
		return instance;
	}

	@Override
	public void cleanStorage() {
		mediaLibraryStorage.cleanStorage();
	}

	@Override
	public void clearAudio() {
		this.mediaLibraryStorage.deleteAudioFileInfo();
		for(ILibraryManagerEventListener l : this.libraryManagerEventListeners){
			l.itemCountChanged(getAudioCount(), FileType.AUDIO);
		}
	}

	@Override
	public void clearPictures() {
		this.mediaLibraryStorage.deletePicturesFileInfo();
		for(ILibraryManagerEventListener l : this.libraryManagerEventListeners){
			l.itemCountChanged(getPictureCount(), FileType.PICTURES);
		}
	}

	@Override
	public void resetStorage() {
		this.mediaLibraryStorage.reset();
		for(ILibraryManagerEventListener l : this.libraryManagerEventListeners){
			l.itemCountChanged(getVideoCount(), FileType.VIDEO);
			l.itemCountChanged(getAudioCount(), FileType.AUDIO);
			l.itemCountChanged(getPictureCount(), FileType.PICTURES);
		}
	}

	@Override
	public void clearVideo() {
		this.mediaLibraryStorage.deleteAllVideos();
		for(ILibraryManagerEventListener l : this.libraryManagerEventListeners){
			l.itemCountChanged(getVideoCount(), FileType.VIDEO);
		}
		
	}

	@Override
	public DOScanReport getScanState() {
		return this.fileScanner.getScanState();
	}

	@Override
	public void pauseScan() throws ScanStateException {
		this.fileScanner.pause();
	}

	@Override
	public void unPauseScan() throws ScanStateException {
		this.fileScanner.unPause();
	}

	@Override
	public void stopScan() {
		this.fileScanner.stop();
	}

	@Override
    public int getAudioCount() {
	    return this.mediaLibraryStorage.getAudioCount();
    }

	@Override
    public int getPictureCount() {
	    return this.mediaLibraryStorage.getPicturesCount();
    }

	@Override
    public int getVideoCount() {
	    return this.mediaLibraryStorage.getVideoCount();
    }

	@Override
    public void scanFolder(DOManagedFile mFolder) {
	    this.fileScanner.scan(mFolder);
    }

	@Override
	public void addFileScannerEventListener(IFileScannerEventListener listener) {
	    this.fileScanner.addFileScannerEventListener(listener);
    }
	
	@Override
	public void addLibraryManagerEventListener(ILibraryManagerEventListener l){
		if(!this.libraryManagerEventListeners.contains(l)){
			this.libraryManagerEventListeners.add(l);
		}
	}
}
