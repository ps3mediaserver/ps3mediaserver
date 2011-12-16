package net.pms.plugin.dlnatreefolder;

import java.io.IOException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.tree.MutableTreeNode;

import net.pms.dlna.DLNAResource;
import net.pms.medialibrary.external.DlnaTreeFolderPlugin;

public class FileSystemFolderPlugin implements DlnaTreeFolderPlugin {
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
		return "File system";
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
		return 1;
	}

	@Override
	public String getDescription() {
		return "Lets add one or more folders to the folders that will show up on the renderer. They will show exactly what's contained in the shared folders and update in real time";
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
