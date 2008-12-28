package net.pms.configuration;

import com.sun.jna.Platform;

public class DefaultPathsFactory {
	public DefaultPaths get() {
		if (Platform.isWindows()) {
			return new WindowsDefaultPaths();
		} else if (Platform.isMac()) {
			return new MacDefaultPaths();
		} else {
			return new LinuxDefaultPaths();
		}
	}
}
