/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2008  A.Brochard
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2
 * of the License only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package net.pms.configuration;

import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import net.pms.io.WinUtils;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.Platform;

/**
 * Container for all configurable PMS settings. Settings are typically defined by three things:
 * a unique key for use in the configuration file "PMS.conf", a getter (and setter) method and
 * a default value. When a key cannot be found in the current configuration, the getter will
 * return a default value. Setters only store a value, they do not permanently save it to
 * file.
 */
public class PmsConfiguration {
	private static final Logger logger = LoggerFactory.getLogger(PmsConfiguration.class);
	private static final int DEFAULT_PROXY_SERVER_PORT = -1;
	private static final int DEFAULT_SERVER_PORT = 5001;

	// MEncoder has a hardwired maximum of 8 threads for -lavcopts and -lavdopts:
	// https://code.google.com/p/ps3mediaserver/issues/detail?id=517
	private static final int MENCODER_MAX_THREADS = 8;

	private static final String KEY_ALTERNATE_SUBS_FOLDER = "alternate_subs_folder";
	private static final String KEY_ALTERNATE_THUMB_FOLDER = "alternate_thumb_folder";
	private static final String KEY_AUDIO_BITRATE = "audiobitrate";
	private static final String KEY_AUDIO_CHANNEL_COUNT = "audiochannels";
	private static final String KEY_AUDIO_RESAMPLE = "audio_resample";
	private static final String KEY_AUDIO_THUMBNAILS_METHOD = "audio_thumbnails_method";
	private static final String KEY_AUTO_UPDATE = "auto_update";
	private static final String KEY_AVISYNTH_CONVERT_FPS = "avisynth_convertfps";
	private static final String KEY_AVISYNTH_SCRIPT = "avisynth_script";
	private static final String KEY_BUFFER_TYPE = "buffertype";
	private static final String KEY_CHAPTER_INTERVAL = "chapter_interval";
	private static final String KEY_CHAPTER_SUPPORT = "chapter_support";
	private static final String KEY_CHARSET_ENCODING = "charsetencoding";
	private static final String KEY_CODEC_SPEC_SCRIPT = "codec_spec_script";
	private static final String KEY_DISABLE_FAKESIZE = "disable_fakesize";
	private static final String KEY_DVDISO_THUMBNAILS = "dvd_isos_thumbnails";
	private static final String KEY_EMBED_DTS_IN_PCM = "embed_dts_in_pcm";
	private static final String KEY_ENGINES = "engines";
	private static final String KEY_FFMPEG_ALTERNATIVE_PATH = "alternativeffmpegpath";
	private static final String KEY_FFMPEG_SETTINGS = "ffmpeg";
	private static final String KEY_FIX_25FPS_AV_MISMATCH = "fix_25fps_av_mismatch";
	private static final String KEY_FORCETRANSCODE = "forcetranscode";
	private static final String KEY_HIDE_EMPTY_FOLDERS = "hide_empty_folders";
	private static final String KEY_HIDE_ENGINENAMES = "hide_enginenames";
	private static final String KEY_HIDE_EXTENSIONS = "hide_extensions";
	private static final String KEY_HIDE_MEDIA_LIBRARY_FOLDER = "hide_media_library_folder";
	private static final String KEY_HIDE_TRANSCODE_FOLDER = "hide_transcode_folder";
	private static final String KEY_HIDE_VIDEO_SETTINGS = "hidevideosettings";
	private static final String KEY_HTTP_ENGINE_V2 = "http_engine_v2";
	private static final String KEY_IP_FILTER = "ip_filter";
	private static final String KEY_IPHOTO_ENABLED = "iphoto";
	private static final String KEY_APERTURE_ENABLED = "aperture";
	private static final String KEY_ITUNES_ENABLED = "itunes";
	private static final String KEY_LANGUAGE = "language";
	private static final String KEY_LOGGING_LEVEL = "level";
	private static final String KEY_MAX_AUDIO_BUFFER = "maxaudiobuffer";
	private static final String KEY_MAX_BITRATE = "maximumbitrate";
	private static final String KEY_MAX_MEMORY_BUFFER_SIZE = "maxvideobuffer";
	private static final String KEY_MENCODER_AC3_FIXED = "mencoder_ac3_fixed";
	private static final String KEY_MENCODER_ASS_DEFAULTSTYLE = "mencoder_ass_defaultstyle";
	private static final String KEY_MENCODER_ASS_MARGIN = "mencoder_ass_margin";
	private static final String KEY_MENCODER_ASS = "mencoder_ass";
	private static final String KEY_MENCODER_ASS_OUTLINE = "mencoder_ass_outline";
	private static final String KEY_MENCODER_ASS_SCALE = "mencoder_ass_scale";
	private static final String KEY_MENCODER_ASS_SHADOW = "mencoder_ass_shadow";
	private static final String KEY_MENCODER_AUDIO_LANGS = "mencoder_audiolangs";
	private static final String KEY_MENCODER_AUDIO_SUB_LANGS = "mencoder_audiosublangs";
	private static final String KEY_MENCODER_DECODE = "mencoder_decode";
	private static final String KEY_MENCODER_DISABLE_SUBS = "mencoder_disablesubs";
	private static final String KEY_MENCODER_FONT_CONFIG = "mencoder_fontconfig";
	private static final String KEY_MENCODER_FONT = "mencoder_font";
	private static final String KEY_MENCODER_FORCE_FPS = "mencoder_forcefps";
	private static final String KEY_MENCODER_INTELLIGENT_SYNC = "mencoder_intelligent_sync";
	private static final String KEY_MENCODER_MAIN_SETTINGS = "mencoder_encode";
	private static final String KEY_MENCODER_MAX_THREADS = "mencoder_max_threads";
	private static final String KEY_MENCODER_MT = "mencoder_mt";
	private static final String KEY_MENCODER_MUX_COMPATIBLE = "mencoder_mux_compatible";
	private static final String KEY_MENCODER_NOASS_BLUR = "mencoder_noass_blur";
	private static final String KEY_MENCODER_NOASS_OUTLINE = "mencoder_noass_outline";
	private static final String KEY_MENCODER_NOASS_SCALE = "mencoder_noass_scale";
	private static final String KEY_MENCODER_NOASS_SUBPOS = "mencoder_noass_subpos";
	private static final String KEY_MENCODER_NO_OUT_OF_SYNC = "mencoder_nooutofsync";
	private static final String KEY_MENCODER_OVERSCAN_COMPENSATION_WIDTH = "mencoder_overscan_compensation_width";
	private static final String KEY_MENCODER_OVERSCAN_COMPENSATION_HEIGHT = "mencoder_overscan_compensation_height";
	private static final String KEY_MENCODER_REMUX_AC3 = "mencoder_remux_ac3";
	private static final String KEY_MENCODER_REMUX_MPEG2 = "mencoder_remux_mpeg2";
	private static final String KEY_MENCODER_SCALER = "mencoder_scaler";
	private static final String KEY_MENCODER_SCALEX = "mencoder_scalex";
	private static final String KEY_MENCODER_SCALEY = "mencoder_scaley";
	private static final String KEY_MENCODER_SUB_CP = "mencoder_subcp";
	private static final String KEY_MENCODER_SUB_FRIBIDI = "mencoder_subfribidi";
	private static final String KEY_MENCODER_SUB_LANGS = "mencoder_sublangs";
	private static final String KEY_MENCODER_USE_PCM = "mencoder_usepcm";
	private static final String KEY_MENCODER_VOBSUB_SUBTITLE_QUALITY = "mencoder_vobsub_subtitle_quality";
	private static final String KEY_MENCODER_YADIF = "mencoder_yadif";
	private static final String KEY_MINIMIZED = "minimized";
	private static final String KEY_MIN_MEMORY_BUFFER_SIZE = "minvideobuffer";
	private static final String KEY_MIN_STREAM_BUFFER = "minwebbuffer";
	private static final String KEY_MUX_ALLAUDIOTRACKS = "tsmuxer_mux_all_audiotracks";
	private static final String KEY_NETWORK_INTERFACE = "network_interface";
	private static final String KEY_NOTRANSCODE = "notranscode";
	private static final String KEY_NUMBER_OF_CPU_CORES = "nbcores";
	private static final String KEY_OPEN_ARCHIVES = "enable_archive_browsing";
	private static final String KEY_OVERSCAN = "mencoder_overscan";
	private static final String KEY_PLUGIN_DIRECTORY = "plugins";
	private static final String KEY_PREVENTS_SLEEP = "prevents_sleep_mode";
	private static final String KEY_PROFILE_NAME = "name";
	private static final String KEY_PROXY_SERVER_PORT = "proxy";
	private static final String KEY_SERVER_HOSTNAME = "hostname";
	private static final String KEY_SERVER_PORT = "port";
	private static final String KEY_SHARES = "shares";
	private static final String KEY_SKIP_LOOP_FILTER_ENABLED = "skiploopfilter";
	private static final String KEY_SORT_METHOD = "key_sort_method";
	private static final String KEY_SUBS_COLOR = "subs_color";
	private static final String KEY_TEMP_FOLDER_PATH = "temp";
	private static final String KEY_THUMBNAIL_SEEK_POS = "thumbnail_seek_pos";
	private static final String KEY_THUMBNAILS_ENABLED = "thumbnails";
	private static final String KEY_IMAGE_THUMBNAILS_ENABLED = "image_thumbnails";
	private static final String KEY_TRANSCODE_BLOCKS_MULTIPLE_CONNECTIONS = "transcode_block_multiple_connections";
	private static final String KEY_TRANSCODE_KEEP_FIRST_CONNECTION = "transcode_keep_first_connection";
	private static final String KEY_TSMUXER_FORCEFPS = "tsmuxer_forcefps";
	private static final String KEY_TSMUXER_PREREMIX_AC3 = "tsmuxer_preremix_ac3";
	private static final String KEY_TURBO_MODE_ENABLED = "turbomode";
	private static final String KEY_USE_CACHE = "usecache";
	private static final String KEY_USE_MPLAYER_FOR_THUMBS = "use_mplayer_for_video_thumbs";
	private static final String KEY_USE_SUBTITLES = "autoloadsrt";
	private static final String KEY_VIDEOTRANSCODE_START_DELAY = "key_videotranscode_start_delay";
	private static final String KEY_VIRTUAL_FOLDERS = "vfolders";
	private static final String KEY_HDAUDIO_PASSTHROUGH = "hdaudio_passthrough";
	private static final String KEY_UPNP_PORT = "upnp_port";
	private static final String UNLIMITED_BITRATE = "0";
	private static final String DVRMS_FILE_EXTENSIONS = "dvrms_file_extensions";
	private static final String FLAC_FILE_EXTENSIONS = "flac_file_extensions";
	private static final String GIF_FILE_EXTENSIONS = "gif_file_extensions";
	private static final String ISO_FILE_EXTENSIONS = "iso_file_extensions";
	private static final String JPG_FILE_EXTENSIONS = "jpg_file_extensions";
	private static final String M4A_FILE_EXTENSIONS = "m4a_file_extensions";
	private static final String MKV_FILE_EXTENSIONS = "mkv_file_extensions";
	private static final String MP3_FILE_EXTENSIONS = "mp3_file_extensions";
	private static final String MPG_FILE_EXTENSIONS = "mpg_file_extensions";
	private static final String OGG_FILE_EXTENSIONS = "ogg_file_extensions";
	private static final String PNG_FILE_EXTENSIONS = "png_file_extensions";
	private static final String RAW_FILE_EXTENSIONS = "raw_file_extensions";
	private static final String TIF_FILE_EXTENSIONS = "tif_file_extensions";
	private static final String WEB_FILE_EXTENSIONS = "web_file_extensions";
	private HashMap<String, String[]> fileExtensionsCache = new HashMap<String, String[]>();
	

