package net.pms.plugin.dlnatreefolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.tree.MutableTreeNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.dlna.AudiosFeed;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.ImagesFeed;
import net.pms.dlna.VideosFeed;
import net.pms.dlna.WebAudioStream;
import net.pms.dlna.WebVideoStream;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.plugins.DlnaTreeFolderPlugin;

public class WebFolderPlugin implements DlnaTreeFolderPlugin {
	private static final Logger log = LoggerFactory.getLogger(WebFolderPlugin.class);
	private Properties properties = new Properties();
	protected static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("net.pms.plugin.dlnatreefolder.webfolderplugin.lang.messages");
	private String rootFolderName = "root";
	
	public WebFolderPlugin() {
		loadProperties();
	}

	@Override
	public JPanel getInstanceConfigurationPanel() {
		return null;
	}

	@Override
	public DLNAResource getDLNAResource() {
		File webConf = new File("WEB.conf");
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
		return RESOURCE_BUNDLE.getString("WebFolderPlugin.Name");
	}

	@Override
	public void setDisplayName(String name) {
		rootFolderName = name;
	}

	@Override
	public void loadInstanceConfiguration(String configFilePath) throws IOException {
		// Do nothing
	}

	@Override
	public void saveInstanceConfiguration(String configFilePath) throws IOException {
		// Do nothing
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
    public boolean isAvailable() {
	    return true;
    }

	@Override
	public String getVersion() {
		return properties.getProperty("project.version");
	}

	@Override
	public String getShortDescription() {
		return RESOURCE_BUNDLE.getString("WebFolderPlugin.ShortDescription");
	}

	@Override
	public String getLongDescription() {
		return RESOURCE_BUNDLE.getString("WebFolderPlugin.Longescription");
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
	
	/**
	 * Loads the properties from the plugin properties file
	 */
	private void loadProperties() {
		String fileName = "/webfolderplugin.properties";
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
