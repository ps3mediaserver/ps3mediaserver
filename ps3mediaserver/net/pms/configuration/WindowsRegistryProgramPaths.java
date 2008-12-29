package net.pms.configuration;

import java.io.File;

import net.pms.PMS;
import net.pms.io.WinUtils;

class WindowsRegistryProgramPaths implements ProgramPaths {

	private final ProgramPaths defaults;
	
	WindowsRegistryProgramPaths(ProgramPaths defaults) {
		this.defaults = defaults;
	}
	
	@Override
	public String getEac3toPath() {
		return defaults.getEac3toPath();
	}

	@Override
	public String getFfmpegPath() {
		return defaults.getFfmpegPath();
	}

	@Override
	public String getFlacPath() {
		return defaults.getFlacPath();
	}

	@Override
	public String getMencoderPath() {
		return defaults.getMencoderPath();
	}

	@Override
	public String getMplayerPath() {
		return defaults.getMplayerPath();
	}

	@Override
	public String getTsmuxerPath() {
		return defaults.getTsmuxerPath();
	}

	@Override
	public String getVlcPath() {
		WinUtils registry = new WinUtils();
		if (registry.getVlcp() != null) {
			String vlc = registry.getVlcp();
			String version = registry.getVlcv();
			if (new File(vlc).exists() && version != null) {
				PMS.info("Found VLC version " + version + " in Windows Registry: " + vlc); //$NON-NLS-1$ //$NON-NLS-2$
				return vlc;
			}
		}
		return defaults.getVlcPath();
	}

}
