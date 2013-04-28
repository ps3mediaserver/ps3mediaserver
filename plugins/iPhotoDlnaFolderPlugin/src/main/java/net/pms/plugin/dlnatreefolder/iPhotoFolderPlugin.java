package net.pms.plugin.dlnatreefolder;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.dlna.DLNAResource;
import net.pms.dlna.RealFile;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.plugins.DlnaTreeFolderPlugin;
import net.pms.xmlwise.Plist;

public class iPhotoFolderPlugin implements DlnaTreeFolderPlugin {
	private static final Logger log = LoggerFactory.getLogger(iPhotoFolderPlugin.class);
	private Properties properties = new Properties();
	protected static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("net.pms.plugin.dlnatreefolder.iphoto.lang.messages");
	
	private String rootFolderName = "root";

	public iPhotoFolderPlugin() {
		loadProperties();
	}

	@Override
	public JPanel getInstanceConfigurationPanel() {
		return null;
	}

	@Override
	public DLNAResource getDLNAResource() {
		DLNAResource res = null;
		
		Map<String, Object> iPhotoLib;
		ArrayList<?> ListofRolls;
		HashMap<?, ?> Roll;
		HashMap<?, ?> PhotoList;
		HashMap<?, ?> Photo;
		ArrayList<?> RollPhotos;

		try {
				Process prc = Runtime.getRuntime().exec("defaults read com.apple.iApps iPhotoRecentDatabases");  
				BufferedReader in = new BufferedReader(  
						new InputStreamReader(prc.getInputStream()));  
				String line = null;  
				if ((line = in.readLine()) != null) {
					line = in.readLine();		//we want the 2nd line
					line = line.trim();		//remove extra spaces	
					line = line.substring(1, line.length() - 1); // remove quotes and spaces
				}
				in.close();
				if (line != null) {
	 				URI tURI = new URI(line);
	 				iPhotoLib = Plist.load(URLDecoder.decode(tURI.toURL().getFile(), System.getProperty("file.encoding")));    // loads the (nested) properties.
					PhotoList = (HashMap<?, ?>) iPhotoLib.get("Master Image List");	// the list of photos
					ListofRolls = (ArrayList<?>) iPhotoLib.get("List of Rolls");	// the list of events (rolls)
					VirtualFolder vf = new VirtualFolder(rootFolderName, null);
					for (Object item : ListofRolls) {
						Roll = (HashMap<?, ?>) item;
						VirtualFolder rf = new VirtualFolder(Roll.get("RollName").toString(),null);
						RollPhotos = (ArrayList<?>) Roll.get("KeyList");	// list of photos in an event (roll)
						for (Object p : RollPhotos) {
							Photo = (HashMap<?, ?>) PhotoList.get(p);
							RealFile file = new RealFile(new File(Photo.get("ImagePath").toString()));
		       	                         	rf.addChild(file);
						}
						vf.addChild(rf);
		 			}
					res = vf;
				} else {
					log.warn("iPhoto folder not found !?");
				}
		} catch (Exception e) {
			log.error("Something wrong with the iPhoto Library scan: ", e);
           }
		return res;
	}

	@Override
	public String getName() {
		return "iPhoto";
	}
	
	@Override
	public void setDisplayName(String name){
		rootFolderName = name;
	}

	@Override
	public void loadInstanceConfiguration(String configFilePath) throws IOException {
		//do nothing
	}

	@Override
	public void saveInstanceConfiguration(String configFilePath) throws IOException {
		//do nothing
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
    public boolean isInstanceAvailable() {
	    return System.getProperty("os.name").toLowerCase().indexOf( "mac" ) >= 0;
    }

	@Override
	public boolean isPluginAvailable() {
	    return System.getProperty("os.name").toLowerCase().indexOf( "mac" ) >= 0;
	}

	@Override
	public String getVersion() {
		return properties.getProperty("project.version");
	}

	@Override
	public String getShortDescription() {
		return RESOURCE_BUNDLE.getString("iPhotoFolderPlugin.ShortDescription");
	}

	@Override
	public String getLongDescription() {
		return RESOURCE_BUNDLE.getString("iPhotoFolderPlugin.LongDescription");
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
		return new ImageIcon(getClass().getResource("/iphoto-32.png"));
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

	@Override
	public Icon getTreeNodeIcon() {
		return new ImageIcon(getClass().getResource("/iphoto-16.png"));
	}
	
	/**
	 * Loads the properties from the plugin properties file
	 */
	private void loadProperties() {
		String fileName = "/iphotofolderplugin.properties";
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
