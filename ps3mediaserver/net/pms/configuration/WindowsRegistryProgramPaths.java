package net.pms.configuration;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.PMS;
import net.pms.io.WinUtils;

class WindowsRegistryProgramPaths implements ProgramPaths {
	private static final Logger logger = LoggerFactory.getLogger(WindowsRegistryProgramPaths.class);
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
	public String getMencoderMTPath() {
		return defaults.getMencoderMTPath();
	}

	@Override
	public String getMencoderAlternatePath() {
		return defaults.getMencoderAlternatePath();
	}

	@Override
	public String getMencoderAlternateMTPath() {
		return defaults.getMencoderAlternateMTPath();
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
		WinUtils registry = PMS.get().getRegistry();
		if (registry.getVlcp() != null) {
			String vlc = registry.getVlcp();
			String version = registry.getVlcv();
			if (new File(vlc).exists() && version != null) {
				logger.debug("Found VLC version " + version + " in Windows Registry: " + vlc); //$NON-NLS-1$ //$NON-NLS-2$
				return vlc;
			}
		}
		return defaults.getVlcPath();
	}

	@Override
	public String getDCRaw() {
		return defaults.getDCRaw();
	}
}
