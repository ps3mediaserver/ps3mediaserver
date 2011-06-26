package net.pms.update;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperatingSystem {
	private static final Logger logger = LoggerFactory.getLogger(OperatingSystem.class);
	private static final String platformName = detectPlatform();

	private static String detectPlatform() {
		String fullPlatform = System.getProperty("os.name", "unknown");
		String platform = fullPlatform.split(" ")[0].toLowerCase();
		logger.debug("Platform detected as [" + platform + "]");
		return platform;
	}

	public String getPlatformName() {
		assert platformName != null;
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
