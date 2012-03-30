package net.pms.plugin.webservice;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.PMS;
import net.pms.external.ExternalListener;
import net.pms.plugin.webservice.configuration.ConfigurationWebService;
import net.pms.plugin.webservice.medialibrary.LibraryWebService;
import net.pms.util.PMSUtil;

public class WebServicePlugin implements ExternalListener {
	private static final Logger log = LoggerFactory.getLogger(WebServicePlugin.class);
	
	private ConfigurationWebService configurationWs;
	private String configurationWsName = "PmsConfiguration";
	
	private LibraryWebService libraryWs;
	private String libraryWsName = "PmsLibrary";
	
	private String hostName = "localhost";
	private int port = 54423;
	

	public WebServicePlugin() {
		hostName = getHostName();
		
		configurationWs = new ConfigurationWebService();
		configurationWs.bind(hostName, port, configurationWsName);
		
		libraryWs = new LibraryWebService();
		libraryWs.bind(hostName, port, libraryWsName);
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
		configurationWs.shutdown();
	}
	
	private static String getHostName(){
		String res = "localhost";
		try {
			NetworkInterface ni = null;
			InetAddress iafinal = null;
			Enumeration<NetworkInterface> enm = NetworkInterface.getNetworkInterfaces();
			InetAddress ia = null;
			boolean found = false;
			String fixedNetworkInterfaceName = PMS.getConfiguration().getNetworkInterface();
			NetworkInterface fixedNI = NetworkInterface.getByName(fixedNetworkInterfaceName);
			while (enm.hasMoreElements()) {
				ni = enm.nextElement();
				if (fixedNI != null)
					ni = fixedNI;
				if(log.isInfoEnabled()) log.info("Scanning network interface " + ni.getName() + " / " + ni.getDisplayName());
				if (!PMSUtil.isNetworkInterfaceLoopback(ni) && ni.getName() != null && (ni.getDisplayName() == null || !ni.getDisplayName().toLowerCase().contains("vmnet")) && !ni.getName().toLowerCase().contains("vmnet")) {
					
					Enumeration<InetAddress> addrs = ni.getInetAddresses();
					while (addrs.hasMoreElements()) {
						ia = addrs.nextElement();
						if (!(ia instanceof Inet6Address) && !ia.isLoopbackAddress()) {
							iafinal = ia;
							found = true;
							if (StringUtils.isNotEmpty(PMS.getConfiguration().getServerHostname())) {
								found = iafinal.equals(InetAddress.getByName(PMS.getConfiguration().getServerHostname()));
							}
							break;
						}
					}
					
				}
				if (found || fixedNI != null)
					break;
			}
			
			String hn = PMS.getConfiguration().getServerHostname();
			if (hn != null && hn.length() > 0) {
				InetAddress tempIA = InetAddress.getByName(hn);
				if (tempIA != null && ni != null && ni.equals(NetworkInterface.getByInetAddress(tempIA))) {
					res = tempIA.getHostAddress();
				} else
					res = hn;
			} else if (iafinal != null) {
				res = iafinal.getHostAddress();
			} else {
				res = "localhost";
		}
		}catch(IOException ex){
			log.error("failed to retrieve network interface to bind to");
		}
		 
		return res;
	}
}
