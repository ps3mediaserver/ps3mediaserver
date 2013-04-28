package net.pms.plugins;

import java.io.IOException;

import javax.swing.Icon;
import javax.swing.JPanel;

import net.pms.dlna.DLNAResource;
import net.pms.medialibrary.commons.dataobjects.DOVideoFileInfo;

/**
 * Classes implementing this interface and packaged as plugins can be used to
 * add additional configuration properties when displaying a file as a folder.
 * 
 * @author pw
 * 
 */
public interface FileDetailPlugin extends PluginBase {
	/**
	 * Must return true if it's a folder, false if it's a file
	 * 
	 * @return true if it the plugin adds a folder
	 */
	boolean isFolder();

	/**
	 * Get the icon (16x16) that will show up in the JTree
	 * 
	 * @return the icon to show in the JTree
	 */
	Icon getTreeIcon();

	/**
	 * Gets the panel to configure plugin properties.<br>
	 * If null is being returned the menu item to configure the plugin will be
	 * disabled.
	 * 
	 * @return The panel for plugin configuration or null
	 */
	JPanel getConfigurationPanel();

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
	void saveConfiguration(String saveFilePath) throws IOException;

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
	void loadConfiguration(String saveFilePath) throws IOException;

	/***
	 * the name set through this method has to be used as the name for the root
	 * folder retrieved through getDLNAResource. This method is required as the
	 * name of a DLNAResource can't be set.<br>
	 * This method will be called right before calling getResource
	 */
	void setDisplayName(String name);

	/***
	 * Sets the video file information for the video currently being browsed.<br>
	 * This method will be called right before calling getResource
	 * 
	 * @param videoFileInfo
	 *            object containing all information about the video being
	 *            browsed
	 */
	void setVideo(DOVideoFileInfo videoFileInfo);

	/***
	 * Gets the DLNAResource that will show up on the renderer
	 * 
	 * @return DLNAResource to show
	 */
	DLNAResource getResource();

	/**
	 * If true is being returned, the plugin will show up in the context menu.<br>
	 * This method lets check if all parameters for the plugin to work are met.<br>
	 * E.g. the tmdb rating plugin will return false if no credentials have been
	 * set
	 * 
	 * @return true if the plugin can be used
	 */
	boolean isInstanceAvailable();
}
