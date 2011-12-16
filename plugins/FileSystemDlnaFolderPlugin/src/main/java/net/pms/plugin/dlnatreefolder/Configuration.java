package net.pms.plugin.dlnatreefolder;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Configuration {
	private static final Logger log = LoggerFactory.getLogger(Configuration.class);
	private Properties          properties;

	Configuration() {
		properties = new Properties();
	}

	void SaveConfiguration(String configFilePath) throws IOException {
			if (log.isDebugEnabled()) log.debug("Saving configuration to " + configFilePath);
			FileOutputStream configStream = new FileOutputStream(configFilePath);
			properties.store(configStream, "");
	}

	void LoadConfiguration(String configFilePath) throws IOException {
		if (log.isDebugEnabled()) log.debug("Restoring configuration from " + configFilePath);
		FileInputStream configStream = new FileInputStream(configFilePath);
		properties.load(configStream);
	}

	public void setFolderPaths(List<String> folderPaths) {
		int i = 0;
		properties.clear();
		for (String path : folderPaths) {
			properties.put("p" + i++, path);
		}
	}

	public List<String> getFolderPaths() {
		int i = 0;
		Object prop = null;
		List<String> folderPaths = new ArrayList<String>();
		while (true) {
			prop = properties.get("p" + i++);
			if (prop == null) {
				break;
			}
			folderPaths.add((String) prop);
		}
		return folderPaths;
	}
}
