package net.pms.plugin.dlnatreefolder.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseConfiguration {
	private static final Logger log = LoggerFactory.getLogger(BaseConfiguration.class);
	private Properties properties = new Properties();

	public void save(String propertiesFilePath) throws IOException {
			if (log.isDebugEnabled()) log.debug("Saving configuration to " + propertiesFilePath);
			
			//make sure the save directory exists
			File saveFile = new File(propertiesFilePath);
			File saveDir = new File(saveFile.getParent());
			if(!saveDir.isDirectory()) {
				saveDir.mkdirs();
			}
			
			FileOutputStream configStream = new FileOutputStream(propertiesFilePath);
			properties.store(configStream, "");
	}

	public void load(String propertiesFilePath) throws IOException {
		if (log.isDebugEnabled()) log.debug("Restoring configuration from " + propertiesFilePath);
		FileInputStream configStream = new FileInputStream(propertiesFilePath);
		properties.load(configStream);
	}

	protected void setValue(String key, Object value) {
		if(key != null && value != null) {
			properties.put(key, value.toString());
		}
	}

	@SuppressWarnings("unchecked")
	protected <T> T getValue(String key, T defaultValue) {
		Object val = properties.get(key);
		if(val != null && defaultValue != null) {
			if(defaultValue instanceof Integer) {
				return (T)(Integer)Integer.parseInt(val.toString());
			} else if(defaultValue instanceof Boolean) {
				return (T)(Boolean)Boolean.parseBoolean(val.toString());				
			} else if (defaultValue.getClass().isAssignableFrom(val.getClass())) {
				return (T) val;
			}
		}
		return defaultValue;
	}
}
