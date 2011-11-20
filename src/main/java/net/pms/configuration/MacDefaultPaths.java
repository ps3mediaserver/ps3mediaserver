package net.pms.configuration;

import java.io.IOException;

import net.pms.util.PmsProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MacDefaultPaths implements ProgramPaths {
	private static final Logger logger = LoggerFactory.getLogger(MacDefaultPaths.class);
	
	/**
	 * General properties for the PMS project.
	 */
	private final PmsProperties projectProperties = new PmsProperties();

	@Override
	public String getEac3toPath() {
		return null;
	}

	@Override
	public String getFfmpegPath() {
		return getBinariesPath() + "osx/ffmpeg";
	}

	@Override
	public String getFlacPath() {
		return getBinariesPath() + "osx/flac";
	}

	@Override
	public String getMencoderPath() {
		return getBinariesPath() + "osx/mencoder";
	}

	@Override
	public String getMplayerPath() {
		return getBinariesPath() + "osx/mplayer";
	}

	@Override
	public String getTsmuxerPath() {
		return getBinariesPath() + "osx/tsMuxeR";
	}

	@Override
	public String getVlcPath() {
		return "/Applications/VLC.app/Contents/MacOS/VLC";
	}

	@Override
	public String getDCRaw() {
		return getBinariesPath() + "osx/dcraw";
	}
	
	@Override
	public String getIMConvertPath() {
		return getBinariesPath() + "osx/convert";
	}

	/**
	 * Constructor makes sure the project properties are read
	 */
	public MacDefaultPaths() {
		try {
			// Read project properties resource file.
			getProjectProperties().loadFromResourceFile("/resources/project.properties");
		} catch (IOException e) {
			logger.error("Could not load project.properties");
		}
	}
	
	/**
	 * Returns the project properties object.
	 *
	 * @return The properties object.
	 */
	private PmsProperties getProjectProperties() {
		return projectProperties;
	}

	/**
	 * Returns the path where binaries can be found. This path differs between
	 * the build phase and the test phase. The path will end with a slash unless
	 * it is empty.
	 *
	 * @return The path for binaries.
	 */
	private String getBinariesPath() {
		String path = getProjectProperties().get("project.binaries");
		
		if (path != null && !"".equals(path)) {
			if (path.endsWith("/")) {
				return path;
			} else {
				return path + "/";
			}
		} else {
			return "";
		}
	}
}
