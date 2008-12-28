package net.pms.configuration;

import com.sun.jna.Platform;

class DefaultPathsFactory {
	public ProgramPaths get() {
		if (Platform.isWindows()) {
			return new WindowsDefaultPaths();
		} else if (Platform.isMac()) {
			return new MacDefaultPaths();
		} else {
			return new LinuxDefaultPaths();
		}
	}
}
