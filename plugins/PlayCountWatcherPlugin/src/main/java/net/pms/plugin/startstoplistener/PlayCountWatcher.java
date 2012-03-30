package net.pms.plugin.startstoplistener;

import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.RealFile;
import net.pms.external.StartStopListener;
import net.pms.medialibrary.commons.interfaces.IMediaLibraryStorage;
import net.pms.medialibrary.storage.MediaLibraryStorage;

public class PlayCountWatcher implements StartStopListener {
	private static final Logger log = LoggerFactory.getLogger(PlayCountWatcher.class);
	
	private Queue<QueueItem> playCache = new LinkedList<QueueItem>();
	private IMediaLibraryStorage storage;

	@Override
	public void donePlaying(DLNAMediaInfo media, DLNAResource resource) {
		if(log.isDebugEnabled()) log.debug("Done playing " + resource.getName());
		
		//lazy-load the storage
		if(storage == null){
			storage = MediaLibraryStorage.getInstance();
			if(storage == null) return;
		}
		
		//update the play count if 80% of the file has been played. remove it from the cache anyway as it finished playing
		//TODO: check how this behaves when pausing a stream. Probably not well...
		int playLengthSec = 0;
		while(true){
			QueueItem item = playCache.poll();
			if(item == null){
				break;
			}

			if(item.resourcId.equals(resource.getInternalId())){
				if(resource instanceof RealFile){
					playLengthSec = (int)(new Date().getTime() - item.startDate.getTime()) / 1000;
					break;
				}
			}
		}
		
		if(playLengthSec > 0){
			String filePath = ((RealFile)resource).getFile().getAbsolutePath();
			int fullLengthSec = (int)media.getDurationInSeconds();
			int minPlayLogLength = (int) (fullLengthSec * 0.8);
			if(log.isDebugEnabled()) log.debug(String.format("Stopped playing %s (%s) after %s seconds. Min play length for loging %ss", resource.getName(), resource.getInternalId(), playLengthSec, minPlayLogLength));
			if(playLengthSec > minPlayLogLength){
				storage.updatePlayCount(filePath, playLengthSec, new Date());
				if(log.isInfoEnabled()) log.info(String.format("Updated play count for %s (%s) after %s seconds. Min play length for loging %ss", resource.getName(), resource.getInternalId(), playLengthSec, minPlayLogLength));
			}		
		}
	}

	@Override
	public void nowPlaying(DLNAMediaInfo media, DLNAResource resource) {
		if(log.isDebugEnabled()) log.debug(String.format("Started playing %s (%s)", resource.getName(), resource.getInternalId()));		
		playCache.add(new QueueItem(resource.getInternalId(), new Date()));
	}

	@Override
	public JComponent config() {
		return new JLabel("A file counts as played if it's being stopped or ends after more then 80% of its play time.");
	}

	@Override
	public String name() {
		return "Increments play counts for the media library";
	}

	@Override
	public void shutdown() {
		playCache.clear();
	}

	private class QueueItem{
		String resourcId;
		Date startDate;
		public QueueItem(String id, Date date) {
			resourcId = id;
			startDate = date;
		}
	}
}