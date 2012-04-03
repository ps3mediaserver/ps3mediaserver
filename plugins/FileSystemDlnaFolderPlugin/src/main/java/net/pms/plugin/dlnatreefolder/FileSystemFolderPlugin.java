package net.pms.plugin.dlnatreefolder;

import java.io.IOException;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.tree.MutableTreeNode;

import net.pms.dlna.DLNAResource;
import net.pms.medialibrary.external.DlnaTreeFolderPlugin;

public class FileSystemFolderPlugin implements DlnaTreeFolderPlugin {	
	protected static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("net.pms.plugin.dlnatreefolder.filesystemfolderpluginmessages.messages");
	
	private Configuration      config;
	private ConfigurationPanel configPanel;
	private String rootFolderName = "root";
	private FileSystemResource fileSystemResource;

	public FileSystemFolderPlugin() {
		config = new Configuration();
		configPanel = new ConfigurationPanel();
	}

	@Override
	public JPanel getConfigurationPanel() {
		return configPanel;
	}

	@Override
	public DLNAResource getDLNAResource() {
		if(fileSystemResource == null){
			fileSystemResource = new FileSystemResource(rootFolderName, config.getFolderPaths());
		}
		
		return fileSystemResource;
	}

	@Override
	public Icon getIcon() {
		return new ImageIcon(getClass().getResource("/FileSystemFolder_icon.png"));
	}

	@Override
	public String getName() {
		return RESOURCE_BUNDLE.getString("FileSystemFolderPlugin.1");
	}
	
	@Override
	public void setDisplayName(String name){
		rootFolderName = name;
	}

	@Override
	public void loadConfiguration(String configFilePath) throws IOException {
		config.LoadConfiguration(configFilePath);
		configPanel.setFolders(config.getFolderPaths());
	}

	@Override
	public void saveConfiguration(String configFilePath) throws IOException {
		config.setFolderPaths(configPanel.getFolders());
		config.SaveConfiguration(configFilePath);
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
		return 2;
	}

	@Override
	public String getDescription() {
		return RESOURCE_BUNDLE.getString("FileSystemFolderPlugin.2");
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
