package net.pms.plugins.wrappers;

import javax.swing.Icon;
import javax.swing.JComponent;

import net.pms.external.ExternalListener;
import net.pms.plugins.PluginBase;

/**
 * Base wrapper class implementing all {@link PluginBase} methods
 */
@SuppressWarnings("deprecation")
public abstract class BaseWrapper implements PluginBase {
	private ExternalListener listener;

	/**
	 * Instantiates a new base wrapper.
	 *
	 * @param listener the listener
	 */
	public BaseWrapper(ExternalListener listener) {
		this.listener = listener;
	}

	/**
	 * Gets the external listener.
	 *
	 * @return the listener
	 */
	public ExternalListener getListener() {
		return listener;
	}

	/* (non-Javadoc)
	 * @see net.pms.plugins.PluginBase#getName()
	 */
	@Override
	public String getName() {
		return listener.name();
	}

	/* (non-Javadoc)
	 * @see net.pms.plugins.PluginBase#getVersion()
	 */
	@Override
	public String getVersion() {
		return "0";
	}

	/* (non-Javadoc)
	 * @see net.pms.plugins.PluginBase#getPluginIcon()
	 */
	@Override
	public Icon getPluginIcon() {
		return null;
	}

	/* (non-Javadoc)
	 * @see net.pms.plugins.PluginBase#getShortDescription()
	 */
	@Override
	public String getShortDescription() {
		return "";
	}

	/* (non-Javadoc)
	 * @see net.pms.plugins.PluginBase#getLongDescription()
	 */
	@Override
	public String getLongDescription() {
		return "";
	}

	/* (non-Javadoc)
	 * @see net.pms.plugins.PluginBase#getUpdateUrl()
	 */
	@Override
	public String getUpdateUrl() {
		return "";
	}

	/* (non-Javadoc)
	 * @see net.pms.plugins.PluginBase#getWebSiteUrl()
	 */
	@Override
	public String getWebSiteUrl() {
		return "";
	}

	/* (non-Javadoc)
	 * @see net.pms.plugins.PluginBase#initialize()
	 */
	@Override
	public void initialize() {
	}

	/* (non-Javadoc)
	 * @see net.pms.plugins.PluginBase#shutdown()
	 */
	@Override
	public void shutdown() {
		listener.shutdown();
	}

	/* (non-Javadoc)
	 * @see net.pms.plugins.PluginBase#getGlobalConfigurationPanel()
	 */
	@Override
	public JComponent getGlobalConfigurationPanel() {
		return listener.config();
	}

	/* (non-Javadoc)
	 * @see net.pms.plugins.PluginBase#saveConfiguration()
	 */
	@Override
	public void saveConfiguration() {
	}
	
	/* (non-Javadoc)
	 * @see net.pms.plugins.PluginBase#isPluginAvailable()
	 */
	@Override
	public boolean isPluginAvailable() {
		return true;
	}
}
