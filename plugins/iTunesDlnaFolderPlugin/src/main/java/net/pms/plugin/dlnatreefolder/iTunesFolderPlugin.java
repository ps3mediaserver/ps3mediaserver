package net.pms.plugin.dlnatreefolder;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.tree.MutableTreeNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.Platform;

import net.pms.dlna.DLNAResource;
import net.pms.dlna.RealFile;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.plugin.dlnatreefolder.itunes.configuration.InstanceConfiguration;
import net.pms.plugin.dlnatreefolder.itunes.gui.InstanceConfigurationPanel;
import net.pms.plugins.DlnaTreeFolderPlugin;
import net.pms.util.PmsProperties;
import net.pms.xmlwise.Plist;

public class iTunesFolderPlugin implements DlnaTreeFolderPlugin {
	private static final Logger log = LoggerFactory.getLogger(iTunesFolderPlugin.class);
	
	private String rootFolderName = "root";
	
	public static final ResourceBundle messages = ResourceBundle.getBundle("net.pms.plugin.dlnatreefolder.itunes.lang.messages");
	
	/** Holds only the project version. It's used to always use the maven build number in code */
	private static final PmsProperties properties = new PmsProperties();
	static {
		try {
			properties.loadFromResourceFile("/itunesfolderplugin.properties", iTunesFolderPlugin.class);
		} catch (IOException e) {
			log.error("Could not load itunesfolderplugin.properties", e);
		}
	}
	
	/** The instance configuration is shared amongst all plugin instances. */
	private InstanceConfiguration instanceConfig;

	/** GUI */
	private InstanceConfigurationPanel pInstanceConfiguration;

	@Override
	public JPanel getInstanceConfigurationPanel() {
		//make sure the instance configuration has been initialized;
		if(instanceConfig == null) {
			instanceConfig = new InstanceConfiguration();
		}
		
		//lazy initialize the configuration panel
		if(pInstanceConfiguration == null ) {
			pInstanceConfiguration = new InstanceConfigurationPanel(instanceConfig);
		}
		
		//make sure the iTunes file path points to an existing file
		if(instanceConfig.getiTunesFilePath() == null || !new File(instanceConfig.getiTunesFilePath()).exists()) {
			try {
				instanceConfig.setiTunesFilePath(getiTunesFile());
			} catch (Exception e) {
				log.error("Failed to resolve iTunes file path", e);
			}
		}
		pInstanceConfiguration.applyConfig();
		
		return pInstanceConfiguration;
	}

	@Override
	public DLNAResource getDLNAResource() {
		DLNAResource res = null;
		
		Map<String, Object> iTunesLib;
		ArrayList<?> Playlists;
		HashMap<?, ?> Playlist;
		HashMap<?, ?> Tracks;
		HashMap<?, ?> Track;
		ArrayList<?> PlaylistTracks;
		
		//make sure the iTunes file path points to an existing file
		if(instanceConfig.getiTunesFilePath() == null || !new File(instanceConfig.getiTunesFilePath()).exists()) {
			try {
				instanceConfig.setiTunesFilePath(getiTunesFile());
			} catch (Exception e) {
				log.error("Failed to resolve iTunes file path", e);
			}
		}

		try {
			String iTunesFile = instanceConfig.getiTunesFilePath();
			if(iTunesFile != null && (new File(iTunesFile)).exists()) {
				iTunesLib = Plist.load(URLDecoder.decode(iTunesFile, System.getProperty("file.encoding")));     // loads the (nested) properties.
				Tracks = (HashMap<?, ?>) iTunesLib.get("Tracks");       // the list of tracks
				Playlists = (ArrayList<?>) iTunesLib.get("Playlists");       // the list of Playlists
				VirtualFolder vf = new VirtualFolder(rootFolderName, null);
				for (Object item : Playlists) {
					Playlist = (HashMap<?, ?>) item;
					VirtualFolder pf = new VirtualFolder(Playlist.get("Name").toString(),null);
					PlaylistTracks = (ArrayList<?>) Playlist.get("Playlist Items");   // list of tracks in a playlist
					if (PlaylistTracks != null) {
						for (Object t : PlaylistTracks) {
							HashMap<?, ?> td = (HashMap<?, ?>) t;
							Track = (HashMap<?, ?>) Tracks.get(td.get("Track ID").toString());
							if (Track.get("Location").toString().startsWith("file://")) {
								URI tURI2 = new URI(Track.get("Location").toString());
								RealFile file = new RealFile(new File(URLDecoder.decode(tURI2.toURL().getFile(), "UTF-8")));
								pf.addChild(file);
							}	                                	}
					}
					vf.addChild(pf);
				}
				res = vf;
			} else {
				log.warn("Could not find the iTunes file");
			}
		} catch (Exception e) {
			log.error("Something wrong with the iTunes Library scan: ",e);
		}
		
		return res;
	}
	
