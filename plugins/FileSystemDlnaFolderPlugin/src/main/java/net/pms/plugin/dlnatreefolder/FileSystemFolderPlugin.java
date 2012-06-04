/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2012  Ph.Waeber
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2
 * of the License only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
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
import net.pms.plugin.dlnatreefolder.fsfp.configuration.GlobalConfiguration;
import net.pms.plugin.dlnatreefolder.fsfp.configuration.InstanceConfiguration;
import net.pms.plugin.dlnatreefolder.fsfp.dlna.FileSystemResource;
import net.pms.plugin.dlnatreefolder.fsfp.gui.GlobalConfigurationPanel;
import net.pms.plugin.dlnatreefolder.fsfp.gui.InstanceConfigurationPanel;
import net.pms.plugins.DlnaTreeFolderPlugin;
import net.pms.util.PmsProperties;

/**
 * This plugin for the ps3 media server lets configure 0-n folders 
 * from the file system to be shared in a folder
 */
public class FileSystemFolderPlugin implements DlnaTreeFolderPlugin {
	private static final Logger log = LoggerFactory.getLogger(FileSystemFolderPlugin.class);
	
	/** Resource used for localization */
	public static final ResourceBundle messages = ResourceBundle.getBundle("net.pms.plugin.dlnatreefolder.fsfp.lang.messages");
	
	/** Holds only the project version. It's used to always use the maven build number in code */
	private static final PmsProperties properties = new PmsProperties();
	static {
		try {
			properties.loadFromResourceFile("/filesystemfolderplugin.properties", FileSystemFolderPlugin.class);
		} catch (IOException e) {
			log.error("Could not load filesystemfolderplugin.properties", e);
		}
	}
	
	/** The global configuration is shared amongst all plugin instances. */
	private static final GlobalConfiguration globalConfig;
	static {
		globalConfig = new GlobalConfiguration();
		try {
			globalConfig.load();
		} catch (IOException e) {
			log.error("Failed to load global configuration", e);
		}
	}
	
	/** The instance configuration is per instance */
	private InstanceConfiguration instanceConfig;
	
	private String rootFolderName = "root";
	private FileSystemResource fileSystemResource;
	
	/** GUI */
	private InstanceConfigurationPanel pInstanceConfiguration;
	private GlobalConfigurationPanel pGlobalConfiguration;

	/* (non-Javadoc)
	 * @see net.pms.plugins.DlnaTreeFolderPlugin#getInstanceConfigurationPanel()
	 */
	@Override
	public JPanel getInstanceConfigurationPanel() {
		return pInstanceConfiguration;
	}

	/* (non-Javadoc)
	 * @see net.pms.plugins.DlnaTreeFolderPlugin#getDLNAResource()
	 */
	@Override
	public DLNAResource getDLNAResource() {
		if(fileSystemResource == null){
			fileSystemResource = new FileSystemResource(rootFolderName, instanceConfig.getFolderPaths());
		}
		
		return fileSystemResource;
	}

	/* (non-Javadoc)
	 * @see net.pms.plugins.PluginBase#getName()
	 */
	@Override
	public String getName() {
		return messages.getString("FileSystemFolderPlugin.Name");
	}
	
	/* (non-Javadoc)
	 * @see net.pms.plugins.DlnaTreeFolderPlugin#setDisplayName(java.lang.String)
	 */
	@Override
	public void setDisplayName(String name){
		rootFolderName = name;
	}

	/* (non-Javadoc)
	 * @see net.pms.plugins.DlnaTreeFolderPlugin#loadInstanceConfiguration(java.lang.String)
	 */
	@Override
	public void loadInstanceConfiguration(String configFilePath) throws IOException {
		instanceConfig.load(configFilePath);
		pInstanceConfiguration.setFolders(instanceConfig.getFolderPaths());
	}

	/* (non-Javadoc)
	 * @see net.pms.plugins.DlnaTreeFolderPlugin#saveInstanceConfiguration(java.lang.String)
	 */
	@Override
	public void saveInstanceConfiguration(String configFilePath) throws IOException {
		instanceConfig.setFolderPaths(pInstanceConfiguration.getFolders());
		instanceConfig.save(configFilePath);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getName();
	}

	/* (non-Javadoc)
	 * @see net.pms.plugins.DlnaTreeFolderPlugin#getTreeNode()
	 */
	@Override
    public MutableTreeNode getTreeNode() {
	    return null;
    }

	/* (non-Javadoc)
	 * @see net.pms.plugins.PluginBase#isPluginAvailable()
	 */
	@Override
    public boolean isPluginAvailable() {
	    return true;
	}

	/* (non-Javadoc)
	 * @see net.pms.plugins.DlnaTreeFolderPlugin#isInstanceAvailable()
	 */
	@Override
	public boolean isInstanceAvailable() {
	    return true;
	}

	/* (non-Javadoc)
	 * @see net.pms.plugins.PluginBase#getVersion()
	 */
	@Override
	public String getVersion() {
		return properties.get("project.version");
	}

	/* (non-Javadoc)
	 * @see net.pms.plugins.PluginBase#getShortDescription()
	 */
	@Override
	public String getShortDescription() {
		return messages.getString("FileSystemFolderPlugin.ShortDescription");
	}

	/* (non-Javadoc)
	 * @see net.pms.plugins.PluginBase#getLongDescription()
	 */
	@Override
	public String getLongDescription() {
		return messages.getString("FileSystemFolderPlugin.LongDescription");
	}

	/* (non-Javadoc)
	 * @see net.pms.plugins.PluginBase#shutdown()
	 */
	@Override
	public void shutdown() {
		// do nothing
	}

	/* (non-Javadoc)
	 * @see net.pms.plugins.PluginBase#getGlobalConfigurationPanel()
	 */
	@Override
	public JComponent getGlobalConfigurationPanel() {
		if(pGlobalConfiguration == null ) {
			pGlobalConfiguration = new GlobalConfigurationPanel(globalConfig);
		}
		pGlobalConfiguration.applyConfig();
		return pGlobalConfiguration;
	}

	/* (non-Javadoc)
	 * @see net.pms.plugins.PluginBase#getPluginIcon()
	 */
	@Override
	public Icon getPluginIcon() {
		return new ImageIcon(getClass().getResource("/FileSystemFolder_icon-32.png"));
	}

	/* (non-Javadoc)
	 * @see net.pms.plugins.PluginBase#getUpdateUrl()
	 */
	@Override
	public String getUpdateUrl() {
		return null;
	}

	/* (non-Javadoc)
	 * @see net.pms.plugins.PluginBase#getWebSiteUrl()
	 */
	@Override
	public String getWebSiteUrl() {
		return "http://www.ps3mediaserver.org/";
	}

	/* (non-Javadoc)
	 * @see net.pms.plugins.PluginBase#initialize()
	 */
	@Override
	public void initialize() {
		instanceConfig = new InstanceConfiguration();
		pInstanceConfiguration = new InstanceConfigurationPanel();
	}

	/* (non-Javadoc)
	 * @see net.pms.plugins.PluginBase#saveConfiguration()
	 */
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

	/* (non-Javadoc)
	 * @see net.pms.plugins.DlnaTreeFolderPlugin#getTreeNodeIcon()
	 */
	@Override
	public Icon getTreeNodeIcon() {
		return new ImageIcon(getClass().getResource("/FileSystemFolder_icon-16.png"));
	}
}
