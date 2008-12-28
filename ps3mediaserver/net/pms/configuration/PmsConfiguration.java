package net.pms.configuration;

import java.io.File;
import java.io.IOException;


import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;

public class PmsConfiguration {
	private static final String CONFIGURATION_FILENAME = "PMS.conf";

	private static final String KEY_TEMP_FOLDER_PATH = "temp";
	private static final String KEY_VLC_PATH = "vlc_path";
	private static final String KEY_EAC3TO_PATH = "eac3to_path";
	private static final String KEY_MENCODER_PATH = "mencoder_path";
	private static final String KEY_FFMPEG_PATH = "ffmpeg_path";
	private static final String KEY_MPLAYER_PATH = "mplayer_path";
	private static final String KEY_TSMUXER_PATH = "tsmuxer_path";
	private static final String KEY_FLAC_PATH = "flac_path";

	private final PropertiesConfiguration configuration;
	private final TempFolder tempFolder;
	private final WindowsRegistry windowsRegistry;
	private final DefaultPaths defaultPaths;
	
	private boolean disableVlc = false;
	private boolean disableMencoder = false;
	private boolean disableFfmpeg = false;
	private boolean disableMplayer = false;

	public PmsConfiguration() throws ConfigurationException, IOException {
		configuration = new PropertiesConfiguration(new File(CONFIGURATION_FILENAME));
		tempFolder = new TempFolder(configuration.getString(KEY_TEMP_FOLDER_PATH));
		windowsRegistry = new WindowsRegistry();
		defaultPaths = new DefaultPathsFactory().get();
	}

	public File getTempFolder() throws IOException {
		return tempFolder.getTempFolder();
	}

	public String getVlcPath() {
		String path;
		if (disableVlc) {
			return null;
		} else if ((path = windowsRegistry.getVlcPath()) != null) {
			return path;
		} else if ((path = stringFromConfigFile(KEY_VLC_PATH)) != null) {
			return path;
		} else {
			return defaultPaths.getVlcPath();
		}
	}

	private String stringFromConfigFile(String key) {
		return stringFromConfigFile(key, null);
	}
	
	private String stringFromConfigFile(String key, String def) {
		String value = configuration.getString(key);
		return StringUtils.isNotBlank(value) ? value : def;
	}

	public void disableVlc() {
		disableVlc = true;
	}

	public String getEac3toPath() {
		return stringFromConfigFile(KEY_EAC3TO_PATH, defaultPaths.getEac3toPath());
	}

	public String getMencoderPath() {
		if (!disableMencoder) {
			return stringFromConfigFile(KEY_MENCODER_PATH, defaultPaths.getMencoderPath());
		} else {
			return null;
		}
	}

	public void disableMEncoder() {
		disableMencoder = true;
	}

	public String getFfmpegPath() {
		if (!disableFfmpeg) {
			return stringFromConfigFile(KEY_FFMPEG_PATH, defaultPaths.getFfmpegPath());
		} else {
			return null;
		}
	}

	public void disableFfmpeg() {
		disableFfmpeg = true;
	}

	public String getMplayerPath() {
		if (!disableMplayer) {
			return stringFromConfigFile(KEY_MPLAYER_PATH, defaultPaths.getMplayerPath());
		} else {
			return null;
		}
	}

	public void disableMplayer() {
		disableMplayer = true;
	}

	public String getTsmuxerPath() {
		return stringFromConfigFile(KEY_TSMUXER_PATH, defaultPaths.getTsmuxerPath());
	}

	public String getFlacPath() {
		return stringFromConfigFile(KEY_FLAC_PATH, defaultPaths.getFlacPath());
	}
	
}
