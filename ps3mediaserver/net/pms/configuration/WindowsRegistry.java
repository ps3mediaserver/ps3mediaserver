package net.pms.configuration;

import java.io.File;

import net.pms.PMS;
import net.pms.io.WinUtils;

class WindowsRegistry {

	String getVlcPath() {
		WinUtils registry = new WinUtils();
		if (registry.getVlcp() != null) {
			String vlc = registry.getVlcp();
			String version = registry.getVlcv();
			if (new File(vlc).exists() && version != null) {
				PMS.info("Found VLC version " + version + " in Windows Registry: " + vlc); //$NON-NLS-1$ //$NON-NLS-2$
				return vlc;
			}
		}
		return null;
	}

}
