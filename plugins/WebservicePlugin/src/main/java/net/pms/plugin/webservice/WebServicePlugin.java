package net.pms.plugin.webservice;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.PMS;
import net.pms.plugin.webservice.configuration.ConfigurationWebService;
import net.pms.plugin.webservice.medialibrary.LibraryWebService;
import net.pms.plugins.Plugin;

public class WebServicePlugin implements Plugin {
	private static final Logger log = LoggerFactory.getLogger(WebServicePlugin.class);
	protected static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("net.pms.plugin.webservice.webservicepluginmessages.messages");
	private Properties properties = new Properties();
	private static Object initializationLocker = new Object();
	private static Thread thRegister;
	
	private static ConfigurationWebService configurationWs;
	private String configurationWsName = "PmsConfiguration";
	
	private static LibraryWebService libraryWs;
	private String libraryWsName = "PmsLibrary";
	
	private String hostName;
	private int port = 54423;
	
	public WebServicePlugin() {
		loadProperties();
	}

	@Override
	public JComponent getGlobalConfigurationPanel() {
		String libraryEndPoint = "http://" + hostName + ":" + port + "/" + libraryWsName + "?wsdl";
		String configEndPoint = "http://" + hostName + ":" + port + "/" + configurationWsName + "?wsdl";
		return new JLabel(String.format("<html>%s<br><br>%s<br>%s</html>", RESOURCE_BUNDLE.getString("WebServicePlugin.2"), libraryEndPoint, configEndPoint));
	}

	@Override
	public String getName() {
		return RESOURCE_BUNDLE.getString("WebServicePlugin.1");
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
		return properties.getProperty("project.version");
	}

	@Override
	public Icon getPluginIcon() {
		return null;
	}

	@Override
	public String getShortDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLongDescription() {
		return null;
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
						configurationWs.bind(hostName, port, configurationWsName);
						
						libraryWs = new LibraryWebService();
						libraryWs.bind(hostName, port, libraryWsName);
					}
				});
				thRegister.start();
			}
		}
	}

	@Override
	public void saveConfiguration() {
	}
	
	/**
	 * Loads the properties from the plugin properties file
	 */
	private void loadProperties() {
		String fileName = "/webserviceplugin.properties";
		InputStream inputStream = getClass().getResourceAsStream(fileName);
		try {
			properties.load(inputStream);
		} catch (Exception e) {
			log.error("Failed to load properties", e);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					log.error("Failed to properly close stream properties", e);
				}
			}
		}
	}
}