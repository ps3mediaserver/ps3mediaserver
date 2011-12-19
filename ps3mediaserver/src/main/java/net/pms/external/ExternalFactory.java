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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExternalFactory {
	private static final Logger logger = LoggerFactory.getLogger(ExternalFactory.class);
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
		logger.info("Searching for plugins in " + pluginDirectory.getAbsolutePath());

		if (!pluginDirectory.exists()) {
			logger.warn("Plugin directory doesn't exist: " + pluginDirectory);
			return;
		}

		if (!pluginDirectory.isDirectory()) {
			logger.warn("Plugin directory is not a directory: " + pluginDirectory);
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
			logger.info("No plugins found");
			return;
		}

		List<URL> jarURLList = new ArrayList<URL>();

		for (int i = 0; i < nJars; ++i) {
			try {
				jarURLList.add(jarFiles[i].toURI().toURL());
			} catch (MalformedURLException e) {
				logger.error("Can't convert file path " + jarFiles[i] + " to URL", e);
			}
		}

		URL[] jarURLs = new URL[jarURLList.size()];
		jarURLList.toArray(jarURLs);

		// specify the parent classloader being PMS to include the required plugin interface definitions
		// for the classloader. If this isn't being set, a ClassNotFoundException is being raised
		// because the interface implemented by the plugin can't be resolved.
		URLClassLoader classLoader = new URLClassLoader(jarURLs, PMS.class.getClassLoader());
		Enumeration<URL> resources;

		try {
			resources = classLoader.getResources("plugin");
		} catch (IOException e) {
			logger.error("Can't load plugin resources", e);
			return;
		}

		while (resources.hasMoreElements()) {
			URL url = resources.nextElement();
			try {
				InputStreamReader in = new InputStreamReader(url.openStream());
				char[] name = new char[512];
				in.read(name);
				in.close();
				String pluginMainClassName = new String(name).trim();
				logger.info("Found plugin: " + pluginMainClassName);
				Object instance;
				try {
					instance = classLoader.loadClass(pluginMainClassName).newInstance();
				} catch (ClassNotFoundException ex) {
					// this can happen if a plugin created for a custom build is being dropped inside
					// the plugins directory of pms. The plugin might implement an interface only
					// available in the custom build, but not in pms.
					logger.warn(String.format("The plugin '%s' couldn't be loaded because %s"
							, pluginMainClassName, ex.getMessage()));
					continue;
				}
				if (instance instanceof ExternalListener) {
					registerListener((ExternalListener) instance);
				}
			} catch (Exception e) {
				logger.error("Error loading plugin", e);
			}
		}
	}
}
