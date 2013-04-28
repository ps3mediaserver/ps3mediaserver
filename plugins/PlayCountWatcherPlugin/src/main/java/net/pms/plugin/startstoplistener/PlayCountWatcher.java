package net.pms.plugin.startstoplistener;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.RealFile;
import net.pms.plugin.startstoplistener.pcw.configuration.GlobalConfiguration;
import net.pms.plugin.startstoplistener.pcw.gui.GlobalConfigurationPanel;
import net.pms.plugins.StartStopListener;
import net.pms.util.PmsProperties;
import net.pms.medialibrary.commons.interfaces.IMediaLibraryStorage;
import net.pms.medialibrary.storage.MediaLibraryStorage;

public class PlayCountWatcher implements StartStopListener {
	private static final Logger log = LoggerFactory.getLogger(PlayCountWatcher.class);
	public static final ResourceBundle messages = ResourceBundle.getBundle("net.pms.plugin.startstoplistener.pcw.lang.messages");
	
	private Queue<QueueItem> playCache = new LinkedList<QueueItem>();
	private IMediaLibraryStorage storage;

	/** Holds only the project version. It's used to always use the maven build number to display the version in code */
	private static final PmsProperties properties = new PmsProperties();
	static {
		try {
			properties.loadFromResourceFile("/playcountwatcherplugin.properties", PlayCountWatcher.class);
		} catch (IOException e) {
			log.error("Could not load playcountwatcherplugin.properties", e);
		}
	}
	
	/** The global configuration is shared amongst all plugin instances. */
	private static final GlobalConfiguration globalConfig;
	static {
		globalConfig = new GlobalConfiguration();
		try {
			globalConfig.load();
		} catch (IOException e) {
			log.error("Failed to load global configuration", e);
		}
	}
	
	/** GUI */
	private GlobalConfigurationPanel pGlobalConfiguration;

	@Override
	public void donePlaying(DLNAMediaInfo media, DLNAResource resource) {
		if(log.isDebugEnabled()) log.debug("Done playing " + resource.getName());
		
		//lazy-load the storage
		if(storage == null){
			storage = MediaLibraryStorage.getInstance();
			if(storage == null) return;
		}
		
		//update the play count if 80% of the file has been played. remove it from the cache anyway as it finished playing
		//TODO: try to improve this behavior when pausing a stream
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
			int fullLengthSec = (int) media.getDurationInSeconds();
			int minPlayLogLength = (int) (fullLengthSec * ((double) globalConfig.getPercentPlayedRequired() / 100));
			if(log.isDebugEnabled()) log.debug(String.format("Stopped playing %s (%s) after %s seconds. Min play length for loging %ss", resource.getName(), resource.getInternalId(), playLengthSec, minPlayLogLength));
			if(playLengthSec > minPlayLogLength) {
				//TODO: insert the file with basic info (mencoder/ffmpeg but no plugins) if it hasn't been previously inserted into the library
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
		if(pGlobalConfiguration == null ) {
			pGlobalConfiguration = new GlobalConfigurationPanel(globalConfig);
		}
		pGlobalConfiguration.applyConfig();
		return pGlobalConfiguration;
	}

	@Override
	public String getName() {
		return messages.getString("PlayCountWatcher.Name");
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
		return properties.get("project.version");
	}

	@Override
	public Icon getPluginIcon() {
		return new ImageIcon(getClass().getResource("/playcountwatcher-32.png"));
	}

	@Override
	public String getShortDescription() {
		return messages.getString("PlayCountWatcher.ShortDescription");
	}

	@Override
	public String getLongDescription() {
		return messages.getString("PlayCountWatcher.LongDescription");
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
		
	}

	@Override
	public void saveConfiguration() {
		if(pGlobalConfiguration != null) {
			pGlobalConfiguration.updateConfiguration(globalConfig);
			try {
				globalConfig.save();
			} catch (IOException e) {
				log.error("Failed to save global configuration", e);
			}
		}
	}

	@Override
	public boolean isPluginAvailable() {
		return true;
	}
}