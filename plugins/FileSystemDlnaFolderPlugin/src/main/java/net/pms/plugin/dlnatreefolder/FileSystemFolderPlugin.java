package net.pms.plugin.dlnatreefolder;

import java.io.IOException;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.tree.MutableTreeNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.dlna.DLNAResource;
import net.pms.plugin.dlnatreefolder.configuration.GlobalConfiguration;
import net.pms.plugin.dlnatreefolder.configuration.InstanceConfiguration;
import net.pms.plugin.dlnatreefolder.dlna.FileSystemResource;
import net.pms.plugin.dlnatreefolder.gui.ConfigurationPanel;
import net.pms.plugin.dlnatreefolder.gui.GlobalConfigurationPanel;
import net.pms.plugins.DlnaTreeFolderPlugin;
import net.pms.util.PmsProperties;

public class FileSystemFolderPlugin implements DlnaTreeFolderPlugin {	
	private static final Logger log = LoggerFactory.getLogger(FileSystemFolderPlugin.class);
	
	public static final ResourceBundle messages = ResourceBundle.getBundle("net.pms.plugin.dlnatreefolder.filesystemfolderplugin.lang.messages");
	protected static final PmsProperties properties = new PmsProperties();
	static {
		try {
			properties.loadFromResourceFile("/filesystemfolderplugin.properties", FileSystemFolderPlugin.class);
		} catch (IOException e) {
			log.error("Could not load filesystemfolderplugin.properties", e);
		}
	}
	protected static GlobalConfiguration globalConfig;	
	protected InstanceConfiguration instanceConfig;
	
	private ConfigurationPanel pInstanceConfiguration;
	private String rootFolderName = "root";
	private FileSystemResource fileSystemResource;

	private GlobalConfigurationPanel pGlobalConfiguration;

	@Override
	public JPanel getInstanceConfigurationPanel() {
		return pInstanceConfiguration;
	}

	@Override
	public DLNAResource getDLNAResource() {
		if(fileSystemResource == null){
			fileSystemResource = new FileSystemResource(rootFolderName, instanceConfig.getFolderPaths());
		}
		
		return fileSystemResource;
	}

	@Override
	public String getName() {
		return messages.getString("FileSystemFolderPlugin.Name");
	}
	
	@Override
	public void setDisplayName(String name){
		rootFolderName = name;
	}

	@Override
	public void loadInstanceConfiguration(String configFilePath) throws IOException {
		instanceConfig.LoadConfiguration(configFilePath);
		pInstanceConfiguration.setFolders(instanceConfig.getFolderPaths());
	}

	@Override
	public void saveInstanceConfiguration(String configFilePath) throws IOException {
		instanceConfig.setFolderPaths(pInstanceConfiguration.getFolders());
		instanceConfig.SaveConfiguration(configFilePath);
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
		return properties.get("project.version");
	}

	@Override
	public String getShortDescription() {
		return messages.getString("FileSystemFolderPlugin.ShortDescription");
	}

	@Override
	public String getLongDescription() {
		return messages.getString("FileSystemFolderPlugin.LongDescription");
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
		return new ImageIcon(getClass().getResource("/FileSystemFolder_icon-32.png"));
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
		if(globalConfig == null) {
			globalConfig = new GlobalConfiguration();
			try {
				globalConfig.load();
			} catch (IOException e) {
				log.error("Failed to load global configuration", e);
			}
		}
		
		instanceConfig = new InstanceConfiguration();		
		pInstanceConfiguration = new ConfigurationPanel();
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
	public Icon getTreeNodeIcon() {
		return new ImageIcon(getClass().getResource("/FileSystemFolder_icon-16.png"));
	}
}
