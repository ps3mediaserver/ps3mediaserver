package net.pms.plugin.webservice;

import java.io.IOException;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.PMS;
import net.pms.plugin.webservice.configuration.GlobalConfiguration;
import net.pms.plugin.webservice.configurationws.ConfigurationWebService;
import net.pms.plugin.webservice.medialibraryws.LibraryWebService;
import net.pms.plugins.Plugin;
import net.pms.util.PmsProperties;

public class WebServicePlugin implements Plugin {
	private static final Logger log = LoggerFactory.getLogger(WebServicePlugin.class);
	public static final ResourceBundle messages = ResourceBundle.getBundle("net.pms.plugin.webservice.lang.messages");
	private static Object initializationLocker = new Object();
	private static Thread thRegister;
	
	private static ConfigurationWebService configurationWs;
	private String configurationWsName = "PmsConfiguration";
	
	private static LibraryWebService libraryWs;
	private String libraryWsName = "PmsLibrary";
	
	private String hostName;

	/** Holds only the project version. It's used to always use the maven build number in code */
	private static final PmsProperties properties = new PmsProperties();
	static {
		try {
			properties.loadFromResourceFile("/webserviceplugin.properties", WebServicePlugin.class);
		} catch (IOException e) {
			log.error("Could not load webserviceplugin.properties", e);
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
	
	/** GUI */
	private GlobalConfigurationPanel pGlobalConfiguration;

	@Override
	public JComponent getGlobalConfigurationPanel() {
		if(pGlobalConfiguration == null ) {
			pGlobalConfiguration = new GlobalConfigurationPanel(globalConfig);
		}
		pGlobalConfiguration.applyConfig();
		return pGlobalConfiguration;
	}

	@Override
	public String getName() {
		return messages.getString("WebServicePlugin.1");
	}

	@Override
	public void shutdown() {
		if(configurationWs != null) {
			configurationWs.shutdown();
			configurationWs = null;
		}
		if(libraryWs != null) {
			libraryWs.shutdown();
			libraryWs = null;
		}
		
		thRegister = null;
	}

	@Override
	public String getVersion() {
		return properties.get("project.version");
	}

	@Override
	public Icon getPluginIcon() {
		return new ImageIcon(getClass().getResource("/webservice-32.png"));
	}

	@Override
	public String getShortDescription() {
		return messages.getString("WebServicePlugin.ShortDescription");
	}

	@Override
	public String getLongDescription() {
		String libraryEndPoint = "http://" + hostName + ":" + globalConfig.getPort() + "/" + libraryWsName + "?wsdl";
		String configEndPoint = "http://" + hostName + ":" + globalConfig.getPort() + "/" + configurationWsName + "?wsdl";

		return messages.getString("WebServicePlugin.LongDescription") 
				+ "<br><br>" + libraryEndPoint + "<br>" + configEndPoint;
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
		//try to get the host name asynchronously as the server might not be ready when initializing
		synchronized (initializationLocker) {
			if(thRegister == null) {
				thRegister = new Thread(new Runnable() {
					
					@Override
					public void run() {
						while(hostName == null) {
							if(PMS.get().getServer() != null && PMS.get().getServer().getIafinal() != null) {
								hostName = PMS.get().getServer().getIafinal().getHostAddress();			
							}
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								//do nothing;
							}
						}
						configurationWs = new ConfigurationWebService();
						configurationWs.bind(hostName, globalConfig.getPort(), configurationWsName);
						
						libraryWs = new LibraryWebService();
						libraryWs.bind(hostName, globalConfig.getPort(), libraryWsName);
					}
				});
				thRegister.start();
			}
		}
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
	public boolean isPluginAvailable() {
		return true;
	}
}