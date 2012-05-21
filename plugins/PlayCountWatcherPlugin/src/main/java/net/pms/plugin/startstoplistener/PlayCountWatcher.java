package net.pms.plugin.startstoplistener;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Queue;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.PMS;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.RealFile;
import net.pms.plugins.StartStopListener;
import net.pms.medialibrary.commons.interfaces.IMediaLibraryStorage;
import net.pms.medialibrary.storage.MediaLibraryStorage;

public class PlayCountWatcher implements StartStopListener {
	private static final Logger log = LoggerFactory.getLogger(PlayCountWatcher.class);
	private Properties properties = new Properties();
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("net.pms.plugin.startstoplistener.playcountwatcher.lang.messages");
	
	private Queue<QueueItem> playCache = new LinkedList<QueueItem>();
	private IMediaLibraryStorage storage;
	
	private double percentPlayRequired = 80;
	
	public PlayCountWatcher() {
		loadProperties();
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
	public JComponent getGlobalConfigurationPanel() {
		return null;
	}

	@Override
	public String getName() {
		return RESOURCE_BUNDLE.getString("PlayCountWatcher.Name");
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

	@Override
	public String getVersion() {
		return properties.getProperty("project.version");
	}

	@Override
	public Icon getPluginIcon() {
		return new ImageIcon(getClass().getResource("/playcountwatcher-32.png"));
	}

	@Override
	public String getShortDescription() {
		return RESOURCE_BUNDLE.getString("PlayCountWatcher.ShortDescription");
	}

	@Override
	public String getLongDescription() {
		return String.format(RESOURCE_BUNDLE.getString("PlayCountWatcher.LongDescription"), percentPlayRequired);
	}

	@Override
	public String getUpdateUrl() {
		return null;
	}

	@Override
	public String getWebSiteUrl() {
		return "http://www.ps3mediaserver.org/";
	}

	@Override
	public void initialize() {
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
	public void saveConfiguration() {
	}
	
	/**
	 * Loads the properties from the plugin properties file
	 */
	private void loadProperties() {
		String fileName = "/playcountwatcherplugin.properties";
		InputStream inputStream = getClass().getResourceAsStream(fileName);
		try {
			properties.load(inputStream);
		} catch (Exception e) {
			log.error("Failed to load properties", e);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					log.error("Failed to properly close stream properties", e);
				}
			}
		}
	}
}