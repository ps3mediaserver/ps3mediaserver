/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2008  A.Brochard
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2
 * of the License only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package net.pms.plugins;

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
import net.pms.notifications.NotificationCenter;
import net.pms.notifications.types.PluginEvent;
import net.pms.notifications.types.PluginEvent.Event;
import net.pms.plugins.wrappers.AdditionalFolderAtRootWrapper;
import net.pms.plugins.wrappers.AdditionalFoldersAtRootWrapper;
import net.pms.plugins.wrappers.BaseWrapper;
import net.pms.plugins.wrappers.ExternalListenerWrapper;
import net.pms.plugins.wrappers.FinalizeTranscoderArgsListenerWrapper;
import net.pms.plugins.wrappers.StartStopListenerWrapper;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class takes care of registering plugins. Plugin jars are loaded,
 * instantiated and stored for later retrieval.
 */
public class PluginsFactory {
	/**
	 * For logging messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(PluginsFactory.class);

	/**
	 * The name of the file containing the package and class name
	 * for which pms will instantiate the plugin. This file has to 
	 * be contained at the root of the jar
	 */
	private static final String DESCRIPTOR_FILE_NAME = "plugin";

	/**
	 * The class loader will be lazy initialized the first time it's 
	 * being used
	 */
	private static ClassLoader classLoader;
	
	/**
	 * All registered plugins
	 */
	private static List<PluginBase> plugins = new ArrayList<PluginBase>();

	/**
	 * Gets all registered plugins
	 *
	 * @return The instances.
	 */
	public static List<PluginBase> getPlugins() {
		return plugins;
	}

	/**
	 * Gets all registered dlna tree folder plugins.
	 *
	 * @return the dlna tree folder plugins
	 */
	public static List<DlnaTreeFolderPlugin> getDlnaTreeFolderPlugins() {
		return getPlugins(DlnaTreeFolderPlugin.class);
	}

	/**
	 * Gets all registered file detail plugins.
	 *
	 * @return the file detail plugins
	 */
	public static List<FileDetailPlugin> getFileDetailPlugins() {
		return getPlugins(FileDetailPlugin.class);
	}

	/**
	 * Gets all registered file import plugins.
	 *
	 * @return the file import plugins
	 */
	public static List<FileImportPlugin> getFileImportPlugins() {
		return getPlugins(FileImportPlugin.class);
	}
	
	/**
	 * Gets all registered finalize transcoder args listeners.
	 *
	 * @return the finalize transcoder args listeners
	 */
	public static List<FinalizeTranscoderArgsListener> getFinalizeTranscoderArgsListeners() {
		return getPlugins(FinalizeTranscoderArgsListener.class);
	}

	/**
	 * Gets all registered start stop listeners.
	 *
	 * @return the start stop listeners
	 */
	public static List<StartStopListener> getStartStopListeners() {
		return getPlugins(StartStopListener.class);
	}

	/**
	 * Gets all registered additional folder at root.
	 *
	 * @return the additional folder at root list
	 */
	@SuppressWarnings("deprecation")
	public static List<net.pms.external.AdditionalFolderAtRoot> getAdditionalFolderAtRootList() {
		List<net.pms.external.AdditionalFolderAtRoot> res = new ArrayList<net.pms.external.AdditionalFolderAtRoot>();
		for(AdditionalFolderAtRootWrapper wp : getPlugins(AdditionalFolderAtRootWrapper.class)) {
			res.add(wp.getFolder());
		}
		return res;
	}

	/**
	 * Gets all registered additional folders at root.
	 *
	 * @return the additional folders at root list
	 */
	@SuppressWarnings("deprecation")
	public static List<net.pms.external.AdditionalFoldersAtRoot> getAdditionalFoldersAtRootList() {
		List<net.pms.external.AdditionalFoldersAtRoot> res = new ArrayList<net.pms.external.AdditionalFoldersAtRoot>();
		for(AdditionalFoldersAtRootWrapper wp : getPlugins(AdditionalFoldersAtRootWrapper.class)) {
			res.add(wp.getFolders());
		}
		return res;
	}
	/**
	 * Gets a dlna tree folder plugin by name.
	 *
	 * @param className the class name
	 * @return the dlna tree folder plugin resolved by name. Null if it couldn't be resolved
	 */
	public static DlnaTreeFolderPlugin getDlnaTreeFolderPluginByName(String className) {
		DlnaTreeFolderPlugin plugin =  getPluginByName(DlnaTreeFolderPlugin.class, className);
		return plugin;
	}

