package net.pms.update;

import java.util.logging.Logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

public class OperatingSystem {
	
	private static final Logger LOG = Logger.getLogger(OperatingSystem.class.getName());
	
	private static final String platformName = detectPlatform();
	
	private static String detectPlatform() {
		String fullPlatform = System.getProperty("os.name", "unknown");
		String platform = fullPlatform.split(" ")[0].toLowerCase();
		LOG.fine("Platform detected as [" + platform + "]");
		return platform;
	}
	
	public String getPlatformName() {
		assertThat(platformName, notNullValue());
		return platformName; 
	}
	
	@Override
	public String toString() {
		return getPlatformName();
	}

	public boolean isWindows() {
		return getPlatformName().equals("windows");
	}
}
