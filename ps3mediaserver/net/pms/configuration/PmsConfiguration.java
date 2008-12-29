package net.pms.configuration;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;

public class PmsConfiguration {

	private static final String KEY_TEMP_FOLDER_PATH = "temp";
	private static final String KEY_TSMUXER_FORCEFPS = "tsmuxer_forcefps";
	private static final String KEY_TSMUXER_PREREMUX_PCM = "tsmuxer_preremux_pcm";
	private static final String KEY_AUDIO_BITRATE = "audiobitrate";
	private static final String KEY_TSMUXER_PREREMIX_AC3 = "tsmuxer_preremix_ac3";
	private static final String KEY_SERVER_PORT = "port";
	private static final String KEY_SERVER_HOSTNAME = "hostname";
	private static final String KEY_PROXY_SERVER_PORT = "proxy";
	private static final String KEY_LANGUAGE = "language";
	private static final String KEY_MIN_MEMORY_BUFFER_SIZE = "minvideobuffer";
	private static final String KEY_MAX_MEMORY_BUFFER_SIZE = "maxvideobuffer";
	private static final String KEY_MENCODER_ASS_MARGIN = "mencoder_ass_margin";
	private static final String KEY_MENCODER_ASS_OUTLINE = "mencoder_ass_outline";
	private static final String KEY_MENCODER_ASS_SCALE = "mencoder_ass_scale";
	private static final String KEY_MENCODER_ASS_SHADOW = "mencoder_ass_shadow";
	private static final String KEY_MENCODER_NOASS_SCALE = "mencoder_noass_scale";
	private static final String KEY_MENCODER_NOASS_SUBPOS = "mencoder_noass_subpos";
	private static final String KEY_MENCODER_NOASS_BLUR = "mencoder_noass_blur";
	private static final String KEY_MENCODER_NOASS_OUTLINE = "mencoder_noass_outline";

	private static final int DEFAULT_SERVER_PORT = 5001;
	private static final int DEFAULT_PROXY_SERVER_PORT = -1;
	
	private static final int MAX_MAX_MEMORY_BUFFER_SIZE = 630;
	
	private static final String CONFIGURATION_FILENAME = "PMS.conf";

	private final Configuration configuration;
	private final TempFolder tempFolder;
	private final ProgramPathDisabler programPaths;

	public PmsConfiguration() throws ConfigurationException, IOException {
		configuration = new PropertiesConfiguration(new File(CONFIGURATION_FILENAME));
		tempFolder = new TempFolder(getString(KEY_TEMP_FOLDER_PATH, null));
		programPaths = createProgramPathsChain(configuration);
	}

	/**
	 * Check if we have disabled something first, then check the Windows
	 * registry, then the config file, then check for a platform-specific
	 * default.
	 */
	private static ProgramPathDisabler createProgramPathsChain(Configuration configuration) {
		return  new ProgramPathDisabler(
				new WindowsRegistryProgramPaths(
				new ConfigurationProgramPaths(configuration, 
				new PlatformSpecificDefaultPathsFactory().get())));
	}

	public File getTempFolder() throws IOException {
		return tempFolder.getTempFolder();
	}

	public String getVlcPath() {
		return programPaths.getVlcPath();
	}

	public void disableVlc() {
		programPaths.disableVlc();
	}

	public String getEac3toPath() {
		return programPaths.getEac3toPath();
	}

	public String getMencoderPath() {
		return programPaths.getMencoderPath();
	}

	public void disableMEncoder() {
		programPaths.disableMencoder();
	}

	public String getFfmpegPath() {
		return programPaths.getFfmpegPath();
	}

	public void disableFfmpeg() {
		programPaths.disableFfmpeg();
	}

	public String getMplayerPath() {
		return programPaths.getMplayerPath();
	}

	public void disableMplayer() {
		programPaths.disableMplayer();
	}

	public String getTsmuxerPath() {
		return programPaths.getTsmuxerPath();
	}

	public String getFlacPath() {
		return programPaths.getFlacPath();
	}

	public boolean isTsmuxerForceFps() {
		return configuration.getBoolean(KEY_TSMUXER_FORCEFPS, true);
	}

	public boolean isTsmuxerPreremuxAc3() {
		return configuration.getBoolean(KEY_TSMUXER_PREREMIX_AC3, false);
	}

	public boolean isTsmuxerPreremuxPcm() {
		return configuration.getBoolean(KEY_TSMUXER_PREREMUX_PCM, false);
	}

