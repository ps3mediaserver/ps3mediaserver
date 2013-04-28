package net.pms.plugins;

import java.io.IOException;
import javax.swing.Icon;
import javax.swing.JPanel;

import net.pms.dlna.DLNAResource;

/**
 * Classes implementing this interface and packaged as plugins can be used in
 * pms to add folders that will show up on the renderer anywhere in the tree.<br>
 * When added to the plugins folder it will show up in the client context menu
 * of the main JTree. When opening the context menu on a folder and expanding
 * the selections in 'Add' the plugin will show up under the separator.<br>
 * 
 * 
 * @author pw
 * 
 */
public interface DlnaTreeFolderPlugin extends PluginBase {

	/***
	 * Gets the icon that will show in the context menu of the JTree in the Tree
	 * view when extending 'Add'. The icon should have a size of 16x16px.
	 * 
	 * @return the icon icon to show in the tree
	 */
	Icon getTreeNodeIcon();

	/***
	 * The name set through this method has to be used as the name for the root
	 * folder retrieved through getDLNAResource.<br>
	 * The reason this method exists is that the name can only be retrieved, but
	 * not set for a DLNAResource.
	 */
	void setDisplayName(String name);

	/***
	 * Gets the configuration panel that will be shown to the user for editing
	 * in the pms client.<br>
	 * If null is being returned, the edit option won't be enabled in the
	 * context menu of the pms client tree
	 * 
	 * @return the panel to configure the plugin or null if no configuration is
	 *         required
	 */
	JPanel getInstanceConfigurationPanel();

	/***
	 * Gets the file or folder that will be displayed on the renderer
	 * 
	 * @return the dlna resource that will be displayed when browsing the
	 *         renderer
	 */
	DLNAResource getDLNAResource();

	/***
	 * Save the configuration to the specified path to restore the same state as
	 * the plugin had when calling saveConfiguration. This mechanism allows a
	 * single configuration per plugin instance<br>
	 * The plugin can store its configuration how it wants. The plugin must be
	 * able to restore the same state as when this method was called, when
	 * calling loadConfiguration<br>
	 * If their is no configuration to save, do nothing.
	 * 
	 * @exception IOException
	 *                can be thrown when the file couldn't be saved
	 * @param saveFilePath
	 *            The path to which the configuration has to be saved to
	 */
	void saveInstanceConfiguration(String saveFilePath) throws IOException;

	/**
	 * Load the configuration from the specified path to restore the same state
	 * as the plugin had when calling saveConfiguration. This mechanism allows a
	 * single configuration per plugin instance<br>
	 * If their is no configuration to load, do nothing.
	 * 
	 * @param saveFilePath
	 *            path from which the plugin can load its configuration
	 * @exception IOException
	 *                can be thrown when the file couldn't be loaded
	 */
	void loadInstanceConfiguration(String saveFilePath) throws IOException;

	/**
	 * If true is being returned, the plugin will show up in the context menu.<br>
	 * This method lets check if all parameters for the plugin to work are met.<br>
	 * E.g. TmdbRater needs a valid TMDB user account to be configured.
	 * 
	 * @return true if the plugin can be used
	 */
	boolean isInstanceAvailable();
}
