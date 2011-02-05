package net.pms.external;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import net.pms.PMS;

public class ExternalFactory {
	
	private static List<ExternalListener> externalListeners;
	
	public static List<ExternalListener> getExternalListeners() {
		return externalListeners;
	}

	static {
		externalListeners = new ArrayList<ExternalListener>();
	}
	
	public static void registerListener(ExternalListener listener) {
		if (!externalListeners.contains(listener)) {
			externalListeners.add(listener);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void lookup() throws Exception {
		Enumeration<URL> launchers = PMS.get().getClass().getClassLoader().getResources("plugin");
		while (launchers.hasMoreElements()) {
			URL url = launchers.nextElement();
			InputStreamReader in = new InputStreamReader(url.openStream());
			char className [] = new char [512]; 
			in.read(className);
			in.close();
			String classNameLauncher = new String(className).trim();
			PMS.minimal("Detected plugin " + classNameLauncher);
			Class clLauncher = PMS.get().getClass().getClassLoader().loadClass(classNameLauncher);
			Object instance = clLauncher.newInstance();
			if (instance instanceof ExternalListener)
				registerListener((ExternalListener) instance);
		}
	}

}
