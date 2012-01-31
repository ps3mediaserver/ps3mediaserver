package net.pms.medialibrary.scanner;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.Messages;
import net.pms.medialibrary.commons.dataobjects.DOFileInfo;
import net.pms.medialibrary.commons.dataobjects.DOManagedFile;
import net.pms.medialibrary.commons.dataobjects.DOScanReport;
import net.pms.medialibrary.commons.enumarations.FileType;
import net.pms.medialibrary.commons.enumarations.ScanState;
import net.pms.medialibrary.commons.exceptions.InitialisationException;
import net.pms.medialibrary.commons.exceptions.ScanStateException;
import net.pms.medialibrary.commons.interfaces.IFileScannerEventListener;
import net.pms.medialibrary.commons.interfaces.IMediaLibraryStorage;
import net.pms.medialibrary.storage.MediaLibraryStorage;

public class FileScanner implements Runnable{
	public static FileScanner instance;

	private static final Logger log = LoggerFactory.getLogger(FileScanner.class);
	private static int nbScans = 0;
	
	private Queue<DOManagedFile> directoryPaths;
	private Thread scanThread;
	private ScanState scanState = ScanState.IDLE;
	private int nbScannedItems;
	private int nbItemsToScan;
	private FullDataCollector dataCollector;
	private IMediaLibraryStorage mediaLibraryStorage;
	private Object scanThreadPause;
	private List<IFileScannerEventListener> fileScannerEventListeners;
	
	public int updateIntervalDays = 100;
	
	private FileScanner() throws InitialisationException{
		directoryPaths = new ConcurrentLinkedQueue<DOManagedFile>();
		scanThread = new Thread(this);
		nbScannedItems = 0;
		nbItemsToScan = 0;
		dataCollector = FullDataCollector.getInstance();
		mediaLibraryStorage = MediaLibraryStorage.getInstance();
		fileScannerEventListeners = new ArrayList<IFileScannerEventListener>();
		scanThreadPause = new Object();
	}
	
	private synchronized void enqueueManagedFile(DOManagedFile mf){
		directoryPaths.add(mf);
	}
	
	private synchronized DOManagedFile dequeueManagedFile(){
		return directoryPaths.poll();
	}

	public static synchronized FileScanner getInstance() throws InitialisationException {
	    if(instance == null){
	    	try{
	    		instance = new FileScanner();
	    	}catch(InitialisationException ex){
	    		throw new InitialisationException("Both components FullDataCollector and MediaLibraryStorage have to be configured before this method can be called", ex);	    	
	    	}
	    }
	    return instance;
    }
	
	public DOScanReport getScanState(){
		return new DOScanReport(scanState, nbScannedItems, nbItemsToScan);
	}

	public void scan(DOManagedFile mFolder) {
		File folderToScan = new File(mFolder.getPath());
		if (folderToScan.isDirectory()) {
			net.pms.PMS.get().getFrame().setStatusLine(String.format(Messages.getString("ML.FileScanner.ScanFolder"), folderToScan.getAbsoluteFile()));

			File[] childPaths = folderToScan.listFiles();
			if(childPaths != null) {
				for (int i = 0; i < childPaths.length; i++) {
					
					File currentFile = childPaths[i];
					
					if(currentFile.isHidden()) {
						continue;
					}
					
					DOManagedFile tmpFile = new DOManagedFile(mFolder.isWatchEnabled(), currentFile.toString(), 
							mFolder.isVideoEnabled(), mFolder.isAudioEnabled(), mFolder.isPicturesEnabled(),
							mFolder.isSubFoldersEnabled(), mFolder.isFileImportEnabled(), mFolder.getFileImportTemplate());
					
					if (currentFile.isFile()) {
						enqueueManagedFile(tmpFile);
						synchronized (scanState) {
							if (scanState != ScanState.STARTING && scanState != ScanState.RUNNING) {
								changeScanState(ScanState.STARTING);
								scanThread = new Thread(this);
								scanThread.setName("scan" + nbScans++);
								scanThread.start();
							
							}
						}
					} else if (currentFile.isDirectory() && mFolder.isSubFoldersEnabled()) {
						scan(tmpFile);
					}
				}
			} else {
				log.debug("No children found for folder " + folderToScan.getAbsolutePath());
			}
		}
	}
	
	public void pause() throws ScanStateException{	
		if(log.isDebugEnabled()) log.debug("pause() called.");
		if(scanState != ScanState.RUNNING){
			throw new ScanStateException(ScanState.RUNNING, scanState, "The pause() method can only be called when the ScanState==RUNNING. current state="
					+ scanState); 
		}
		changeScanState(ScanState.PAUSING);
		if(log.isDebugEnabled()) log.debug("Pausing set. Waiting for scan thread to pause.");
	}
	
