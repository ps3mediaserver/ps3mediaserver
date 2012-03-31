package net.pms.plugin.webservice;

import javax.swing.JComponent;
import javax.swing.JLabel;

import net.pms.PMS;
import net.pms.external.ExternalListener;
import net.pms.plugin.webservice.configuration.ConfigurationWebService;
import net.pms.plugin.webservice.medialibrary.LibraryWebService;

public class WebServicePlugin implements ExternalListener {
	private static Object initializationLocker = new Object();
	private static Thread thRegister;
	
	private static ConfigurationWebService configurationWs;
	private String configurationWsName = "PmsConfiguration";
	
	private static LibraryWebService libraryWs;
	private String libraryWsName = "PmsLibrary";
	
	private String hostName;
	private int port = 54423;

	public WebServicePlugin() {
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
	public JComponent config() {
		String libraryEndPoint = "http://" + hostName + ":" + port + "/" + libraryWsName + "?wsdl";
		String configEndPoint = "http://" + hostName + ":" + port + "/" + configurationWsName + "?wsdl";
		return new JLabel(String.format("<html>This plugin exposes webservices and can't be configured.<br><br>%s<br>%s</html>", libraryEndPoint, configEndPoint));
	}

	@Override
	public String name() {
		return "Web service plugin";
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
}