package net.pms.plugin.dlnatreefolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.tree.MutableTreeNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.PMS;
import net.pms.dlna.AudiosFeed;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.ImagesFeed;
import net.pms.dlna.VideosFeed;
import net.pms.dlna.WebAudioStream;
import net.pms.dlna.WebVideoStream;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.plugin.dlnatreefolder.web.configuration.InstanceConfiguration;
import net.pms.plugin.dlnatreefolder.web.gui.InstanceConfigurationPanel;
import net.pms.plugins.DlnaTreeFolderPlugin;
import net.pms.util.PmsProperties;

public class WebFolderPlugin implements DlnaTreeFolderPlugin {
	private static final Logger log = LoggerFactory.getLogger(WebFolderPlugin.class);
	public static final ResourceBundle messages = ResourceBundle.getBundle("net.pms.plugin.dlnatreefolder.web.lang.messages");
	private String rootFolderName = "root";

	/** Holds only the project version. It's used to always use the maven build number in code */
	private static final PmsProperties properties = new PmsProperties();
	static {
		try {
			properties.loadFromResourceFile("/webfolderplugin.properties", WebFolderPlugin.class);
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
			if(instanceConfig.getFilePath() == null || !new File(instanceConfig.getFilePath()).exists()) {
				String profileDir = PMS.getConfiguration().getProfileDirectory();
				String defaultWebConf = profileDir + File.separatorChar + "web.conf";
				if(new File(defaultWebConf).exists()) {
					instanceConfig.setFilePath(defaultWebConf);
				}			
			}
			
			pInstanceConfiguration = new InstanceConfigurationPanel(instanceConfig.getFilePath());
		}
		pInstanceConfiguration.applyConfig();
		
		return pInstanceConfiguration;
	}

	@Override
	public DLNAResource getDLNAResource() {
		if( instanceConfig == null) {
			return null;
		}
		
		File webConf = new File(instanceConfig.getFilePath());
		DLNAResource res = new VirtualFolder(rootFolderName, null);
		
		if (webConf.exists()) {
			try {
				LineNumberReader br = new LineNumberReader(new InputStreamReader(new FileInputStream(webConf), "UTF-8"));
				String line = null;
				while ((line = br.readLine()) != null) {
					line = line.trim();
					if (line.length() > 0 && !line.startsWith("#") && line.indexOf("=") > -1) {
						String key = line.substring(0, line.indexOf("="));
						String value = line.substring(line.indexOf("=") + 1);
						String keys[] = parseFeedKey((String) key);
						try {
							if (keys[0].equals("imagefeed") || keys[0].equals("audiofeed") || keys[0].equals("videofeed") || keys[0].equals("audiostream") || keys[0].equals("videostream")) {

								String values[] = parseFeedValue((String) value);
								DLNAResource parent = null;
								if (keys[1] != null) {
									StringTokenizer st = new StringTokenizer(keys[1], ",");
									DLNAResource currentRoot = res;
									while (st.hasMoreTokens()) {
										String folder = st.nextToken();
										parent = currentRoot.searchByName(folder);
										if (parent == null) {
											parent = new VirtualFolder(folder, "");
											currentRoot.addChild(parent);
										}
										currentRoot = parent;
									}
								}
								if (parent == null) parent = res;
								if (keys[0].equals("imagefeed")) {
									parent.addChild(new ImagesFeed(values[0]));
								} else if (keys[0].equals("videofeed")) {
									parent.addChild(new VideosFeed(values[0]));
								} else if (keys[0].equals("audiofeed")) {
									parent.addChild(new AudiosFeed(values[0]));
								} else if (keys[0].equals("audiostream")) {
									parent.addChild(new WebAudioStream(values[0], values[1], values[2]));
								} else if (keys[0].equals("videostream")) {
									parent.addChild(new WebVideoStream(values[0], values[1], values[2]));
								}
							}

							// catch exception here and go with parsing
						} catch (ArrayIndexOutOfBoundsException e) {
							log.error("Error in line " + br.getLineNumber() + " of file WEB.conf", e);
						}
					}
				}
				br.close();
			} catch (Exception e) {
				log.error("Unexpected error in WEB.conf", e);
			}
		}
		return res;
	}

	private String[] parseFeedKey(String entry) {
		StringTokenizer st = new StringTokenizer(entry, ".");
		String results[] = new String[2];
		int i = 0;
		while (st.hasMoreTokens()) {
			results[i++] = st.nextToken();
		}
		return results;
	}

	private String[] parseFeedValue(String entry) {
		StringTokenizer st = new StringTokenizer(entry, ",");
		String results[] = new String[3];
		int i = 0;
		while (st.hasMoreTokens()) {
			results[i++] = st.nextToken();
		}
		return results;
	}

	@Override
	public Icon getTreeNodeIcon() {
		return new ImageIcon(getClass().getResource("/webfolder-16.png"));
	}

	@Override
	public String getName() {
		return messages.getString("WebFolderPlugin.Name");
	}

	@Override
	public void setDisplayName(String name) {
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
	    return true;
    }

	@Override
	public String getVersion() {
		return properties.get("project.version");
	}

	@Override
	public String getShortDescription() {
		return messages.getString("WebFolderPlugin.ShortDescription");
	}

	@Override
	public String getLongDescription() {
		return messages.getString("WebFolderPlugin.LongDescription");
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
		return new ImageIcon(getClass().getResource("/webfolder-32.png"));
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
	public boolean isPluginAvailable() {
		return true;
	}
}
