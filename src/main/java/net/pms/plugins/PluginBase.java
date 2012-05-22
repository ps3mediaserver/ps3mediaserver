package net.pms.plugins;

import javax.swing.Icon;
import javax.swing.JComponent;

/**
 * Base class shared by all pms plugins.<br>
 * This interface should never be implemented. Use the {@link Plugin} interface
 * instead if no additional functionality is required
 * 
 * @author pw
 * 
 */
public interface PluginBase {

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
	public String getVersion();

	/**
	 * Gets the icon that will be shown in the plugins tab. It should have a
	 * size of 32x32
	 * 
	 * @return the icon
	 */
	public Icon getPluginIcon();

	/**
	 * Gets the short description of the plugin that will be shown in the pms
	 * plugin panel<br>
	 * It should be localized.
	 * 
	 * @return Short description of what the plugin does
	 */
	public String getShortDescription();

	/**
	 * Gets the long description of the plugin that will be shown in the pms
	 * plugin dialog<br>
	 * It should be localized.
	 * 
	 * @return Long description of what the plugin does
	 */
	public String getLongDescription();

	/**
	 * Gets the url of the file containing information about the current plugin
	 * version<br>
	 * TODO: specify the format. Must contain version, download url, change log?
	 * 
	 * @return the update url
	 */
	public String getUpdateUrl();

	/**
	 * Gets the web site url.<br>
	 * This link can e.g. be a link to a forum post, a web site or null if no
	 * web site exists
	 * 
	 * @return the web site url as a string or null if no web site exists.
	 */
	public String getWebSiteUrl();

	/**
	 * When instantiating a new plugin by using its constructor without
	 * parameters, not all pms components will be ready. Don't do any calls to
	 * pms components from the constructor!<br>
	 * This method will be called once all components are ready to be used.<br>
	 * Except for {@link FinalizeTranscoderArgsListener} which will be
	 * initialized before the player
	 */
	public void initialize();

	/**
	 * Called when pms is being closed. When this method returns, the plugin
	 * must guarantee to have closed all threads it has created and released all
	 * resources.
	 */
	public void shutdown();

	/**
	 * Gets the graphical component to configure the plugin.<br>
	 * If no configuration is required, return null.
	 * 
	 * @return JComponent for plugin configuration or null
	 */
	public JComponent getGlobalConfigurationPanel();

	/**
	 * If the plugin can be configured with the JComponent returned by
	 * getGlobalConfigurationPanel(), the configuration has to be saved when
	 * this method is being called. This is, when the user clicks save in the
	 * configuration dialog.<br>
	 * The plugin can save its configuration how and where it wants. E.g. in
	 * PMS.getConfiguration().getProfileDirectory()
	 * 
	 * @see #getGlobalConfigurationPanel()
	 * 
	 */
	public void saveConfiguration();

	/**
	 * Checks if is plugin available. If this method returns true, all features
	 * of the plugin will be available for pms. If it returns false, the plugin
	 * will be shown as disabled in the list of plugins
	 * 
	 * @return true, if is plugin available, otherwise false
	 */
	public boolean isPluginAvailable();
}