	private String getiTunesFile() throws Exception {
		String line = null;
		String iTunesFile = null;
		if (Platform.isMac()) {
			Process prc = Runtime.getRuntime().exec("defaults read com.apple.iApps iTunesRecentDatabases");
			BufferedReader in = new BufferedReader(new InputStreamReader(prc.getInputStream()));

			// we want the 2nd line
			if ((line = in.readLine()) != null && (line = in.readLine()) != null) {
				line = line.trim(); // remove extra spaces
				line = line.substring(1, line.length() - 1); // remove quotes and spaces
				URI tURI = new URI(line);
				iTunesFile = URLDecoder.decode(tURI.toURL().getFile(), "UTF8");
			}
			if (in != null) {
				in.close();
			}
		} else if (Platform.isWindows()) {
			Process prc = Runtime.getRuntime().exec("reg query \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders\" /v \"My Music\"");
			BufferedReader in = new BufferedReader(new InputStreamReader(prc.getInputStream()));
			String location = null;
			while ((line = in.readLine()) != null) {
				final String LOOK_FOR = "REG_SZ";
				if (line.contains(LOOK_FOR)) {
					location = line.substring(line.indexOf(LOOK_FOR) + LOOK_FOR.length()).trim();
				}
			}
			if (in != null) {
				in.close();
			}
			if (location != null) {
				// add the itunes folder to the end
				location = location + "\\iTunes\\iTunes Music Library.xml";
				iTunesFile = location;
			} else {
				log.info("Could not find the My Music folder");
			}
		}

		return iTunesFile;
	}

	@Override
	public String getName() {
		return "iTunes";
	}
	
	@Override
	public void setDisplayName(String name){
		rootFolderName = name;
	}

	@Override
	public void loadInstanceConfiguration(String configFilePath) throws IOException {
		instanceConfig = new InstanceConfiguration();
		instanceConfig.load(configFilePath);
	}

	@Override
	public void saveInstanceConfiguration(String configFilePath) throws IOException {
		if(pInstanceConfiguration != null) {
			pInstanceConfiguration.updateConfiguration(instanceConfig);
			instanceConfig.save(configFilePath);
		}
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
    public MutableTreeNode getTreeNode() {
	    return null;
    }

	@Override
    public boolean isInstanceAvailable() {
		String iTunesFile = instanceConfig.getiTunesFilePath();
		if(isPluginAvailable() && iTunesFile != null && (new File(iTunesFile)).exists()) {
			return true;
	    }
		return false;
    }

	@Override
	public boolean isPluginAvailable() {
		if(System.getProperty("os.name").toLowerCase().indexOf("nix") < 0) {
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
		return messages.getString("iTunesFolderPlugin.ShortDescription");
	}

	@Override
	public String getLongDescription() {
		return messages.getString("iTunesFolderPlugin.LongDescription");
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
		return new ImageIcon(getClass().getResource("/itunes-32.png"));
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
		instanceConfig = new InstanceConfiguration();
	}

	@Override
	public void saveConfiguration() {
	}

	@Override
	public Icon getTreeNodeIcon() {
		return new ImageIcon(getClass().getResource("/itunes-16.png"));
	}
}
