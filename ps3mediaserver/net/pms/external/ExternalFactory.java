package net.pms.external;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
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
	
	public static void lookup() {
		File pluginDirectory = new File(PMS.getConfiguration().getPluginDirectory());
		PMS.minimal("Loading plugins from " + pluginDirectory.getAbsolutePath());

		if (!pluginDirectory.exists()) {
			// FIXME: should be a warning or error
			PMS.minimal("Plugin directory doesn't exist: " + pluginDirectory);
			return;
		}

		if (!pluginDirectory.isDirectory()) {
			// FIXME: should be a warning or error
			PMS.minimal("Plugin directory is not a directory: " + pluginDirectory);
			return;
		}
		   
		File[] jarFiles = pluginDirectory.listFiles(
			new FileFilter() {
				public boolean accept(File file) {
					return file.isFile() && file.getName().toLowerCase().endsWith(".jar");
				}
			}
		);

		int nJars = jarFiles.length;

		if (nJars == 0) {
			PMS.minimal("No plugins found");
			return;
		}

		List<URL> jarURLList = new ArrayList<URL>();

		for (int i = 0; i < nJars; ++i) {
			try {
				jarURLList.add(jarFiles[i].toURI().toURL());
			} catch (MalformedURLException e) {
				PMS.error("Can't convert file path " + jarFiles[i] + " to URL", e);
			}
		}

		URL[] jarURLs = new URL[jarURLList.size()];
		jarURLList.toArray(jarURLs);

		URLClassLoader classLoader = new URLClassLoader(jarURLs);
		Enumeration<URL> resources;

		try {
			resources = classLoader.getResources("plugin");
		} catch (IOException e) {
			PMS.error("Can't load plugin resources", e);
			return;
		}

		while (resources.hasMoreElements()) {
			URL url = resources.nextElement();
			try {
				InputStreamReader in = new InputStreamReader(url.openStream());
				char[] name = new char [512]; 
				in.read(name);
				in.close();
				String pluginMainClassName = new String(name).trim();
				PMS.minimal("Found plugin: " + pluginMainClassName);
				Object instance = classLoader.loadClass(pluginMainClassName).newInstance();
				if (instance instanceof ExternalListener)
					registerListener((ExternalListener) instance);
			} catch (Exception e) {
				PMS.error("Error loading plugin", e);
			}
		}
	}
}