	// the name of the subdirectory under which PMS config files are stored for this build (default: PMS).
	// see Build for more details
	private static final String PROFILE_DIRECTORY_NAME = Build.getProfileDirectoryName();

	// the default profile name displayed on the renderer
	private static String HOSTNAME;

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
	private static final String BUFFER_TYPE_FILE = "file";
	private static final int MAX_MAX_MEMORY_BUFFER_SIZE = 600;
	private static final char LIST_SEPARATOR = ',';
	private static final String KEY_FOLDERS = "folders";
	private final PropertiesConfiguration configuration;
	private final TempFolder tempFolder;
	private final ProgramPathDisabler programPaths;

	/*
		The following code enables a single setting - PMS_PROFILE - to be used to
		initialize PROFILE_PATH i.e. the path to the current session's profile (AKA PMS.conf).
		It also initializes PROFILE_DIRECTORY - i.e. the directory the profile is located in -
		which is needed for configuration-by-convention detection of WEB.conf (anything else?).

		While this convention - and therefore PROFILE_DIRECTORY - will remain,
		adding more configurables - e.g. web_conf = ... - is on the TODO list.

		PMS_PROFILE is read (in this order) from the property pms.profile.path or the
		environment variable PMS_PROFILE. If PMS is launched with the command-line option
		"profiles" (e.g. from a shortcut), it displays a file chooser dialog that
		allows the pms.profile.path property to be set. This makes it easy to run PMS
		under multiple profiles without fiddling with environment variables, properties or
		command-line arguments.

		1) if PMS_PROFILE is not set, PMS.conf is located in: 

			Windows:             %ALLUSERSPROFILE%\$build
			Mac OS X:            $HOME/Library/Application Support/$build
			Everything else:     $HOME/.config/$build

		- where $build is a subdirectory that ensures incompatible PMS builds don't target/clobber
		the same configuration files. The default value for $build is "PMS". Other builds might use e.g.
		"PMS Rendr Edition" or "pms-mlx".

		2) if a relative or absolute *directory path* is supplied (the directory must exist),
		it is used as the profile directory and the profile is located there under the default profile name (PMS.conf):

			PMS_PROFILE = /absolute/path/to/dir
			PMS_PROFILE = relative/path/to/dir # relative to the working directory

		Amongst other things, this can be used to restore the legacy behaviour of locating PMS.conf in the current
		working directory e.g.:

			PMS_PROFILE=. ./PMS.sh

		3) if a relative or absolute *file path* is supplied (the file doesn't have to exist),
		it is taken to be the profile, and its parent dir is taken to be the profile (i.e. config file) dir: 

			PMS_PROFILE = PMS.conf            # profile dir = .
			PMS_PROFILE = folder/dev.conf     # profile dir = folder
			PMS_PROFILE = /path/to/some.file  # profile dir = /path/to/
	 */
	private static final String DEFAULT_PROFILE_FILENAME = "PMS.conf"; //$NON-NLS-1
	private static final String ENV_PROFILE_PATH = "PMS_PROFILE"; //$NON-NLS-1
	private static final String PROFILE_DIRECTORY; // path to directory containing PMS config files
	private static final String PROFILE_PATH; // abs path to profile file e.g. /path/to/PMS.conf
	private static final String PROPERTY_PROFILE_PATH = "pms.profile.path"; //$NON-NLS-1

