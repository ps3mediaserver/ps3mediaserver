/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2012  Ph.Waeber
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
package net.pms.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import net.pms.PMS;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base configuration class which manages properties which can be saved and
 * loaded through the appropriate methods. the generic getValue method gives
 * easy access to typed properties
 * 
 * @author pw
 * 
 */
public class BaseConfiguration {
	private static final Logger log = LoggerFactory.getLogger(BaseConfiguration.class);
	protected Properties properties = new Properties();
	
	/**
	 * Gets the path to the folder containing the plugin configurations
	 * 
	 * @return the path to the directory containing the plugin configurations
	 */
	public String getGlobalConfigurationDirectory() {
		return PMS.getConfiguration().getProfileDirectory() + File.separator + "plugins" + File.separator + "global" + File.separator;
	}
	
	/**
	 * Saves the properties to the specified file path.<br>
	 * Sub-directories will be created automatically if needed
	 *
	 * @param propertiesFilePath the save file path
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void save(String propertiesFilePath) throws IOException {
		if (log.isDebugEnabled()) log.debug("Saving configuration to " + propertiesFilePath);

		// make sure the save directory exists
		File saveFile = new File(propertiesFilePath);
		File saveDir = new File(saveFile.getParent());
		if (!saveDir.isDirectory()) {
			saveDir.mkdirs();
		}

		FileOutputStream configStream = new FileOutputStream(propertiesFilePath);
		properties.store(configStream, "");
		log.debug("Saved configuration to " + propertiesFilePath);
	}

	/**
	 * Saves the properties to the specified file path.
	 *
	 * @param propertiesFilePath the file to load the properties from
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void load(String propertiesFilePath) throws IOException {
		if(new File(propertiesFilePath).exists()) {
			if (log.isDebugEnabled()) log.debug("Restoring configuration from " + propertiesFilePath);
			FileInputStream configStream = new FileInputStream(propertiesFilePath);
			properties.load(configStream);
			log.info("Loaded configuration from " + propertiesFilePath);
		} else {
			log.debug("No configuration file found at " + propertiesFilePath);
		}
	}

	/**
	 * Sets the value for the given key.
	 *
	 * @param key the key
	 * @param value the value
	 */
	protected void setValue(String key, Object value) {
		if (key != null && value != null) {
			properties.put(key, value.toString());
		}
	}

	/**
	 * Gets the value of type T for the given key. If no value can be found, the default value will be returned
	 *
	 * @param <T> the generic type. Supported types are String, Integer, Boolean, Enum
	 * @param key the key
	 * @param defaultValue the default value
	 * @return the value
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected <T> T getValue(String key, T defaultValue) {
		Object val = properties.get(key);
		if (val != null && defaultValue != null) {
			if (defaultValue instanceof Integer) {
				return (T) (Integer) Integer.parseInt(val.toString());
			} else if (defaultValue instanceof Boolean) {
				return (T) (Boolean) Boolean.parseBoolean(val.toString());
			} else if (defaultValue instanceof Enum) {
				return (T) Enum.valueOf((Class)defaultValue.getClass(), val.toString());
			} else if (defaultValue.getClass().isAssignableFrom(val.getClass())) {
				return (T) val;
			}
		}
		return defaultValue;
	}
}
