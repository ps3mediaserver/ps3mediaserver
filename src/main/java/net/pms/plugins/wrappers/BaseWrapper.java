package net.pms.plugins.wrappers;

import javax.swing.Icon;
import javax.swing.JComponent;

import net.pms.external.ExternalListener;
import net.pms.plugins.PluginBase;

public abstract class BaseWrapper implements PluginBase {
	private ExternalListener listener;
	
	public BaseWrapper(ExternalListener listener) {
		this.listener = listener;
	}

	public ExternalListener getListener() {
		return listener;
	}	

	@Override
	public String getName() {
		return listener.name();
	}

	@Override
	public String getVersion() {
		return "0";
	}

	@Override
	public Icon getPluginIcon() {
		return null;
	}

	@Override
	public String getShortDescription() {
		return "";
	}

	@Override
	public String getLongDescription() {
		return "";
	}

	@Override
	public String getUpdateUrl() {
		return "";
	}

	@Override
	public String getWebSiteUrl() {
		return "";
	}

	@Override
	public void initialize() {
	}

	@Override
	public void shutdown() {
		listener.shutdown();
	}

	@Override
	public JComponent getGlobalConfigurationPanel() {
		return listener.config();
	}

	@Override
	public void saveConfiguration() {		
	}
}