	/**
	 * Gets the file detail plugin by name.
	 *
	 * @param className the class name
	 * @return the file detail plugin resolved by name. Null if it couldn't be resolved
	 */
	public static FileDetailPlugin getFileDetailPluginByName(String className) {
		FileDetailPlugin plugin =  getPluginByName(FileDetailPlugin.class, className);
		return plugin;
	}

	/**
	 * Gets the file import plugin by name.
	 *
	 * @param className the class name
	 * @return the file import plugin resolved by name. Null if it couldn't be resolved
	 */
	public static FileImportPlugin getFileImportPluginByName(String className) {
		FileImportPlugin plugin =  getPluginByName(FileImportPlugin.class, className);
		return plugin;
	}

	/**
	 * Stores the instance of an external listener in a list for later
	 * retrieval. The same instance will only be stored once.
	 *
	 * @param listener The instance to store.
	 */
	public static void registerPlugin(PluginBase plugin) {
		boolean add = true;
		PluginBase pluginToRemove = null;
		// only allow a single instance for a plugin implementation to be added
		for (PluginBase p : plugins) {
			try {
				if (p.getClass().equals(plugin.getClass())) {
					if (compareVersion(p.getVersion(), plugin.getVersion()) > 0) {
						add = false;
						break;
					} else {
						add = true;
						pluginToRemove = p;
						break;
					}
				}
			} catch (Throwable t) {
				// catch throwable for every external call to avoid plugins crashing pms
				LOGGER.error("Failed to call getVersion on a plugin", t);
				add = false;
			}
		}
		
		if(pluginToRemove != null) {
			plugins.remove(pluginToRemove);
		}

		if (add) {
			plugins.add(plugin);
		}
	}

	/**
	 * This method scans the plugins directory for ".jar" files and processes
	 * each file that is found. First, a resource named "plugin" is extracted
	 * from the jar file. Its contents determine the name of the main plugin
	 * class. This main plugin class is then loaded and an instance is created
	 * and registered for later use.
	 */
	@SuppressWarnings("deprecation")
	public static void lookup() {
		File pluginDirectory = new File(PMS.getConfiguration().getPluginDirectory());

		if (!pluginDirectory.exists()) {
			LOGGER.warn("Plugin directory doesn't exist: " + pluginDirectory);
			return;
		}

		if (!pluginDirectory.isDirectory()) {
			LOGGER.warn("Plugin directory is not a directory: " + pluginDirectory);
			return;
		}

		LOGGER.info("Searching for plugins in " + pluginDirectory.getAbsolutePath());
		
		// Filter all .jar files from the plugin directory
		File[] jarFiles = pluginDirectory.listFiles(
			new FileFilter() {
				public boolean accept(File file) {
					return file.isFile() && file.getName().toLowerCase().endsWith(".jar");
				}
			}
		);

		int nJars = jarFiles.length;

		if (nJars == 0) {
			LOGGER.info("No plugins found");
			return;
		}

		// To load a .jar file the filename needs to converted to a file URL
		List<URL> jarURLList = new ArrayList<URL>();

		for (int i = 0; i < nJars; ++i) {
			try {
				jarURLList.add(jarFiles[i].toURI().toURL());
			} catch (MalformedURLException e) {
				LOGGER.error("Can't convert file path " + jarFiles[i] + " to URL", e);
			}
		}

		URL[] jarURLs = new URL[jarURLList.size()];
		jarURLList.toArray(jarURLs);

		if(classLoader == null) {
			// specify the parent classloader being PMS to include the required plugin interface definitions
			// for the classloader. If this isn't being set, a ClassNotFoundException might be raised
			// because the interface implemented by the plugin can't be resolved.
			classLoader = new URLClassLoader(jarURLs, PMS.class.getClassLoader());
		}
		Enumeration<URL> resources;

		try {
			// Each plugin .jar file has to contain a resource named "plugin"
			// which should contain the name of the main plugin class.
			resources = classLoader.getResources(DESCRIPTOR_FILE_NAME);
		} catch (IOException e) {
			LOGGER.error("Can't load plugin resources", e);
			return;
		}

		while (resources.hasMoreElements()) {
			URL url = resources.nextElement();

			try {
				// Determine the plugin main class name from the contents of
				// the plugin file.
				InputStreamReader in = new InputStreamReader(url.openStream());
				char[] name = new char[512];
				in.read(name);
				in.close();
				String pluginMainClassName = new String(name).trim();
				LOGGER.info("Found plugin: " + pluginMainClassName);
				Object instance;
				try {
					instance = classLoader.loadClass(pluginMainClassName).newInstance();
				} catch (Throwable t) {
					// this can happen if a plugin created for a custom build is being dropped inside
					// the plugins directory of pms. The plugin might implement an interface only
					// available in the custom build, but not in pms.
					LOGGER.warn(String.format("The plugin '%s' couldn't be loaded", pluginMainClassName), t);
					continue;
				}
				if (instance instanceof PluginBase) {
					registerPlugin((PluginBase) instance);
				} else if (instance instanceof net.pms.external.ExternalListener) {
					registerOldPlugin((net.pms.external.ExternalListener) instance);
				}
			} catch (Exception e) {
				LOGGER.error("Error loading plugin", e);
			} catch (NoClassDefFoundError e) {
				LOGGER.error("Error loading plugin", e);
			}
		}
	}

