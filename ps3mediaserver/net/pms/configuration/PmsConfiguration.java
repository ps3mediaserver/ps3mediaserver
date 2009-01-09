package net.pms.configuration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import net.pms.PMS;
import net.pms.io.WinUtils;

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
	private static final String KEY_THUMBNAIL_SEEK_POS = "thumbnail_seek_pos";
	private static final String KEY_MENCODER_FONT = "mencoder_font"; 
	private static final String KEY_MENCODER_SUB_FRIBIDI = "mencoder_subfribidi";
	private static final String KEY_MENCODER_AUDIO_LANGS = "mencoder_audiolangs";
	private static final String KEY_MENCODER_SUB_LANGS = "mencoder_sublangs";
	private static final String KEY_MENCODER_AUDIO_SUB_LANGS = "mencoder_audiosublangs";
	private static final String KEY_MENCODER_SUB_CP = "mencoder_subcp";
	private static final String KEY_MENCODER_ASS = "mencoder_ass";
	private static final String KEY_MENCODER_DISABLE_SUBS = "mencoder_disablesubs";
	private static final String KEY_MENCODER_USE_PCM = "mencoder_usepcm";
	private static final String KEY_MENCODER_FONT_CONFIG = "mencoder_fontconfig";
	private static final String KEY_MENCODER_FORCE_FPS = "mencoder_forcefps";
	private static final String KEY_MENCODER_DECODE = "mencoder_decode";	
	private static final String KEY_MENCODER_YADIF = "mencoder_yadif";
	private static final String KEY_MENCODER_SCALER = "mencoder_scaler";
	private static final String KEY_MENCODER_SCALEX = "mencoder_scalex";
	private static final String KEY_MENCODER_SCALEY = "mencoder_scaley";
	private static final String KEY_OPEN_ARCHIVES = "enable_archive_browsing";
	private static final String KEY_AUDIO_CHANNEL_COUNT = "audiochannels";
	private static final String KEY_MAX_BITRATE = "maximumbitrate";
	private static final String KEY_THUMBNAILS_ENABLED = "thumbnails";
	private static final String KEY_NUMBER_OF_CPU_CORES = "nbcores";
	private static final String KEY_TURBO_MODE_ENABLED = "turbomode";
	private static final String KEY_MINIMIZED = "minimized";
	private static final String KEY_USE_SUBTITLES = "autoloadsrt";
	private static final String KEY_HIDE_VIDEO_SETTINGS = "hidevideosettings";
	private static final String KEY_USE_CACHE = "usecache";
	private static final String KEY_AVISYNTH_CONVERT_FPS = "avisynth_convertfps";
	private static final String KEY_AVISYNTH_SCRIPT = "avisynth_script";
	private static final String KEY_MAX_AUDIO_BUFFER = "maxaudiobuffer";
	private static final String KEY_MIN_STREAM_BUFFER = "minwebbuffer";
	private static final String KEY_BUFFER_TYPE = "buffertype";
	private static final String KEY_FFMPEG_SETTINGS = "ffmpeg";
	private static final String KEY_MENCODER_NO_OUT_OF_SYNC  = "mencoder_nooutofsync";
	private static final String KEY_TRANSCODE_BLOCKS_MULTIPLE_CONNECTIONS = "transcode_block_multiple_connections";
	private static final String KEY_CHARSET_ENCODING = "charsetencoding";
	private static final String KEY_MENCODER_INTELLIGENT_SYNC = "mencoder_intelligent_sync";
	private static final String KEY_FFMPEG_ALTERNATIVE_PATH = "alternativeffmpegpath";
	private static final String KEY_SKIP_LOOP_FILTER_ENABLED = "skiploopfilter";
	private static final String KEY_MENCODER_MAIN_SETTINGS = "mencoder_encode";
	private static final String KEY_LOGGING_LEVEL = "level";
	private static final String KEY_ENGINES = "engines";
	private static final String KEY_CODEC_SPEC_SCRIPT = "codec_spec_script";
	
	private static final int DEFAULT_SERVER_PORT = 5001;
	private static final int DEFAULT_PROXY_SERVER_PORT = -1;
	private static final String UNLIMITED_BITRATE = "0";
	
	private static final String DEFAULT_AVI_SYNTH_SCRIPT = 
		  "#AviSynth script is now fully customisable !\n" 
		+ "#You must use the following variables (\"clip\" being the avisynth variable of the movie):\n"
		+ "#<movie>: insert the complete DirectShowSource instruction [ clip=DirectShowSource(movie, convertfps) ]\n" 
		+ "#<sub>: insert the complete TextSub/VobSub instruction if there's any detected srt/sub/idx/ass subtitle file\n" 
		+ "#<moviefilename>: variable of the movie filename, if you want to do all this by yourself\n" 
		+ "#Be careful, the custom script MUST return the clip object\n"
		+ "<movie>\n" 
		+ "<sub>\n" 
		+ "return clip";
	
	private static final String DEFAULT_CODEC_CONF_SCRIPT = 
		  "#Here you can put specific parameters for some codec combinations.\n" 
		+ "#It's mostly for A/V synchronization issues, but it can be used for anything else as well\n" 
		+ "#Consider it like very expert settings as this shouldn't be modified if you don't know exactly what you're doing\n" 
		+ "#\n" 
		+ "#Syntax is {java condition} : {mencoder options}  ; You can cumulate several options\n"
		+ "#Ttokens authorized: container vcodec acodec samplerate framerate (xx000/1001) width height\n" 
		+ "#Careful, any malformed line will be wiped out\n" 
		+ "#\n" 
		+ "#Special options:\n" 
		+ "# -noass:  definitely disable ASS/SSA subtitles as they can mess up A/V sync\n" 
		+ "# -nosync: definitely disable A/V sync alternative method for this condition (-mc usage will do the same)\n" 
		+ "#\n" 
		+ "#This list will improve with time: tweaks/feedbacks on various codecs/files are always welcome\n" 
		+ "\n" 
		+ "container.equals(\"iso\") : -nosync\n" 
		+ "container.equals(\"avi\") && vcodec.equals(\"mpeg4\") && acodec.equals(\"mp3\") : -mc 0.1\n" 
		+ "container.equals(\"flv\") : -mc 0.1\n" 
		+ "container.equals(\"m4v\") : -mc 0.1 -noass\n" 
		+ "container.equals(\"rm\")  : -mc 0.1\n" ;
	
	private static final String BUFFER_TYPE_FILE = "file";
	
	private static final int MAX_MAX_MEMORY_BUFFER_SIZE = 600;
	
	private static final String CONFIGURATION_FILENAME = "PMS.conf";
	private static final char LIST_SEPARATOR = ',';
	private static final String KEY_FOLDERS = "folders";

	private final PropertiesConfiguration configuration;
	private final TempFolder tempFolder;
	private final ProgramPathDisabler programPaths;

	public PmsConfiguration() throws ConfigurationException, IOException {
		configuration = new PropertiesConfiguration();
		configuration.setListDelimiter((char)0);
		if (new File("PMS.conf").exists())
			configuration.load(CONFIGURATION_FILENAME);
		tempFolder = new TempFolder(getString(KEY_TEMP_FOLDER_PATH, null));
		programPaths = createProgramPathsChain(configuration);
		Locale.setDefault(new Locale(getLanguage()));
		
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
		String value = getString(KEY_SERVER_HOSTNAME, "");
		if (StringUtils.isNotBlank(value)) {
			return value;
		} else {
			return null;
		}
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
	
	private boolean getBoolean(String key, boolean def) {
		try {
			return configuration.getBoolean(key, def);
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
		if (value > 600) {
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
		return getString(KEY_MENCODER_NOASS_OUTLINE, "1");
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

	public int getThumbnailSeekPos() {
		return getInt(KEY_THUMBNAIL_SEEK_POS, 1);
	}

	public void setThumbnailSeekPos(int value) {
		configuration.setProperty(KEY_THUMBNAIL_SEEK_POS, value);
	}

	public boolean isMencoderAss() {
		return getBoolean(KEY_MENCODER_ASS, true);
	}

	public boolean isMencoderDisableSubs() {
		return getBoolean(KEY_MENCODER_DISABLE_SUBS, false);
	}

	public boolean isMencoderUsePcm() {
		return getBoolean(KEY_MENCODER_USE_PCM, false);
	}

	public String getMencoderFont() {
		return getString(KEY_MENCODER_FONT, "");
	}

	public String getMencoderAudioLanguages() {
		return getString(KEY_MENCODER_AUDIO_LANGS, getDefaultLanguages());
	}

	private String getDefaultLanguages() {
		if ("fr".equals(getLanguage())) {
			return "fre,jpn,ger,eng";
		} else {
			return "eng,fre,jpn,ger";
		}
	}

	public String getMencoderSubLanguages() {
		return getString(KEY_MENCODER_SUB_LANGS, getDefaultLanguages());
	}

	public String getMencoderAudioSubLanguages() {
		return getString(KEY_MENCODER_AUDIO_SUB_LANGS, "");
	}

	public boolean isMencoderSubFribidi() {
		return getBoolean(KEY_MENCODER_SUB_FRIBIDI, false);
	}

	public String getMencoderSubCp() {
		return getString(KEY_MENCODER_SUB_CP, "cp1252");
	}

	public boolean isMencoderFontConfig() {
		return getBoolean(KEY_MENCODER_FONT_CONFIG, false);
	}

	public void setMencoderForceFps(boolean value) {
		configuration.setProperty(KEY_MENCODER_FORCE_FPS, value);
	}

	public boolean isMencoderForceFps() {
		return getBoolean(KEY_MENCODER_FORCE_FPS, false);
	}

	public void setMencoderAudioLanguages(String value) {
		configuration.setProperty(KEY_MENCODER_AUDIO_LANGS, value);
	}

	public void setMencoderSubLanguages(String value) {
		configuration.setProperty(KEY_MENCODER_SUB_LANGS, value);
	}

	public void setMencoderAudioSubLanguages(String value) {
		configuration.setProperty(KEY_MENCODER_AUDIO_SUB_LANGS, value);		
	}

	public String getMencoderDecode() {
		return getString(KEY_MENCODER_DECODE, "");
	}

	public void setMencoderDecode(String value) {
		configuration.setProperty(KEY_MENCODER_DECODE, value);
	}

	public void setMencoderSubCp(String value) {
		configuration.setProperty(KEY_MENCODER_SUB_CP, value);
	}

	public void setMencoderSubFribidi(boolean value) {
		configuration.setProperty(KEY_MENCODER_SUB_FRIBIDI, value);
	}

	public void setMencoderFont(String value) {
		configuration.setProperty(KEY_MENCODER_FONT, value);
	}

	public void setMencoderAss(boolean value) {
		configuration.setProperty(KEY_MENCODER_ASS, value);		
	}

	public void setMencoderFontConfig(boolean value) {
		configuration.setProperty(KEY_MENCODER_FONT_CONFIG, value);
	}

	public void setMencoderDisableSubs(boolean value) {
		configuration.setProperty(KEY_MENCODER_DISABLE_SUBS, value);
	}

	public void setMencoderUsePcm(boolean value) {
		configuration.setProperty(KEY_MENCODER_USE_PCM, value);
	}
	
	public boolean isArchiveBrowsing() {
		return getBoolean(KEY_OPEN_ARCHIVES, false);
	}
	
	public void setArchiveBrowsing(boolean value) {
		configuration.setProperty(KEY_OPEN_ARCHIVES, value);
	}
	
	
	public boolean isMencoderYadif() {
		return getBoolean(KEY_MENCODER_YADIF, false);
	}
	
	public void setMencoderYadif(boolean value) {
		configuration.setProperty(KEY_MENCODER_YADIF, value);
	}
	
	public boolean isMencoderScaler() {
		return getBoolean(KEY_MENCODER_SCALER, false);
	}
	
	public void setMencoderScaler(boolean value) {
		configuration.setProperty(KEY_MENCODER_SCALER, value);
	}
	
	public int getMencoderScaleX() {
		return getInt(KEY_MENCODER_SCALEX, 0);
	}
	
	public void setMencoderScaleX(int value) {
		configuration.setProperty(KEY_MENCODER_SCALEX, value);
	}
	
	public int getMencoderScaleY() {
		return getInt(KEY_MENCODER_SCALEY, 0);
	}

	public void setMencoderScaleY(int value) {
		configuration.setProperty(KEY_MENCODER_SCALEY, value);
	}
	
	public int getAudioChannelCount() {
		return getInt(KEY_AUDIO_CHANNEL_COUNT, 6);
	}
	
	public void setAudioChannelCount(int value) {
		configuration.setProperty(KEY_AUDIO_CHANNEL_COUNT, value);
	}

	public void setAudioBitrate(int value) {
		configuration.setProperty(KEY_AUDIO_BITRATE, value);
	}

	public String getMaximumBitrate() {
		return getString(KEY_MAX_BITRATE, UNLIMITED_BITRATE);
	}

	public void setMaximumBitrate(String value) {
		configuration.setProperty(KEY_MAX_BITRATE, value);
	}

	public boolean getThumbnailsEnabled() {
		return getBoolean(KEY_THUMBNAILS_ENABLED, true);
	}

	public void setThumbnailsEnabled(boolean value) {
		configuration.setProperty(KEY_THUMBNAILS_ENABLED, value);
	}

	public int getNumberOfCpuCores() {
		return getInt(KEY_NUMBER_OF_CPU_CORES, 1);
	}
	
	public void setNumberOfCpuCores(int value) {
		configuration.setProperty(KEY_NUMBER_OF_CPU_CORES, value);
	}

	public boolean isTurboModeEnabled() {
		return getBoolean(KEY_TURBO_MODE_ENABLED, false);
	}

	public void setTurboModeEnabled(boolean value) {
		configuration.setProperty(KEY_TURBO_MODE_ENABLED, value);
	}

	public boolean isMinimized() {
		return getBoolean(KEY_MINIMIZED, false);
	}

	public void setMinimized(boolean value) {
		configuration.setProperty(KEY_MINIMIZED, value);
	}

	public boolean getUseSubtitles() {
		return getBoolean(KEY_USE_SUBTITLES, true);
	}
	
	public void setUseSubtitles(boolean value) {
		configuration.setProperty(KEY_USE_SUBTITLES, value);
	}

	public boolean getHideVideoSettings() {
		return getBoolean(KEY_HIDE_VIDEO_SETTINGS, false);
	}

	public void setHideVideoSettings(boolean value) {
		configuration.setProperty(KEY_HIDE_VIDEO_SETTINGS, value);
	}

	public boolean getUseCache() {
		return getBoolean(KEY_USE_CACHE, false);
	}

	public void setUseCache(boolean value) {
		configuration.setProperty(KEY_USE_CACHE, value);
	}

	public void setAvisynthConvertFps(boolean value) {
		configuration.setProperty(KEY_AVISYNTH_CONVERT_FPS, value);
	}

	public boolean getAvisynthConvertFps() {
		return getBoolean(KEY_AVISYNTH_CONVERT_FPS, true);
	}

	public String getAvisynthScript() {
		return getString(KEY_AVISYNTH_SCRIPT, DEFAULT_AVI_SYNTH_SCRIPT);
	}

	public void setAvisynthScript(String value) {
		configuration.setProperty(KEY_AVISYNTH_SCRIPT, value);
	}
	
	public String getCodecSpecificConfig() {
		return getString(KEY_CODEC_SPEC_SCRIPT, DEFAULT_CODEC_CONF_SCRIPT);
	}

	public void setCodecSpecificConfig(String value) {
		configuration.setProperty(KEY_CODEC_SPEC_SCRIPT, value);
	}

	public int getMaxAudioBuffer() {
		return getInt(KEY_MAX_AUDIO_BUFFER, 100);
	}
	
	public int getMinStreamBuffer() {
		return getInt(KEY_MIN_STREAM_BUFFER, 1);
	}

	public boolean isFileBuffer() {
		String bufferType = getString(KEY_BUFFER_TYPE, "").trim();
		return bufferType.equals(BUFFER_TYPE_FILE);
	}

	public void setFfmpegSettings(String value) {
		configuration.setProperty(KEY_FFMPEG_SETTINGS, value);
	}
	
	public String getFfmpegSettings() {
		return getString(KEY_FFMPEG_SETTINGS, "-threads 2 -g 1 -qscale 1 -qmin 2");
	}

	public boolean isMencoderNoOutOfSync() {
		return getBoolean(KEY_MENCODER_NO_OUT_OF_SYNC, true );
	}

	public void setMencoderNoOutOfSync(boolean value) {
		configuration.setProperty(KEY_MENCODER_NO_OUT_OF_SYNC, value);
	}

	public boolean getTrancodeBlocksMultipleConnections() {
		return configuration.getBoolean(KEY_TRANSCODE_BLOCKS_MULTIPLE_CONNECTIONS, false);
	}

	public void setTranscodeBlocksMultipleConnections(boolean value) {
		configuration.setProperty(KEY_TRANSCODE_BLOCKS_MULTIPLE_CONNECTIONS, value);
	}

	public String getCharsetEncoding() {
		return getString(KEY_CHARSET_ENCODING, "850");
	}
	
	public void setCharsetEncoding(String value) {
		configuration.setProperty(KEY_CHARSET_ENCODING, value);
	}

	public boolean isMencoderIntelligentSync() {
		return getBoolean(KEY_MENCODER_INTELLIGENT_SYNC, true);
	}

	public void setMencoderIntelligentSync(boolean value) {
		configuration.setProperty(KEY_MENCODER_INTELLIGENT_SYNC, value);
	}

	public String getFfmpegAlternativePath() {
		return getString(KEY_FFMPEG_ALTERNATIVE_PATH, null);
	}

	public void setFfmpegAlternativePath(String value) {
		configuration.setProperty(KEY_FFMPEG_ALTERNATIVE_PATH, value);
	}

	public boolean getSkipLoopFilterEnabled() {
		return getBoolean(KEY_SKIP_LOOP_FILTER_ENABLED, false);
	}

	public void setSkipLoopFilterEnabled(boolean value) {
		configuration.setProperty(KEY_SKIP_LOOP_FILTER_ENABLED, value);
	}

	public String getMencoderMainSettings() {
		return getString(KEY_MENCODER_MAIN_SETTINGS, "keyint=1:vqscale=1:vqmin=2");
	}

	public void setMencoderMainSettings(String value) {
		configuration.setProperty(KEY_MENCODER_MAIN_SETTINGS, value);
	}

	public int getLoggingLevel() {
		return getInt(KEY_LOGGING_LEVEL, 2);
	}

	public void setEnginesAsList(ArrayList<String> enginesAsList) {
		configuration.setProperty(KEY_ENGINES, listToString(enginesAsList));
	}

	public List<String> getEnginesAsList(WinUtils registry) {
		List<String> engines = stringToList(getString(KEY_ENGINES, "mencoder,avsmencoder,tsmuxer,mplayeraudio,ffmpegaudio,tsmuxeraudio,vlcvideo,mencoderwebvideo,mplayervideodump,mplayerwebaudio,vlcaudio,ffmpegdvrmsremux"));
		engines = hackAvs(registry, engines);
		return engines;
	}
	
	private static String listToString(List<String> enginesAsList) {
		return StringUtils.join(enginesAsList, LIST_SEPARATOR);
	}

	private static List<String> stringToList(String input) {
		List<String> output = new ArrayList<String>();
		Collections.addAll(output, StringUtils.split(input, LIST_SEPARATOR));
		return output;
	}
	
	// TODO: Get this out of here
	private static boolean avsHackLogged = false;
	
	// TODO: Get this out of here
	private static List<String> hackAvs(WinUtils registry, List<String> input) {
		List<String> toBeRemoved = new ArrayList<String>();
		for (String engineId : input) {
			if (engineId.startsWith("avs")  && !registry.isAvis() && PMS.get().isWindows()) {
				if (!avsHackLogged) {
					PMS.minimal("AviSynth in not installed ! You cannot use " + engineId + " as transcoding engine !"); //$NON-NLS-1$ //$NON-NLS-2$
					avsHackLogged = true;
				}
				toBeRemoved.add(engineId);
			}
		}
		List<String> output = new ArrayList<String>();
		output.addAll(input);
		output.removeAll(toBeRemoved);
		return output;
	}

	public void save() throws ConfigurationException {
		configuration.setFileName(CONFIGURATION_FILENAME);
		configuration.save();
		PMS.minimal("Configuration saved.");
	}

	public String getFolders() {
		return getString(KEY_FOLDERS, "");
	}
	
	public void setFolders(String value) {
		configuration.setProperty(KEY_FOLDERS, value);
	}		
}