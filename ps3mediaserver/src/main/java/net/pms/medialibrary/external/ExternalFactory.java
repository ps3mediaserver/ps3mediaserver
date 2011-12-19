package net.pms.medialibrary.external;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.PMS;

public class ExternalFactory {
	private static final Logger log = LoggerFactory.getLogger(ExternalFactory.class);
	private static List<DlnaTreeFolderPlugin> specialFolders;
	private static List<FileDetailPlugin> fileEntries;
	private static List<FileImportPlugin> movieInfoPlugins;
	private static ClassLoader classLoader;
	private static final String DESCRIPTOR_FILE_NAME = "plugin";

   	static {
		specialFolders = new ArrayList<DlnaTreeFolderPlugin>();
		fileEntries = new ArrayList<FileDetailPlugin>();
		movieInfoPlugins = new ArrayList<FileImportPlugin>();
	}

	public static List<DlnaTreeFolderPlugin> getSpecialFolders() {
		return specialFolders;
	}

	public static void registerRegisterDlnaTreeFolder(DlnaTreeFolderPlugin folder) {
		boolean add = true;
		//only allow a single instance for a plugin implementation to be added
		for(DlnaTreeFolderPlugin l : specialFolders){
			try {
				if(l.getClass().equals(folder.getClass()) && l.getVersion() >= folder.getVersion()){
					add = false;
					break;
				}
			} catch(Throwable t) {
				//catch throwable for every external call to avoid plugins crashing pms
				log.error("Failed to call getVersion on a plugin", t);
				add = false;
			}
		}
		if (add) {
			specialFolders.add(folder);
		}
	}

	public static List<FileImportPlugin> getFileImportPlugins() {
		return movieInfoPlugins;
	}

	public static void registerFileImportPlugin(FileImportPlugin entry) {
		boolean add = true;
		//only allow a single instance for a plugin implementation to be added
		for(FileImportPlugin l : movieInfoPlugins){
			try {
				if(l.getClass().equals(entry.getClass()) && l.getVersion() >= entry.getVersion()){
					add = false;
					break;
				}
			} catch(Throwable t) {
				//catch throwable for every external call to avoid plugins crashing pms
				log.error("Failed to call getVersion on a plugin", t);
				add = false;
			}
		}
		if (add) {
			movieInfoPlugins.add(entry);
		}
	}

	public static List<FileDetailPlugin> getFileEntries() {
		return fileEntries;
	}

	public static void registerFileDetailPlugin(FileDetailPlugin entry) {
		boolean add = true;
		//only allow a single instance for a plugin implementation to be added
		for(FileDetailPlugin l : fileEntries){
			try {
				if(l.getClass().equals(entry.getClass()) && l.getVersion() >= entry.getVersion()){
					add = false;
					break;
				}
			} catch(Throwable t) {
				//catch throwable for every external call to avoid plugins crashing pms
				log.error("Failed to call getVersion on a plugin", t);
				add = false;
			}
		}
		if (add) {
			fileEntries.add(entry);
		}
	}

	public static void lookup() {
		File pluginDirectory = new File(PMS.getConfiguration().getPluginDirectory());
		log.info("Searching for plugins in " + pluginDirectory.getAbsolutePath());

		if (!pluginDirectory.exists()) {
			log.warn("Plugin directory doesn't exist: " + pluginDirectory);
			return;
		}

		if (!pluginDirectory.isDirectory()) {
			log.warn("Plugin directory is not a directory: " + pluginDirectory);
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
			log.info("No plugins found");
			return;
		}

		List<URL> jarURLList = new ArrayList<URL>();

		for (int i = 0; i < nJars; ++i) {
			try {
				jarURLList.add(jarFiles[i].toURI().toURL());
			} catch (MalformedURLException e) {
				log.error("Can't convert file path " + jarFiles[i] + " to URL", e);
			}
		}

		URL[] jarURLs = new URL[jarURLList.size()];
		jarURLList.toArray(jarURLs);

		classLoader = new URLClassLoader(jarURLs, PMS.class.getClassLoader());
		Enumeration<URL> resources;

		try {
			resources = classLoader.getResources(DESCRIPTOR_FILE_NAME);
		} catch (IOException e) {
			log.error("Can't load plugin resources", e);
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
				log.info("Found plugin: " + pluginMainClassName);
				Object instance;
				try {
					instance = classLoader.loadClass(pluginMainClassName).newInstance();
				} catch (ClassNotFoundException ex) {
					// this can happen if a plugin created for a custom build is being dropped inside
					// the plugins directory of pms. The plugin might implement an interface only
					// available in the custom build, but not in pms.
					log.warn(String.format("The plugin '%s' couldn't be loaded because %s"
							, pluginMainClassName, ex.getMessage()));
					continue;
				}
				
				if (instance instanceof DlnaTreeFolderPlugin) {
					if(((DlnaTreeFolderPlugin)instance).isAvailable()){
	    				registerRegisterDlnaTreeFolder((DlnaTreeFolderPlugin) instance);
	    				if (log.isInfoEnabled()) log.info("Registered SpecialFolder " + pluginMainClassName);
					} else {
	    				if (log.isInfoEnabled()) log.info(String.format("Don't register DlnaTreeFolderPlugin %s, because it isn't available", pluginMainClassName));					
					}
				}
				
				if (instance instanceof FileDetailPlugin) {
	    			registerFileDetailPlugin((FileDetailPlugin) instance);
	    			if (log.isInfoEnabled()) log.info("Registered FileEntry plugin type " + pluginMainClassName);
				}
				
				if (instance instanceof FileImportPlugin) {
	    			registerFileImportPlugin((FileImportPlugin) instance);
	    			if (log.isInfoEnabled()) log.info("Registered FileImportPlugin plugin type " + pluginMainClassName);
				}
			} catch (Exception e) {
				log.error("Error loading plugin", e);
			}
		}
	}

	public static DlnaTreeFolderPlugin getSpecialFolderByName(String className) {
		DlnaTreeFolderPlugin res = null;
		try {
			Class<?> clLauncher = classLoader.loadClass(className);
			Object instance = clLauncher.newInstance();
			if (instance instanceof DlnaTreeFolderPlugin) {
				res = (DlnaTreeFolderPlugin) instance;
			}
		} catch (Exception ex) {
			log.error("Failed to load SpecialFolder named " + className, ex);
		}
		return res;
	}

	public static FileDetailPlugin getFileEntryPluginByName(String className) {
		FileDetailPlugin res = null;
		try {
			Class<?> clLauncher = classLoader.loadClass(className);
			Object instance = clLauncher.newInstance();
			if (instance instanceof FileDetailPlugin) {
				res = (FileDetailPlugin) instance;
			}
		} catch (Exception ex) {
			log.error("Failed to load FileEntryPlugin named " + className, ex);
		}
		return res;
	}

	public static FileImportPlugin getMovieInfoPluginByName(String className) {
		FileImportPlugin res = null;
		try {
			Class<?> clLauncher = classLoader.loadClass(className);
			Object instance = clLauncher.newInstance();
			if (instance instanceof FileImportPlugin) {
				res = (FileImportPlugin) instance;
			}
		} catch (Exception ex) {
			log.error("Failed to load MovieInfoPlugin named " + className, ex);
		}
		return res;
	}
}