	/**
	 * Initialize all registered plugins except for FinalizeTranscoderArgsListener.
	 */
	public static void initializePlugins() {
		for (PluginBase p : plugins) {
			try {
				p.initialize();
			} catch (Throwable t) {
				// catch throwable for every external call to avoid plugins crashing pms
				LOGGER.error("Failed to initialize a plugin", t);
			}
		}
		NotificationCenter.getInstance(PluginEvent.class).post(new PluginEvent(Event.PluginsLoaded));
	}
	
	/**
	 * Shut down all registered plugins.
	 */
	public static void shutdownPlugins() {
		for(PluginBase p : plugins) {
			try {
				p.shutdown();
			} catch (Throwable t) {
				// catch throwable for every external call to avoid plugins crashing pms
				LOGGER.error("Failed to shut down a plugin", t);
			}
		}
	}
	
	/**
	 * Registers an old style plugin inside a wrapper, to make the
	 * transition to the new plugin system smooth
	 * @param listener
	 */
	@SuppressWarnings("deprecation")
	private static void registerOldPlugin(net.pms.external.ExternalListener listener) {
		BaseWrapper wp = null;
		if(listener instanceof net.pms.external.AdditionalFolderAtRoot) {
			wp = new AdditionalFolderAtRootWrapper((net.pms.external.AdditionalFolderAtRoot) listener);
		} else if(listener instanceof net.pms.external.AdditionalFolderAtRoot) {
			wp = new AdditionalFoldersAtRootWrapper((net.pms.external.AdditionalFoldersAtRoot) listener);
		} else if(listener instanceof net.pms.external.FinalizeTranscoderArgsListener) {
			wp = new FinalizeTranscoderArgsListenerWrapper((net.pms.external.FinalizeTranscoderArgsListener) listener);
		} else if(listener instanceof net.pms.external.StartStopListener) {
			wp = new StartStopListenerWrapper((net.pms.external.StartStopListener) listener);
		} else if(listener != null) {
			wp = new ExternalListenerWrapper(listener);
		}
		
		if(wp != null) {
			registerPlugin(wp);
		}
	}

	/**
	 * Compare two version strings and return the result. E.g.
	 * <code>compareVersion("1.6.1", "1.12-SNAPSHOT")</code> returns a number
	 * less than 0. 
	 *
	 * @param version1 First version string to compare.
	 * @param version2 Seconds version string to compare.
	 * @return A number less than 0, equal to 0 or greater than 0, depending on
	 * 		the comparison outcome.
	 */
	private static int compareVersion(String version1, String version2) {
		DefaultArtifactVersion v1 = new DefaultArtifactVersion(version1);
		DefaultArtifactVersion v2 = new DefaultArtifactVersion(version2);

		return v1.compareTo(v2);
	}

	/**
	 * Gets the list of registered plugins of the
	 * generic type T specified by the class parameter
	 * @param c specifies the type of the returned list
	 * @return a list containing all registered plugins for the specified type (0-n).
	 */
	@SuppressWarnings("unchecked")
	private static <T> List<T> getPlugins(Class<T> c) {
		List<T> res = new ArrayList<T>();
		for (PluginBase p : plugins) {
			if (c.isAssignableFrom(p.getClass())) {
				res.add((T) p);
			}
		}
		return res;
	}

	/**
	 * Returns a plugin instance for the given class name
	 * @param c specifies the type of the returned list
	 * @param className a string with the complete package + class name of the
	 * class to load
	 * @return a plugin instance of the specified type or null if none can be found
	 */
	@SuppressWarnings("unchecked")
	private static <T extends PluginBase> T getPluginByName(Class<T> c, String className) {
		T res = null;
		try {
			Class<?> clLauncher = classLoader.loadClass(className);
			Object instance = clLauncher.newInstance();
			if (c.isAssignableFrom(instance.getClass())) {
				res = (T) instance;
				res.initialize();
			}
		} catch (Throwable t) {
			LOGGER.error("Failed to load plugin by name for " + className, t);
		}
		return res;
	}
}