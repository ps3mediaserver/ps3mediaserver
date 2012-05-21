package net.pms.plugin.filedetail;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.dlna.DLNAResource;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.medialibrary.commons.dataobjects.DOVideoFileInfo;
import net.pms.plugins.FileDetailPlugin;
import net.pms.plugins.TreeEntry;

public class TmdbRatingPlugin implements FileDetailPlugin {
	private static final Logger log = LoggerFactory.getLogger(TmdbRatingPlugin.class);
	protected static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("net.pms.plugin.filedetail.tmdbrater.lang.messages");
	private Properties properties = new Properties();
	private DOVideoFileInfo video;
	private String displayName;
	private static Icon ratingIcon;
	
	public TmdbRatingPlugin(){
		loadProperties();
		
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
	    return RESOURCE_BUNDLE.getString("TmdbRatingPlugin.Name");
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
    public MutableTreeNode getTreeNode() {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(getName());
	    for(int i = 20 ; i >= 0; i--){
	    	DefaultMutableTreeNode cn = new DefaultMutableTreeNode(new TreeEntry(String.valueOf(((float)i) / 2).replace(".0", ""), ratingIcon));
	    	node.add(cn);	    	
	    }
		
	    return node;
    }

	@Override
    public boolean isAvailable() {
	    if(TmdbHelper.getSession() != null){
	    	return true;
	    }
	    return false;
    }

	@Override
	public String getVersion() {
		return properties.getProperty("project.version");
	}

	@Override
	public String getShortDescription() {
		return RESOURCE_BUNDLE.getString("TmdbRatingPlugin.ShortDescription");
	}

	@Override
	public String getLongDescription() {
		return RESOURCE_BUNDLE.getString("TmdbRatingPlugin.LongDescription");
	}

	@Override
	public void shutdown() {
		// do nothing
	}

	@Override
	public JComponent getGlobalConfigurationPanel() {
		return null;
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
	}
	
	/**
	 * Loads the properties from the plugin properties file
	 */
	private void loadProperties() {
		String fileName = "/tmdratingplugin.properties";
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
