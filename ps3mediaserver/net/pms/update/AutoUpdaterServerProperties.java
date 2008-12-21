package net.pms.update;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Data provided by the server for us to update with.  Must be synchronized externally.
 */
public class AutoUpdaterServerProperties {
	
	private static final String ENCODING = "UTF-8";
	
	private static final String KEY_LATEST_VERSION = "LatestVersion";
	private static final String DEFAULT_LATEST_VERSION = "0";

	private static final String KEY_DOWNLOAD_URL = "DownloadUrl";
	private static final String DEFAULT_DOWNLOAD_URL = "";
	
	private final Properties properties = new Properties();
	private final OperatingSystem operatingSystem = new OperatingSystem();
	
	private static final Logger LOG = Logger.getLogger(AutoUpdaterServerProperties.class.getName());
	
		
	public void loadFrom(byte[] data) throws IOException {
		try {
			String utf = new String(data, ENCODING);
			StringReader reader = new StringReader(utf);
			properties.clear();
			properties.load(reader);
			LOG.fine("Keys from server: " + properties.keySet());
			reader.close();
		} catch (UnsupportedEncodingException e) {
			throw new IOException("Could not decode " + ENCODING);
		}
	}
	
	public boolean isStateValid() {
		return getDownloadUrl().length() > 0 && getLatestVersion().isGreaterThan(new Version("0"));
	}
	
	public Version getLatestVersion() {
		return new Version(getStringWithDefault(KEY_LATEST_VERSION, DEFAULT_LATEST_VERSION));
	}
	
	public String getDownloadUrl() {
		return getStringWithDefault(KEY_DOWNLOAD_URL, DEFAULT_DOWNLOAD_URL);
	}

	private String maybeGetPlatformSpecificKey(String key) {
		String platformSpecificKey = key + "." + operatingSystem.getPlatformName();
		if (properties.containsKey(platformSpecificKey)) {
			LOG.fine("Using platform specific key [" + platformSpecificKey + "]");
			return platformSpecificKey;
		} else {
			LOG.fine("No platform override.  Trying key [" + key + "]");
			return key;
		}
	}
	
	private String getStringWithDefault(String key, String defaultValue) {
		key = maybeGetPlatformSpecificKey(key);
		if (properties.containsKey(key)) {
			return trimAndRemoveQuotes("" + properties.get(key));
		} else {
			return defaultValue;
		}
	}
	
	private static String trimAndRemoveQuotes(String in) {
		in = in.trim();
		if (in.startsWith("\"")) {
			in = in.substring(1);
		}
		if (in.endsWith("\"")) {
			in = in.substring(0, in.length() - 1);
		}
		return in;
	}
}