	static {
		// first try the system property, typically set via the profile chooser
		String profile = System.getProperty(PROPERTY_PROFILE_PATH);

		// failing that, try the environment variable
		if (profile == null) {
			profile = System.getenv(ENV_PROFILE_PATH);
		}

		if (profile != null) {
			File f = new File(profile);

			// if it exists, we know whether it's a file or directory
			// otherwise, it must be a file since we don't autovivify directories

			if (f.exists() && f.isDirectory()) {
				PROFILE_DIRECTORY = FilenameUtils.normalize(f.getAbsolutePath());
				PROFILE_PATH = FilenameUtils.normalize(new File(f, DEFAULT_PROFILE_FILENAME).getAbsolutePath());
			} else { // doesn't exist or is a file (i.e. not a directory)
				PROFILE_PATH = FilenameUtils.normalize(f.getAbsolutePath());
				PROFILE_DIRECTORY = FilenameUtils.normalize(f.getParentFile().getAbsolutePath());
			}
		} else {
			String profileDir = null;

			if (Platform.isWindows()) {
				String programData = System.getenv("ALLUSERSPROFILE");
				if (programData != null) {
					profileDir = String.format("%s\\%s", programData, PROFILE_DIRECTORY_NAME);
				} else {
					profileDir = ""; // i.e. current (working) directory
				}
			} else if (Platform.isMac()) {
				profileDir = String.format(
					"%s/%s/%s",
					System.getProperty("user.home"),
					"/Library/Application Support",
					PROFILE_DIRECTORY_NAME
				);
			} else {
				String xdgConfigHome = System.getenv("XDG_CONFIG_HOME");

				if (xdgConfigHome == null) {
					profileDir = String.format("%s/.config/%s", System.getProperty("user.home"), PROFILE_DIRECTORY_NAME);
				} else {
					profileDir = String.format("%s/%s", xdgConfigHome, PROFILE_DIRECTORY_NAME);
				}
			}

			File f = new File(profileDir);

			if ((f.exists() || f.mkdir()) && f.isDirectory()) {
				PROFILE_DIRECTORY = FilenameUtils.normalize(f.getAbsolutePath());
			} else {
				PROFILE_DIRECTORY = FilenameUtils.normalize(new File("").getAbsolutePath());
			}

			PROFILE_PATH = FilenameUtils.normalize(new File(PROFILE_DIRECTORY, DEFAULT_PROFILE_FILENAME).getAbsolutePath());
		}
	}

	public PmsConfiguration() throws ConfigurationException, IOException {
		configuration = new PropertiesConfiguration();
		configuration.setListDelimiter((char) 0);
		configuration.setFileName(PROFILE_PATH);

		File pmsConfFile = new File(PROFILE_PATH);

		if (pmsConfFile.exists() && pmsConfFile.isFile()) {
			configuration.load(PROFILE_PATH);
		}

		tempFolder = new TempFolder(getString(KEY_TEMP_FOLDER_PATH, null));
		programPaths = createProgramPathsChain(configuration);
		Locale.setDefault(new Locale(getLanguage()));
	}

