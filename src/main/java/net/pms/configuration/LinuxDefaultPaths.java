package net.pms.configuration;

import java.io.IOException;

import net.pms.util.PmsProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class LinuxDefaultPaths implements ProgramPaths {
	private static final Logger logger = LoggerFactory.getLogger(MacDefaultPaths.class);
	
	/**
	 * General properties for the PMS project.
	 */
	private final PmsProperties projectProperties = new PmsProperties();

	@Override
	public String getEac3toPath() {
		return "eac3to";
	}

	@Override
	public String getFfmpegPath() {
		return "ffmpeg";
	}

	@Override
	public String getFlacPath() {
		return "flac";
	}

	@Override
	public String getMencoderPath() {
		return "mencoder";
	}

	@Override
	public String getMplayerPath() {
		return "mplayer";
	}

	@Override
	public String getTsmuxerPath() {
		return getBinariesPath() + "linux/tsMuxeR";
	}

	@Override
	public String getVlcPath() {
		return "vlc";
	}

	@Override
	public String getDCRaw() {
		return "dcraw";
	}
	
	@Override
	public String getIMConvertPath() {
		return "convert";
	}

	/**
	 * Constructor makes sure the project properties are read
	 */
	public LinuxDefaultPaths() {
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