	public int getAudioBitrate() {
		return getInt(KEY_AUDIO_BITRATE, 384);
	}

	public void setTsmuxerPreremuxAc3(boolean value) {
		configuration.setProperty(KEY_TSMUXER_PREREMIX_AC3, value);
	}

	public void setTsmuxerPreremuxPcm(boolean value) {
		configuration.setProperty(KEY_TSMUXER_PREREMUX_PCM, value);
	}

	public void setTsmuxerForceFps(boolean value) {
		configuration.setProperty(KEY_TSMUXER_FORCEFPS, value);
	}

	public int getServerPort() {
		return getInt(KEY_SERVER_PORT, DEFAULT_SERVER_PORT);
	}

	public void setServerPort(int value) {
		configuration.setProperty(KEY_SERVER_PORT, value);
	}

	public String getServerHostname() {
		return getString(KEY_SERVER_HOSTNAME, null);
	}

	public void setHostname(String value) {
		configuration.setProperty(KEY_SERVER_HOSTNAME, value);
	}
	
	public int getProxyServerPort() {
		return getInt(KEY_PROXY_SERVER_PORT, DEFAULT_PROXY_SERVER_PORT);
	}


	public String getLanguage() {
		String def = Locale.getDefault().getLanguage();
		String value = getString(KEY_LANGUAGE, def);
		return StringUtils.isNotBlank(value) ? value.trim() : def;
	}
	
	private int getInt(String key, int def) {
		try {
			return configuration.getInt(key, def);
		} catch (ConversionException e) {
			return def;
		}
	}

	private String getString(String key, String def) {
		String value = configuration.getString(key, def);
		if (value != null) {
			value = value.trim();
		}
		return value;
	}

	public int getMinMemoryBufferSize() {
		return getInt(KEY_MIN_MEMORY_BUFFER_SIZE, 12);
	}

	public double getMaxMemoryBufferSize() {
		return getInt(KEY_MAX_MEMORY_BUFFER_SIZE, 400);
	}

	public void setMaxMemoryBufferSize(int value) {
		if (value > 630) {
			value = MAX_MAX_MEMORY_BUFFER_SIZE;
		}
		configuration.setProperty(KEY_MAX_MEMORY_BUFFER_SIZE, value);		
	}
	
	public String getMencoderAssScale() {
		return getString(KEY_MENCODER_ASS_SCALE, "1.0");
	}

	public String getMencoderAssMargin() {
		return getString(KEY_MENCODER_ASS_MARGIN, "10");
	}

	public String getMencoderAssOutline() {
		return getString(KEY_MENCODER_ASS_OUTLINE, "1");
	}

	public String getMencoderAssShadow() {
		return getString(KEY_MENCODER_ASS_SHADOW, "1");
	}

	public String getMencoderNoAssScale() {
		return getString(KEY_MENCODER_NOASS_SCALE, "3");
	}

	public String getMencoderNoAssSubPos() {
		return getString(KEY_MENCODER_NOASS_SUBPOS, "2");
	}

	public String getMencoderNoAssBlur() {
		return getString(KEY_MENCODER_NOASS_BLUR, "1");
	}

	public String getMencoderNoAssOutline() {
		return getString(KEY_MENCODER_ASS_OUTLINE, "3");
	}

	public void setMencoderNoAssOutline(String value) {
		configuration.setProperty(KEY_MENCODER_NOASS_OUTLINE, value);
	}

	public void setMencoderAssMargin(String value) {
		configuration.setProperty(KEY_MENCODER_ASS_MARGIN, value);
	}

	public void setMencoderAssOutline(String value) {
		configuration.setProperty(KEY_MENCODER_ASS_OUTLINE, value);
	}

	public void setMencoderAssShadow(String value) {
		configuration.setProperty(KEY_MENCODER_ASS_SHADOW, value);
	}

	public void setMencoderAssScale(String value) {
		configuration.setProperty(KEY_MENCODER_ASS_SCALE, value);
	}

	public void setMencoderNoAssScale(String value) {
		configuration.setProperty(KEY_MENCODER_NOASS_SCALE, value);
	}

	public void setMencoderNoAssBlur(String value) {
		configuration.setProperty(KEY_MENCODER_NOASS_BLUR, value);
	}

	public void setMencoderNoAssSubPos(String value) {
		configuration.setProperty(KEY_MENCODER_NOASS_SUBPOS, value);
	}

	public void setLanguage(String value) {
		configuration.setProperty(KEY_LANGUAGE, value);
		Locale.setDefault(new Locale(getLanguage()));
	}

}