package net.pms.plugin.dlnatreefolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
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
import net.pms.medialibrary.external.DlnaTreeFolderPlugin;

public class WebFolderPlugin implements DlnaTreeFolderPlugin {
	private static final Logger log = LoggerFactory.getLogger(WebFolderPlugin.class);
	private String rootFolderName = "root";

	public WebFolderPlugin() {
		if(log.isDebugEnabled()) log.debug("WebSpecialFolder constructed");
	}

	@Override
	public JPanel getConfigurationPanel() {
		if(log.isDebugEnabled()) log.debug("Returning null configuration panel");
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
	public Icon getIcon() {
		ImageIcon res = new ImageIcon(getClass().getResource("/WebFolder_icon.png"));
		return res;
	}

	@Override
	public String getName() {
		return "Web";
	}

	@Override
	public void setDisplayName(String name) {
		rootFolderName = name;
	}

	@Override
	public void loadConfiguration(String configFilePath) throws IOException {
		// Do nothing
	}

	@Override
	public void saveConfiguration(String configFilePath) throws IOException {
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
	public int getVersion() {
		return 1;
	}

	@Override
	public String getDescription() {
		return "Online sources can be configured here";
	}

	@Override
	public void shutdown() {
		// do nothing
	}

	@Override
	public JComponent getGlobalConfigurationPanel() {
		return null;
	}
}
