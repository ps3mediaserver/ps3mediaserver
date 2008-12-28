package net.pms;

import java.io.File;
import java.io.IOException;

import net.pms.util.TempFolder;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

class Configuration {
	private static final String CONFIGURATION_FILENAME = "PMS.conf";

	private static final String KEY_TEMP_FOLDER_PATH = "temp";

	private final PropertiesConfiguration configuration;
	private final TempFolder tempFolder;

	Configuration() throws ConfigurationException, IOException {
		configuration = new PropertiesConfiguration(new File(CONFIGURATION_FILENAME));
		tempFolder = new TempFolder(configuration.getString(KEY_TEMP_FOLDER_PATH));
	}

	public File getTempFolder() throws IOException {
		return tempFolder.getTempFolder();
	}

}
