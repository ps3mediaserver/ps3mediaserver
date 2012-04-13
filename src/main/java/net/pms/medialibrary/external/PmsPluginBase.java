package net.pms.medialibrary.external;

import javax.swing.JComponent;

/**
 * Base class shared by all pms-mlx plugins.
 * 
 * @author pw
 * 
 */
public interface PmsPluginBase {

	/**
	 * The name of the plugin.<br>
	 * For FileImportPlugins, the name should not be changed once it has been
	 * released as it is being used as an identifier.
	 * 
	 * @return plugin name
	 */
	public String getName();

	/**
	 * Version of the plugin. This number should be incremented for every
	 * release<br>
	 * If different versions of the same plugin are contained in the plugins
	 * folder, only the one with the highest version number will be loaded.
	 * 
	 * @return version of the plugin
	 */
	public int getVersion();

	/**
	 * Gets the description that will be shown in pms for the plugin. It should
	 * be localized.
	 * 
	 * @return Description of what the plugin does
	 */
	public String getDescription();

	/**
	 * Called when pms is being closed. When this method returns, the plugin
	 * must guarantee to have closed all threads it has created and released all
	 * resources.
	 */
	public void shutdown();

	/**
	 * Gets the graphical component to configure the plugin. If no configuration
	 * is required, return null.
	 * 
	 * @return JComponent for plugin configuration or null
	 */
	public JComponent getGlobalConfigurationPanel();
}
