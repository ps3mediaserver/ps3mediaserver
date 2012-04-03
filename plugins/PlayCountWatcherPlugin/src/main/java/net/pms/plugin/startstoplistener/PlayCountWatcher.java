package net.pms.plugin.startstoplistener;

import java.awt.BorderLayout;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.PMS;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.RealFile;
import net.pms.external.StartStopListener;
import net.pms.medialibrary.commons.interfaces.IMediaLibraryStorage;
import net.pms.medialibrary.storage.MediaLibraryStorage;

public class PlayCountWatcher implements StartStopListener {
	private static final Logger log = LoggerFactory.getLogger(PlayCountWatcher.class);

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("net.pms.plugin.startstoplistener.playcountwatcher.messages");
	
	private Queue<QueueItem> playCache = new LinkedList<QueueItem>();
	private IMediaLibraryStorage storage;
	
	private int percentPlayRequired = 80;
	
	public PlayCountWatcher() {
		Object pStr = PMS.getConfiguration().getCustomProperty("PlayCountWatcher_PercentPlayRequired");
		if(pStr != null && pStr instanceof String) {
			try {
				int pInt = Integer.parseInt((String) pStr);
				if(pInt > 0 && pInt <= 100) {
					percentPlayRequired = pInt;
				}
			} catch(NumberFormatException ex) {
				log.error(String.format("Failed to read value PlayCountWatcher=%s as Integer", pStr));
			}
		}
	}

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
			int minPlayLogLength = (int) (fullLengthSec * (percentPlayRequired / 100));
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
		JPanel p = new JPanel(new BorderLayout(0, 5));
		p.add(new JLabel(String.format(RESOURCE_BUNDLE.getString("PlayCountWatcher.1"), percentPlayRequired)), BorderLayout.NORTH);
		p.add(new JLabel(RESOURCE_BUNDLE.getString("PlayCountWatcher.3")), BorderLayout.SOUTH);
		return p;
	}

	@Override
	public String name() {
		return RESOURCE_BUNDLE.getString("PlayCountWatcher.2");
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