	/**
	 * Check if we have disabled something first, then check the config file,
	 * then the Windows registry, then check for a platform-specific
	 * default.
	 */
	private static ProgramPathDisabler createProgramPathsChain(Configuration configuration) {
		return new ProgramPathDisabler(
			new ConfigurationProgramPaths(configuration,
			new WindowsRegistryProgramPaths(
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

	public String getMencoderMTPath() {
		return programPaths.getMencoderMTPath();
	}

	public String getMencoderAlternatePath() {
		return programPaths.getMencoderAlternatePath();
	}

	public String getMencoderAlternateMTPath() {
		return programPaths.getMencoderAlternateMTPath();
	}

	public int getMencoderMaxThreads() {
		return Math.min(getInt(KEY_MENCODER_MAX_THREADS, getNumberOfCpuCores()), MENCODER_MAX_THREADS);
	}

	public String getDCRawPath() {
		return programPaths.getDCRaw();
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

	/**
	 * If the framerate is not recognized correctly and the video runs too fast or too
	 * slow, tsMuxeR can be forced to parse the fps from FFmpeg. Default value is true.
	 * @return True if tsMuxeR should parse fps from FFmpeg.
	 */
	public boolean isTsmuxerForceFps() {
		return configuration.getBoolean(KEY_TSMUXER_FORCEFPS, true);
	}

	/**
	 * Force tsMuxeR to mux all audio tracks.
	 * TODO: Remove this redundant code.
	 * @return True
	 */
	public boolean isTsmuxerPreremuxAc3() {
		return true;
	}

	/**
	 * The AC3 audio bitrate determines the quality of digital audio sound. An AV-receiver
	 * or amplifier has to be capable of playing this quality. Default value is 640.
	 * @return The AC3 audio bitrate.
	 */
	public int getAudioBitrate() {
		return getInt(KEY_AUDIO_BITRATE, 640);
	}

	/**
	 * Force tsMuxeR to mux all audio tracks.
	 * TODO: Remove this redundant code; getter always returns true.
	 */
	public void setTsmuxerPreremuxAc3(boolean value) {
		configuration.setProperty(KEY_TSMUXER_PREREMIX_AC3, value);
	}

	/**
	 * If the framerate is not recognized correctly and the video runs too fast or too
	 * slow, tsMuxeR can be forced to parse the fps from FFmpeg.
	 * @param value Set to true if tsMuxeR should parse fps from FFmpeg.
	 */
	public void setTsmuxerForceFps(boolean value) {
		configuration.setProperty(KEY_TSMUXER_FORCEFPS, value);
	}

	/**
	 * The server port where PMS listens for TCP/IP traffic. Default value is 5001.
	 * @return The port number.
	 */
	public int getServerPort() {
		return getInt(KEY_SERVER_PORT, DEFAULT_SERVER_PORT);
	}

	/**
	 * Set the server port where PMS must listen for TCP/IP traffic.
	 * @param value The TCP/IP port number.
	 */
	public void setServerPort(int value) {
		configuration.setProperty(KEY_SERVER_PORT, value);
	}

	/**
	 * The hostname of the server.
	 * @return The hostname if it is defined, otherwise <code>null</code>.
	 */
	public String getServerHostname() {
		String value = getString(KEY_SERVER_HOSTNAME, "");
		if (StringUtils.isNotBlank(value)) {
			return value;
		} else {
			return null;
		}
	}

	/**
	 * Set the hostname of the server.
	 * @param value The hostname.
	 */
	public void setHostname(String value) {
		configuration.setProperty(KEY_SERVER_HOSTNAME, value);
	}

	/**
	 * The TCP/IP port number for a proxy server. Default value is -1.
	 * TODO: Is this still used?
	 * @return The proxy port number.
	 */
	public int getProxyServerPort() {
		return getInt(KEY_PROXY_SERVER_PORT, DEFAULT_PROXY_SERVER_PORT);
	}

	/**
	 * Get the code of the preferred language for the PMS user interface. Default
	 * is based on the locale.
	 * @return The ISO 639 language code.
	 */
	public String getLanguage() {
		String def = Locale.getDefault().getLanguage();
		if (def == null) {
			def = "en";
		}
		String value = getString(KEY_LANGUAGE, def);
		return StringUtils.isNotBlank(value) ? value.trim() : def;
	}

	/**
	 * Return the <code>int</code> value for a given configuration key. First, the key
	 * is looked up in the current configuration settings. If it exists and contains a
	 * valid value, that value is returned. If the key contains an invalid value or
	 * cannot be found, the specified default value is returned.
	 * @param key The key to look up.
	 * @param def The default value to return when no valid key value can be found.
	 * @return The value configured for the key.
	 */
	private int getInt(String key, int def) {
		try {
			return configuration.getInt(key, def);
		} catch (ConversionException e) {
			return def;
		}
	}

	/**
	 * Return the <code>boolean</code> value for a given configuration key. First, the
	 * key is looked up in the current configuration settings. If it exists and contains
	 * a valid value, that value is returned. If the key contains an invalid value or
	 * cannot be found, the specified default value is returned.
	 * @param key The key to look up.
	 * @param def The default value to return when no valid key value can be found.
	 * @return The value configured for the key.
	 */
	private boolean getBoolean(String key, boolean def) {
		try {
			return configuration.getBoolean(key, def);
		} catch (ConversionException e) {
			return def;
		}
	}

	/**
	 * Return the <code>String</code> value for a given configuration key. First, the
	 * key is looked up in the current configuration settings. If it exists and contains
	 * a valid value, that value is returned. If the key contains an invalid value or
	 * cannot be found, the specified default value is returned.
	 * @param key The key to look up.
	 * @param def The default value to return when no valid key value can be found.
	 * @return The value configured for the key.
	 */
	private String getString(String key, String def) {
		String value = configuration.getString(key, def);
		if (value != null) {
			value = value.trim();
		}
		return value;
	}

	/**
	 * Returns the preferred minimum size for the transcoding memory buffer in megabytes.
	 * Default value is 12.
	 * @return The minimum memory buffer size.
	 */
	public int getMinMemoryBufferSize() {
		return getInt(KEY_MIN_MEMORY_BUFFER_SIZE, 12);
	}

	/**
	 * Returns the preferred maximum size for the transcoding memory buffer in megabytes.
	 * The value returned has a top limit of 600. Default value is 400.
	 * @return The maximum memory buffer size.
	 */
	public int getMaxMemoryBufferSize() {
		return Math.max(0, Math.min(MAX_MAX_MEMORY_BUFFER_SIZE, getInt(KEY_MAX_MEMORY_BUFFER_SIZE, 400)));
	}

	/**
	 * Returns the top limit that can be set for the maximum memory buffer size.
	 * @return The top limit.
	 */
	public String getMaxMemoryBufferSizeStr() {
		return String.valueOf(MAX_MAX_MEMORY_BUFFER_SIZE);
	}

	/**
	 * Set the preferred maximum for the transcoding memory buffer in megabytes. The top
	 * limit for the value is 600.
	 * @param value The maximum buffer size.
	 */
	public void setMaxMemoryBufferSize(int value) {
		configuration.setProperty(KEY_MAX_MEMORY_BUFFER_SIZE, Math.max(0, Math.min(MAX_MAX_MEMORY_BUFFER_SIZE, value)));
	}

	/**
	 * Returns the font scale used for ASS subtitling. Default value is 1.0.
	 * @return The ASS font scale.
	 */
	public String getMencoderAssScale() {
		return getString(KEY_MENCODER_ASS_SCALE, "1.0");
	}

	/**
	 * Some versions of mencoder produce garbled audio because the "ac3" codec is used
	 * instead of the "ac3_fixed" codec. Returns true if "ac3_fixed" should be used.
	 * Default is false.
	 * @see https://code.google.com/p/ps3mediaserver/issues/detail?id=1092#c1
	 * @return True if "ac3_fixed" should be used. 
	 */
	public boolean isMencoderAc3Fixed() {
		return configuration.getBoolean(KEY_MENCODER_AC3_FIXED, false);
	}

	/**
	 * Returns the margin used for ASS subtitling. Default value is 10.
	 * @return The ASS margin.
	 */
	public String getMencoderAssMargin() {
		return getString(KEY_MENCODER_ASS_MARGIN, "10");
	}

	/**
	 * Returns the outline parameter used for ASS subtitling. Default value is 1.
	 * @return The ASS outline parameter.
	 */
	public String getMencoderAssOutline() {
		return getString(KEY_MENCODER_ASS_OUTLINE, "1");
	}

	/**
	 * Returns the shadow parameter used for ASS subtitling. Default value is 1.
	 * @return The ASS shadow parameter.
	 */
	public String getMencoderAssShadow() {
		return getString(KEY_MENCODER_ASS_SHADOW, "1");
	}

	/**
	 * Returns the subfont text scale parameter used for subtitling without ASS.
	 * Default value is 3.
	 * @return The subfont text scale parameter.
	 */
	public String getMencoderNoAssScale() {
		return getString(KEY_MENCODER_NOASS_SCALE, "3");
	}

	/**
	 * Returns the subpos parameter used for subtitling without ASS.
	 * Default value is 2.
	 * @return The subpos parameter.
	 */
	public String getMencoderNoAssSubPos() {
		return getString(KEY_MENCODER_NOASS_SUBPOS, "2");
	}

	/**
	 * Returns the subfont blur parameter used for subtitling without ASS.
	 * Default value is 1.
	 * @return The subfont blur parameter.
	 */
	public String getMencoderNoAssBlur() {
		return getString(KEY_MENCODER_NOASS_BLUR, "1");
	}

	/**
	 * Returns the subfont outline parameter used for subtitling without ASS.
	 * Default value is 1.
	 * @return The subfont outline parameter.
	 */
	public String getMencoderNoAssOutline() {
		return getString(KEY_MENCODER_NOASS_OUTLINE, "1");
	}

	/**
	 * Set the subfont outline parameter used for subtitling without ASS.
	 * @param value The subfont outline parameter value to set.
	 */
	public void setMencoderNoAssOutline(String value) {
		configuration.setProperty(KEY_MENCODER_NOASS_OUTLINE, value);
	}

	/**
	 * Some versions of mencoder produce garbled audio because the "ac3" codec is used
	 * instead of the "ac3_fixed" codec.
	 * @see https://code.google.com/p/ps3mediaserver/issues/detail?id=1092#c1
	 * @param value Set to true if "ac3_fixed" should be used.
	 */
	public void setMencoderAc3Fixed(boolean value) {
		configuration.setProperty(KEY_MENCODER_AC3_FIXED, value);
	}

	/**
	 * Set the margin used for ASS subtitling.
	 * @param value The ASS margin value to set.
	 */
	public void setMencoderAssMargin(String value) {
		configuration.setProperty(KEY_MENCODER_ASS_MARGIN, value);
	}

	/**
	 * Set the outline parameter used for ASS subtitling.
	 * @param value The ASS outline parameter value to set.
	 */
	public void setMencoderAssOutline(String value) {
		configuration.setProperty(KEY_MENCODER_ASS_OUTLINE, value);
	}

	/**
	 * Set the shadow parameter used for ASS subtitling.
	 * @param value The ASS shadow parameter value to set.
	 */
	public void setMencoderAssShadow(String value) {
		configuration.setProperty(KEY_MENCODER_ASS_SHADOW, value);
	}

	/**
	 * Set the font scale used for ASS subtitling.
	 * @param value The ASS font scale value to set.
	 */
	public void setMencoderAssScale(String value) {
		configuration.setProperty(KEY_MENCODER_ASS_SCALE, value);
	}

	/**
	 * Set the subfont text scale parameter used for subtitling without ASS.
	 * @param value The subfont text scale parameter value to set.
	 */
	public void setMencoderNoAssScale(String value) {
		configuration.setProperty(KEY_MENCODER_NOASS_SCALE, value);
	}

	/**
	 * Set the subfont blur parameter used for subtitling without ASS.
	 * @param value The subfont blur parameter value to set.
	 */
	public void setMencoderNoAssBlur(String value) {
		configuration.setProperty(KEY_MENCODER_NOASS_BLUR, value);
	}

	/**
	 * Set the subpos parameter used for subtitling without ASS.
	 * @param value The subpos parameter value to set.
	 */
	public void setMencoderNoAssSubPos(String value) {
		configuration.setProperty(KEY_MENCODER_NOASS_SUBPOS, value);
	}

	/**
	 * Set the maximum number of concurrent mencoder threads.
	 * FIXME: Currently unused.
	 * @param value The maximum number of concurrent threads.
	 */
	public void setMencoderMaxThreads(int value) {
		configuration.setProperty(KEY_MENCODER_MAX_THREADS, value);
	}

	/**
	 * Set the preferred language for the PMS user interface.
	 * @param value The ISO 639 language code
	 */
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

	/**
	 * Older versions of mencoder do not support ASS/SSA subtitles on all
	 * platforms. Returns true if mencoder supports them. Default is true
	 * on Windows and OS X, false otherwise.
	 * @see https://code.google.com/p/ps3mediaserver/issues/detail?id=1097
	 * @return True if mencoder supports ASS/SSA subtitles.
	 */
	public boolean isMencoderAss() {
		return getBoolean(KEY_MENCODER_ASS, Platform.isWindows() || Platform.isMac());
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
			return "fre,jpn,ger,eng,und";
		} else {
			return "eng,fre,jpn,ger,und";
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
	
	public boolean getImageThumbnailsEnabled() {
		return getBoolean(KEY_IMAGE_THUMBNAILS_ENABLED, true);
	}

	public void setImageThumbnailsEnabled(boolean value) {
		configuration.setProperty(KEY_IMAGE_THUMBNAILS_ENABLED, value);
	}

	public int getNumberOfCpuCores() {
		int nbcores = Runtime.getRuntime().availableProcessors();
		if (nbcores < 1) {
			nbcores = 1;
		}
		return getInt(KEY_NUMBER_OF_CPU_CORES, nbcores);
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
		return getString(KEY_CODEC_SPEC_SCRIPT, "");
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
		return getBoolean(KEY_MENCODER_NO_OUT_OF_SYNC, true);
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

	public boolean getTrancodeKeepFirstConnections() {
		return configuration.getBoolean(KEY_TRANSCODE_KEEP_FIRST_CONNECTION, true);
	}

	public void setTrancodeKeepFirstConnections(boolean value) {
		configuration.setProperty(KEY_TRANSCODE_KEEP_FIRST_CONNECTION, value);
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
		return getString(KEY_MENCODER_MAIN_SETTINGS, "keyint=5:vqscale=1:vqmin=2");
	}

	public void setMencoderMainSettings(String value) {
		configuration.setProperty(KEY_MENCODER_MAIN_SETTINGS, value);
	}

	public String getMencoderVobsubSubtitleQuality() {
		return getString(KEY_MENCODER_VOBSUB_SUBTITLE_QUALITY, "3");
	}

	public void setMencoderVobsubSubtitleQuality(String value) {
		configuration.setProperty(KEY_MENCODER_VOBSUB_SUBTITLE_QUALITY, value);
	}

	public String getMencoderOverscanCompensationWidth() {
		return getString(KEY_MENCODER_OVERSCAN_COMPENSATION_WIDTH, "0");
	}

	public void setMencoderOverscanCompensationWidth(String value) {
		if (value.trim().length() == 0) {
			value = "0";
		}
		configuration.setProperty(KEY_MENCODER_OVERSCAN_COMPENSATION_WIDTH, value);
	}

	public String getMencoderOverscanCompensationHeight() {
		return getString(KEY_MENCODER_OVERSCAN_COMPENSATION_HEIGHT, "0");
	}

	public void setMencoderOverscanCompensationHeight(String value) {
		if (value.trim().length() == 0) {
			value = "0";
		}
		configuration.setProperty(KEY_MENCODER_OVERSCAN_COMPENSATION_HEIGHT, value);
	}

	public int getLoggingLevel() {
		return getInt(KEY_LOGGING_LEVEL, 2);
	}

	public void setEnginesAsList(ArrayList<String> enginesAsList) {
		configuration.setProperty(KEY_ENGINES, listToString(enginesAsList));
	}

	public List<String> getEnginesAsList(WinUtils registry) {
		List<String> engines = stringToList(getString(KEY_ENGINES, "mencoder,avsmencoder,tsmuxer,mplayeraudio,ffmpegaudio,tsmuxeraudio,vlcvideo,mencoderwebvideo,mplayervideodump,mplayerwebaudio,vlcaudio,ffmpegdvrmsremux,rawthumbs"));
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
			if (engineId.startsWith("avs") && !registry.isAvis() && Platform.isWindows()) {
				if (!avsHackLogged) {
					logger.info("AviSynth is not installed. You cannot use " + engineId + " as a transcoding engine."); //$NON-NLS-1$ //$NON-NLS-2$
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
		configuration.save();
		logger.info("Configuration saved to: " + PROFILE_PATH);
	}

	public String getFolders() {
		return getString(KEY_FOLDERS, "");
	}

	public void setFolders(String value) {
		configuration.setProperty(KEY_FOLDERS, value);
	}

	public String getNetworkInterface() {
		return getString(KEY_NETWORK_INTERFACE, "");
	}

	public void setNetworkInterface(String value) {
		configuration.setProperty(KEY_NETWORK_INTERFACE, value);
	}

	public boolean isHideEngineNames() {
		return getBoolean(KEY_HIDE_ENGINENAMES, false);
	}

	public void setHideEngineNames(boolean value) {
		configuration.setProperty(KEY_HIDE_ENGINENAMES, value);
	}

	public boolean isHideExtensions() {
		return getBoolean(KEY_HIDE_EXTENSIONS, false);
	}

	public void setHideExtensions(boolean value) {
		configuration.setProperty(KEY_HIDE_EXTENSIONS, value);
	}

	public String getShares() {
		return getString(KEY_SHARES, "");
	}

	public void setShares(String value) {
		configuration.setProperty(KEY_SHARES, value);
	}

	public String getNoTranscode() {
		return getString(KEY_NOTRANSCODE, "");
	}

	public void setNoTranscode(String value) {
		configuration.setProperty(KEY_NOTRANSCODE, value);
	}

	public String getForceTranscode() {
		return getString(KEY_FORCETRANSCODE, "");
	}

	public void setForceTranscode(String value) {
		configuration.setProperty(KEY_FORCETRANSCODE, value);
	}

	public void setMencoderMT(boolean value) {
		configuration.setProperty(KEY_MENCODER_MT, value);
	}

	public boolean getMencoderMT() {
		boolean isMultiCore = getNumberOfCpuCores() > 1;
		return getBoolean(KEY_MENCODER_MT, isMultiCore);
	}

	public void setRemuxAC3(boolean value) {
		configuration.setProperty(KEY_MENCODER_REMUX_AC3, value);
	}

	public boolean isRemuxAC3() {
		return getBoolean(KEY_MENCODER_REMUX_AC3, true);
	}

	public void setMencoderRemuxMPEG2(boolean value) {
		configuration.setProperty(KEY_MENCODER_REMUX_MPEG2, value);
	}

	public boolean isMencoderRemuxMPEG2() {
		return getBoolean(KEY_MENCODER_REMUX_MPEG2, true);
	}

	public void setDisableFakeSize(boolean value) {
		configuration.setProperty(KEY_DISABLE_FAKESIZE, value);
	}

	public boolean isDisableFakeSize() {
		return getBoolean(KEY_DISABLE_FAKESIZE, false);
	}

	public void setMencoderAssDefaultStyle(boolean value) {
		configuration.setProperty(KEY_MENCODER_ASS_DEFAULTSTYLE, value);
	}

	public boolean isMencoderAssDefaultStyle() {
		return getBoolean(KEY_MENCODER_ASS_DEFAULTSTYLE, false);
	}

	public int getMEncoderOverscan() {
		return getInt(KEY_OVERSCAN, 0);
	}

	public void setMEncoderOverscan(int value) {
		configuration.setProperty(KEY_OVERSCAN, value);
	}

	public int getSortMethod() {
		return getInt(KEY_SORT_METHOD, 0);
	}

	public void setSortMethod(int value) {
		configuration.setProperty(KEY_SORT_METHOD, value);
	}

	public int getAudioThumbnailMethod() {
		return getInt(KEY_AUDIO_THUMBNAILS_METHOD, 0);
	}

	public void setAudioThumbnailMethod(int value) {
		configuration.setProperty(KEY_AUDIO_THUMBNAILS_METHOD, value);
	}

	public String getAlternateThumbFolder() {
		return getString(KEY_ALTERNATE_THUMB_FOLDER, "");
	}

	public void setAlternateThumbFolder(String value) {
		configuration.setProperty(KEY_ALTERNATE_THUMB_FOLDER, value);
	}

	public String getAlternateSubsFolder() {
		return getString(KEY_ALTERNATE_SUBS_FOLDER, "");
	}

	public void setAlternateSubsFolder(String value) {
		configuration.setProperty(KEY_ALTERNATE_SUBS_FOLDER, value);
	}

	public void setDTSEmbedInPCM(boolean value) {
		configuration.setProperty(KEY_EMBED_DTS_IN_PCM, value);
	}

	public boolean isDTSEmbedInPCM() {
		return getBoolean(KEY_EMBED_DTS_IN_PCM, false);
	}
	
	public void setHDAudioPassthrough(boolean value) {
		configuration.setProperty(KEY_HDAUDIO_PASSTHROUGH, value);
	}

	public boolean isHDAudioPassthrough() {
		return getBoolean(KEY_HDAUDIO_PASSTHROUGH, false);
	}

	public void setMencoderMuxWhenCompatible(boolean value) {
		configuration.setProperty(KEY_MENCODER_MUX_COMPATIBLE, value);
	}

	public boolean isMencoderMuxWhenCompatible() {
		return getBoolean(KEY_MENCODER_MUX_COMPATIBLE, true);
	}

	public void setMuxAllAudioTracks(boolean value) {
		configuration.setProperty(KEY_MUX_ALLAUDIOTRACKS, value);
	}

	public boolean isMuxAllAudioTracks() {
		return getBoolean(KEY_MUX_ALLAUDIOTRACKS, false);
	}

	public void setUseMplayerForVideoThumbs(boolean value) {
		configuration.setProperty(KEY_USE_MPLAYER_FOR_THUMBS, value);
	}

	public boolean isUseMplayerForVideoThumbs() {
		return getBoolean(KEY_USE_MPLAYER_FOR_THUMBS, false);
	}

	public String getIpFilter() {
		return getString(KEY_IP_FILTER, "");
	}

	public void setIpFilter(String value) {
		configuration.setProperty(KEY_IP_FILTER, value);
	}

	public void setPreventsSleep(boolean value) {
		configuration.setProperty(KEY_PREVENTS_SLEEP, value);
	}

	public boolean isPreventsSleep() {
		return getBoolean(KEY_PREVENTS_SLEEP, false);
	}

	public void setHTTPEngineV2(boolean value) {
		configuration.setProperty(KEY_HTTP_ENGINE_V2, value);
	}

	public boolean isHTTPEngineV2() {
		return getBoolean(KEY_HTTP_ENGINE_V2, true);
	}

	public boolean getIphotoEnabled() {
		return getBoolean(KEY_IPHOTO_ENABLED, false);
	}

	public void setIphotoEnabled(boolean value) {
		configuration.setProperty(KEY_IPHOTO_ENABLED, value);
	}
	
	public boolean getApertureEnabled() {
		return getBoolean(KEY_APERTURE_ENABLED, false);
	}

	public void setApertureEnabled(boolean value) {
		configuration.setProperty(KEY_APERTURE_ENABLED, value);
	}

	public boolean getItunesEnabled() {
		return getBoolean(KEY_ITUNES_ENABLED, false);
	}

	public void setItunesEnabled(boolean value) {
		configuration.setProperty(KEY_ITUNES_ENABLED, value);
	}

	public boolean isHideEmptyFolders() {
		return getBoolean(PmsConfiguration.KEY_HIDE_EMPTY_FOLDERS, false);
	}

	public void setHideEmptyFolders(final boolean value) {
		this.configuration.setProperty(PmsConfiguration.KEY_HIDE_EMPTY_FOLDERS, value);
	}

	public boolean isHideMediaLibraryFolder() {
		return getBoolean(PmsConfiguration.KEY_HIDE_MEDIA_LIBRARY_FOLDER, false);
	}

	public void setHideMediaLibraryFolder(final boolean value) {
		this.configuration.setProperty(PmsConfiguration.KEY_HIDE_MEDIA_LIBRARY_FOLDER, value);
	}

	public boolean getHideTranscodeEnabled() {
		return getBoolean(KEY_HIDE_TRANSCODE_FOLDER, false);
	}

	public void setHideTranscodeEnabled(boolean value) {
		configuration.setProperty(KEY_HIDE_TRANSCODE_FOLDER, value);
	}

	public boolean isDvdIsoThumbnails() {
		return getBoolean(KEY_DVDISO_THUMBNAILS, false);
	}

	public void setDvdIsoThumbnails(boolean value) {
		configuration.setProperty(KEY_DVDISO_THUMBNAILS, value);
	}

	public Object getCustomProperty(String property) {
		return configuration.getProperty(property);
	}

	public void setCustomProperty(String property, Object value) {
		configuration.setProperty(property, value);
	}

	public boolean isChapterSupport() {
		return getBoolean(KEY_CHAPTER_SUPPORT, false);
	}

	public void setChapterSupport(boolean value) {
		configuration.setProperty(KEY_CHAPTER_SUPPORT, value);
	}

	public int getChapterInterval() {
		return getInt(KEY_CHAPTER_INTERVAL, 5);
	}

	public void setChapterInterval(int value) {
		configuration.setProperty(KEY_CHAPTER_INTERVAL, value);
	}

	public int getSubsColor() {
		if (!GraphicsEnvironment.isHeadless()) {
			return getInt(KEY_SUBS_COLOR, Color.WHITE.getRGB());
		} else {
			return 0xffffff;
		}
	}

	public void setSubsColor(int value) {
		configuration.setProperty(KEY_SUBS_COLOR, value);
	}

	public boolean isFix25FPSAvMismatch() {
		return getBoolean(KEY_FIX_25FPS_AV_MISMATCH, false);
	}

	public void setFix25FPSAvMismatch(boolean value) {
		configuration.setProperty(KEY_FIX_25FPS_AV_MISMATCH, value);
	}

	public int getVideoTranscodeStartDelay() {
		return getInt(KEY_VIDEOTRANSCODE_START_DELAY, 6);
	}

	public void setVideoTranscodeStartDelay(int value) {
		configuration.setProperty(KEY_VIDEOTRANSCODE_START_DELAY, value);
	}

	public boolean isAudioResample() {
		return getBoolean(KEY_AUDIO_RESAMPLE, true);
	}

	public void setAudioResample(boolean value) {
		configuration.setProperty(KEY_AUDIO_RESAMPLE, value);
	}

	public String getVirtualFolders() {
		return getString(KEY_VIRTUAL_FOLDERS, "");
	}

	public String getProfilePath() {
		return PROFILE_PATH;
	}

	public String getProfileDirectory() {
		return PROFILE_DIRECTORY;
	}

	public String getPluginDirectory() {
		return getString(KEY_PLUGIN_DIRECTORY, "plugins");
	}

	public void setPluginDirectory(String value) {
		configuration.setProperty(KEY_PLUGIN_DIRECTORY, value);
	}

	public String getProfileName() {
		if (HOSTNAME == null) { // calculate this lazily
			try {
				HOSTNAME = InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException e) {
				logger.info("Can't determine hostname");
				HOSTNAME = "unknown host";
			}
		}

		return getString(KEY_PROFILE_NAME, HOSTNAME);
	}

	public boolean isAutoUpdate() {
		return Build.isUpdatable() && configuration.getBoolean(KEY_AUTO_UPDATE, false);
	}

	public void setAutoUpdate(boolean value) {
		configuration.setProperty(KEY_AUTO_UPDATE, value);
	}
	
	public String getIMConvertPath() {
		return programPaths.getIMConvertPath();
	}

	public int getUpnpPort() {
		return getInt(KEY_UPNP_PORT, 1900);
	}
	
	public String[] getDvrmsFileExtensions(){
		if(fileExtensionsCache.get(DVRMS_FILE_EXTENSIONS) == null) {
			fileExtensionsCache.put(DVRMS_FILE_EXTENSIONS, configuration.getString(DVRMS_FILE_EXTENSIONS , "dvr-ms|dvr").split("\\|"));
		}
		return fileExtensionsCache.get(DVRMS_FILE_EXTENSIONS);
	}
	
	public String[] getFlacFileExtensions(){
		if(fileExtensionsCache.get(FLAC_FILE_EXTENSIONS) == null) {
			fileExtensionsCache.put(FLAC_FILE_EXTENSIONS, configuration.getString(FLAC_FILE_EXTENSIONS , "flac|mlp|fla").split("\\|"));
		}
		return fileExtensionsCache.get(FLAC_FILE_EXTENSIONS);
	}
	
	public String[] getGifFileExtensions(){
		if(fileExtensionsCache.get(GIF_FILE_EXTENSIONS) == null) {
			fileExtensionsCache.put(GIF_FILE_EXTENSIONS, configuration.getString(GIF_FILE_EXTENSIONS , "gif").split("\\|"));
		}
		return fileExtensionsCache.get(GIF_FILE_EXTENSIONS);
	}
	
	public String[] getIsoFileExtensions(){
		if(fileExtensionsCache.get(ISO_FILE_EXTENSIONS) == null) {
			fileExtensionsCache.put(ISO_FILE_EXTENSIONS, configuration.getString(ISO_FILE_EXTENSIONS , "iso|img").split("\\|"));
		}
		return fileExtensionsCache.get(ISO_FILE_EXTENSIONS);
	}
	
	public String[] getJpgFileExtensions(){
		if(fileExtensionsCache.get(JPG_FILE_EXTENSIONS) == null) {
			fileExtensionsCache.put(JPG_FILE_EXTENSIONS, configuration.getString(JPG_FILE_EXTENSIONS , "jpeg|jpg|jpe").split("\\|"));
		}
		return fileExtensionsCache.get(JPG_FILE_EXTENSIONS);
	}
	
	public String[] getM4aFileExtensions(){
		if(fileExtensionsCache.get(M4A_FILE_EXTENSIONS) == null) {
			fileExtensionsCache.put(M4A_FILE_EXTENSIONS, configuration.getString(M4A_FILE_EXTENSIONS , "wma|m4a|aac").split("\\|"));
		}
		return fileExtensionsCache.get(M4A_FILE_EXTENSIONS);
	}
	
	public String[] getMkvFileExtensions(){
		if(fileExtensionsCache.get(MKV_FILE_EXTENSIONS) == null) {
			fileExtensionsCache.put(MKV_FILE_EXTENSIONS, configuration.getString(MKV_FILE_EXTENSIONS , "mkv|dv|ty|mov|ogm|ogv|hdmov|hdm|rmv|rmvb|rm|asf|evo|asx|flv|m2v|3gp|3g2").split("\\|"));
		}
		return fileExtensionsCache.get(MKV_FILE_EXTENSIONS);
	}
	
	public String[] getMp3FileExtensions(){
		if(fileExtensionsCache.get(MP3_FILE_EXTENSIONS) == null) {
			fileExtensionsCache.put(MP3_FILE_EXTENSIONS, configuration.getString(MP3_FILE_EXTENSIONS , "mp3|wav").split("\\|"));
		}
		return fileExtensionsCache.get(MP3_FILE_EXTENSIONS);
	}
	
	public String[] getMpgFileExtensions(){
		if(fileExtensionsCache.get(MPG_FILE_EXTENSIONS) == null) {
			fileExtensionsCache.put(MPG_FILE_EXTENSIONS, configuration.getString(MPG_FILE_EXTENSIONS , "mpg|mpeg|mpe|mod|tivo|ty|tmf|ts|tp|m2t|m2ts|m2p|mts|mp4|m4v|avi|wmv|wm|vob|divx|div|vdr").split("\\|"));
		}
		return fileExtensionsCache.get(MPG_FILE_EXTENSIONS);
	}
	
	public String[] getOggFileExtensions(){
		if(fileExtensionsCache.get(OGG_FILE_EXTENSIONS) == null) {
			fileExtensionsCache.put(OGG_FILE_EXTENSIONS, configuration.getString(OGG_FILE_EXTENSIONS , "dts|mka|ape|ogg|shn|mpc|ra|mp2|wv|oma|aa3|at3|aif|aiff").split("\\|"));
		}
		return fileExtensionsCache.get(OGG_FILE_EXTENSIONS);
	}
	
	public String[] getPngFileExtensions(){
		if(fileExtensionsCache.get(PNG_FILE_EXTENSIONS) == null) {
			fileExtensionsCache.put(PNG_FILE_EXTENSIONS, configuration.getString(PNG_FILE_EXTENSIONS , "png").split("\\|"));
		}
		return fileExtensionsCache.get(PNG_FILE_EXTENSIONS);
	}
	
	public String[] getRawFileExtensions(){
		if(fileExtensionsCache.get(RAW_FILE_EXTENSIONS) == null) {
			fileExtensionsCache.put(RAW_FILE_EXTENSIONS, configuration.getString(RAW_FILE_EXTENSIONS , "arw|cr2|crw|dng|raf|mrw|nef|pef|srf|orf").split("\\|"));
		}
		return fileExtensionsCache.get(RAW_FILE_EXTENSIONS);
	}
	
	public String[] getTifFileExtensions(){
		if(fileExtensionsCache.get(TIF_FILE_EXTENSIONS) == null) {
			fileExtensionsCache.put(TIF_FILE_EXTENSIONS, configuration.getString(TIF_FILE_EXTENSIONS , "tif|tiff").split("\\|"));
		}
		return fileExtensionsCache.get(TIF_FILE_EXTENSIONS);
	}
	
	public String[] getWebFileExtensions(){
		if(fileExtensionsCache.get(WEB_FILE_EXTENSIONS) == null) {
			fileExtensionsCache.put(WEB_FILE_EXTENSIONS, configuration.getString(WEB_FILE_EXTENSIONS , "http|mms|rtsp|rtp|udp|screen").split("\\|"));
		}
		return fileExtensionsCache.get(WEB_FILE_EXTENSIONS);
	}
}
