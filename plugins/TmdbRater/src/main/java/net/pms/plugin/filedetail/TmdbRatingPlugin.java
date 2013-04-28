package net.pms.plugin.filedetail;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.dlna.DLNAResource;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.medialibrary.commons.dataobjects.DOVideoFileInfo;
import net.pms.plugin.filedetail.tmdbrater.configuration.GlobalConfiguration;
import net.pms.plugin.filedetail.tmdbrater.dlna.RatingResource;
import net.pms.plugin.filedetail.tmdbrater.gui.GlobalConfigurationPanel;
import net.pms.plugins.FileDetailPlugin;
import net.pms.util.PmsProperties;

public class TmdbRatingPlugin implements FileDetailPlugin {
	private static final Logger log = LoggerFactory.getLogger(TmdbRatingPlugin.class);
	public static final ResourceBundle messages = ResourceBundle.getBundle("net.pms.plugin.filedetail.tmdbrater.lang.messages");
	private DOVideoFileInfo video;
	private String displayName;
	private static Icon ratingIcon;

	/** Holds only the project version. It's used to always use the maven build number in code */
	private static final PmsProperties properties = new PmsProperties();
	static {
		try {
			properties.loadFromResourceFile("/tmdratingplugin.properties", TmdbRatingPlugin.class);
		} catch (IOException e) {
			log.error("Could not load tmdratingplugin.properties", e);
		}
	}
	
	/** The global configuration is shared amongst all plugin instances. */
	public static final GlobalConfiguration globalConfig;
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
	
	public TmdbRatingPlugin(){
		if(ratingIcon == null){
			URL icon = getClass().getResource("/resources/images/star-16.png");
			if(icon != null) {
				ratingIcon = new ImageIcon(icon);
			}
		}
	}

	@Override
    public boolean isFolder() {
	    return true;
    }

	@Override
    public String getName() {
	    return messages.getString("TmdbRatingPlugin.Name");
    }

	@Override
    public Icon getTreeIcon() {
		Icon res = null;
		URL icon = getClass().getResource("/tmdb_rate-16.png");
		if(icon != null) {
			res = new ImageIcon(icon);
		}
		return res;
    }

	@Override
    public JPanel getConfigurationPanel() {
	    return null;
    }

	@Override
    public void loadConfiguration(String saveFilePath) throws IOException {
	    // do nothing
	    
    }

	@Override
    public void saveConfiguration(String saveFilePath) throws IOException {
	    // do nothing
    }

	@Override
    public DLNAResource getResource() {
	    VirtualFolder vf = new VirtualFolder(displayName, null);
	    for(int i = 20 ; i >= 0; i--){
		    vf.addChild(new RatingResource(video, ((float)i) / 2));
	    }
	    return vf;
    }

	@Override
    public void setVideo(DOVideoFileInfo video) {
	    this.video = video;
    }

	@Override
    public void setDisplayName(String displayName) {
	    this.displayName = displayName;
    }

	@Override
    public boolean isInstanceAvailable() {
	    if(TmdbHelper.getSession() != null){
	    	return true;
	    }
	    return false;
    }

	@Override
	public String getVersion() {
		return properties.get("project.version");
	}

	@Override
	public String getShortDescription() {
		return messages.getString("TmdbRatingPlugin.ShortDescription");
	}

	@Override
	public String getLongDescription() {
		return messages.getString("TmdbRatingPlugin.LongDescription");
	}

	@Override
	public void shutdown() {
		// do nothing
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
	public Icon getPluginIcon() {
		return new ImageIcon(getClass().getResource("/tmdb_rate-32.png"));
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