	public void unPause() throws ScanStateException{	
		if(log.isDebugEnabled()) log.debug("pause() called.");
		if(scanState != ScanState.PAUSED){
			throw new ScanStateException(ScanState.PAUSED, scanState, "The pause() method can only be called when the ScanState==PAUSED. current state="
					+ scanState); 
		}
		synchronized (scanThreadPause) {
			scanThreadPause.notify();
        }
		if(log.isDebugEnabled()) log.debug("Pausing set. Waiting for scan thread to pause.");
	}
	
	
	public void stop(){	
		if(log.isDebugEnabled()) log.debug("stop() called.");
		changeScanState(ScanState.STOPPING);
		if(log.isDebugEnabled()) log.debug("Stopping set. Waiting for scan thread to terminate.");
		synchronized (scanThreadPause) {
			scanThreadPause.notifyAll();
        }
		try{
			scanThread.join();
			if(log.isDebugEnabled()) log.debug("Stopped! Scan thread terminated properly.");		
		}catch(InterruptedException ex){
			if(log.isDebugEnabled()) log.debug("Stopped! Terminated by a InterruptedException.");								
		}
	}
	
	@Override
	public void run() {
		changeScanState(ScanState.RUNNING);
		// Handle each directory in the list
		DOManagedFile mf;
		int nbFilesAdded = 0;
		//Calendar lastGetDate = Calendar.getInstance();
		while ((mf = dequeueManagedFile()) != null) {
			File f = new File(mf.getPath());
			if (f.isFile()) {
					// check if we have to pause or stop the thread
					if (scanState == ScanState.PAUSING) {
						try {
							if(log.isInfoEnabled()) log.info("Scan paused");
							net.pms.PMS.get().getFrame().setStatusLine("Scan paused");
							changeScanState(ScanState.PAUSED);
							synchronized (scanThreadPause) {
								scanThreadPause.wait();
							}
							net.pms.PMS.get().getFrame().setStatusLine("Restarted scan");
							
							if (scanState == ScanState.STOPPING) {
								break;
							} else {
								changeScanState(ScanState.RUNNING);
								if(log.isInfoEnabled()) log.info("Scan started after pause");
							}
						} catch (InterruptedException ex) {
							log.error("Scan stopped because pause has been interrupted by a Interrupt.", ex);
							break;
						}
					} else if (scanState == ScanState.STOPPING) {
						break;
					}

					//Only update files if they're older then the configured value
					Date dateLastUpdate = mediaLibraryStorage.getFileInfoLastUpdated(f.getAbsolutePath());
					Calendar comp = Calendar.getInstance();
					comp.add(Calendar.DATE, -updateIntervalDays);
					if (dateLastUpdate.before(comp.getTime())) {
						// retrieve file info
						DOFileInfo fileInfo = null;
						try {
							fileInfo = dataCollector.get(new DOManagedFile(mf.isWatchEnabled(), mf.getPath().toString(), mf.isVideoEnabled(), mf.isAudioEnabled(), mf
						        .isPicturesEnabled(), mf.isSubFoldersEnabled(), mf.isFileImportEnabled(), mf.getFileImportTemplate()));
						} catch(Throwable t) {
							log.error("Failed to collect info for " + mf.getPath(), t);
							continue;
						}
						// insert file info if we were able to retrieve it
						if (fileInfo != null) {
							if(mediaLibraryStorage.getFileInfoLastUpdated(f.getAbsolutePath()).equals(new Date(0))){
    							mediaLibraryStorage.insertFileInfo(fileInfo);
    							nbFilesAdded++;
    							for(IFileScannerEventListener l : fileScannerEventListeners){
    								l.itemInserted(FileType.VIDEO);
    							}
							}
						} else {
							if(log.isDebugEnabled()) log.debug("Couldn't read " + f);
						}
					}
			}
			if (scanState == ScanState.STOPPING) {
				if(log.isInfoEnabled()) log.info("Scan stopped after stopping request");
				break;
			}
		}

		net.pms.PMS.get().getFrame().setStatusLine(String.format(Messages.getString("ML.Messages.ScanFinished"), String.valueOf(nbFilesAdded)));

		if(log.isInfoEnabled()) log.info("Finished scanning " + nbFilesAdded + " files");
		changeScanState(ScanState.IDLE);
	}

	public void addFileScannerEventListener(IFileScannerEventListener listener) {
		fileScannerEventListeners.add(listener);
    }
	
	private void changeScanState(ScanState state){
		synchronized (scanState) {
			scanState = state;
			for(IFileScannerEventListener l : fileScannerEventListeners) {
				l.scanStateChanged(scanState);
			}
		}
	}
}