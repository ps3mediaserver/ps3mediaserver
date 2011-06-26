package net.pms.configuration;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import net.pms.Messages;
import net.pms.PMS;
import net.pms.dlna.MediaInfoParser;
import net.pms.dlna.RootFolder;
import net.pms.formats.Format;
import net.pms.io.OutputParams;
import net.pms.io.ProcessWrapperImpl;
import net.pms.network.HTTPResource;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.Platform;

public class RendererConfiguration {
	/*
	 * Static section
	 */
	private static final Logger logger = LoggerFactory.getLogger(RendererConfiguration.class);
	private static ArrayList<RendererConfiguration> renderersConfs;
	private static RendererConfiguration defaultConf;

	public static RendererConfiguration getDefaultConf() {
		return defaultConf;
	}

	public static void loadRendererConfigurations() {
		renderersConfs = new ArrayList<RendererConfiguration>();
		try {
			defaultConf = new RendererConfiguration();
		} catch (ConfigurationException e) {
		}

		File renderersDir = new File("renderers");

		if (renderersDir.exists()) {
			logger.info("Loading renderer configurations from " + renderersDir.getAbsolutePath());

			File confs[] = renderersDir.listFiles();
			int rank = 1;
			for (File f : confs) {
				if (f.getName().endsWith(".conf")) {
					try {
						logger.info("Loading configuration file: " + f.getName());
						RendererConfiguration r = new RendererConfiguration(f);
						r.rank = rank++;
						renderersConfs.add(r);
					} catch (ConfigurationException ce) {
						logger.info("Error in loading configuration of: " + f.getAbsolutePath());
					}

				}
			}
		}
	}

	private InetAddress currentRendererAddress;
	private int speedInMbits;
	private RootFolder rootFolder;
	private boolean mediaLibraryAdded = false;

	public boolean isMediaLibraryAdded() {
		return mediaLibraryAdded;
	}

	public void setMediaLibraryAdded(boolean mediaLibraryAdded) {
		this.mediaLibraryAdded = mediaLibraryAdded;
	}

	public static void resetAllRenderers() {
		for (RendererConfiguration r : renderersConfs) {
			r.mediaLibraryAdded = false;
		}
	}

	public RootFolder getRootFolder() {
		return getRootFolder(true);
	}

	public RootFolder getRootFolder(boolean initialize) {
		if (rootFolder == null) {
			rootFolder = new RootFolder();
			if (initialize) {
				try {
					PMS.get().manageRoot(this);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return rootFolder;
	}

	public int getSpeedInMbits() {
		return speedInMbits;
	}

	public InetAddress getCurrentRendererAddress() {
		return currentRendererAddress;
	}

	public void associateIP(InetAddress sa) {
		currentRendererAddress = sa;
		logger.info("Renderer " + this + " found on this address: " + sa);

		// let's get that speed
		OutputParams op = new OutputParams(null);
		op.log = true;
		op.maxBufferSize = 1;
		String count = Platform.isWindows() ? "-n" : "-c";
		String size = Platform.isWindows() ? "-l" : "-s";
		final ProcessWrapperImpl pw = new ProcessWrapperImpl(new String[]{"ping", count, "3", size, "64000", sa.getHostAddress()}, op, true, false);
		Runnable r = new Runnable() {
			public void run() {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
				}
				pw.stopProcess();
			}
		};
		Thread failsafe = new Thread(r);
		failsafe.start();
		pw.run();
		List<String> ls = pw.getOtherResults();
		int time = 0;
		int c = 0;
		for (String line : ls) {
			int msPos = line.indexOf("ms");
			try {
				if (msPos > -1) {
					String timeString = line.substring(line.lastIndexOf("=", msPos) + 1, msPos).trim();
					time += Double.parseDouble(timeString);
					c++;
				}
			} catch (Exception e) {
				// no big deal
			}
		}
		if (c > 0) {
			time = (int) (time / c);
		}
		if (time > 0) {
			speedInMbits = (int) (1024 / time);
			logger.info("Renderer " + this + " has an estimated network speed of: " + speedInMbits + " Mb/s");
		}
	}

	public static void main(String args[]) throws Exception {
		new RendererConfiguration().associateIP(InetAddress.getByName("192.168.0.10"));
	}

	public static RendererConfiguration getRendererConfigurationBySocketAddress(InetAddress sa) {
		for (RendererConfiguration r : renderersConfs) {
			if (sa.equals(r.currentRendererAddress)) {
				return r;
			}
		}
		return null;
	}

	public static RendererConfiguration getRendererConfiguration(int mediarenderer) {
		for (RendererConfiguration r : renderersConfs) {
			if (r.rank == mediarenderer) {
				return r;
			}
		}
		return defaultConf;
	}

	public static RendererConfiguration getRendererConfigurationByUA(String userAgentString) {
		for (RendererConfiguration r : renderersConfs) {
			if (r.matchUserAgent(userAgentString)) {
				return manageRendererMatch(r);
			}
		}
		return null;
	}

	private static RendererConfiguration manageRendererMatch(RendererConfiguration r) {
		if (r.currentRendererAddress == null) {
			return r; // no other clients with the same renderer on this network (yet)
		} else {
			// seems there's another same machine on the network
			logger.info("Another renderer like " + r.getRendererName() + " was found!");
			try {
				RendererConfiguration duplicated = new RendererConfiguration(r.configurationFile);
				renderersConfs.add(duplicated);
				return duplicated;
			} catch (ConfigurationException e) {
				logger.info("Serious error in adding a duplicated renderer: " + e.getMessage());
			}
		}
		return r;
	}

	public static RendererConfiguration getRendererConfigurationByUAAHH(String header) {
		for (RendererConfiguration r : renderersConfs) {
			if (StringUtils.isNotBlank(r.getUserAgentAdditionalHttpHeader()) && header.startsWith(r.getUserAgentAdditionalHttpHeader())) {
				String value = header.substring(header.indexOf(":", r.getUserAgentAdditionalHttpHeader().length()) + 1);
				if (r.matchAdditionalUserAgent(value)) {
					return manageRendererMatch(r);
				}
			}
		}
		return null;
	}
	/*
	 * 
	 */
	private File configurationFile;
	private PropertiesConfiguration configuration;
	private FormatConfiguration formatConfiguration;

	public FormatConfiguration getFormatConfiguration() {
		return formatConfiguration;
	}
	private int rank;
	private Map<String, String> mimes;
	private Map<String, String> DLNAPN;

	public int getRank() {
		return rank;
	}

	// Those 'is' methods should disappear
	public boolean isXBOX() {
		return getRendererName().toUpperCase().contains("XBOX");
	}

	public boolean isXBMC() {
		return getRendererName().toUpperCase().contains("XBMC");
	}

	public boolean isPS3() {
		return getRendererName().toUpperCase().contains("PLAYSTATION") || getRendererName().toUpperCase().contains("PS3");
	}

	public boolean isBRAVIA() {
		return getRendererName().toUpperCase().contains("BRAVIA");
	}

	public boolean isFDSSDP() {
		return getRendererName().toUpperCase().contains("FDSSDP");
	}

	private static final String RENDERER_NAME = "RendererName";
	private static final String RENDERER_ICON = "RendererIcon";
	private static final String USER_AGENT = "UserAgentSearch";
	private static final String USER_AGENT_ADDITIONAL_HEADER = "UserAgentAdditionalHeader";
	private static final String USER_AGENT_ADDITIONAL_SEARCH = "UserAgentAdditionalHeaderSearch";
	private static final String VIDEO = "Video";
	private static final String AUDIO = "Audio";
	private static final String IMAGE = "Image";
	private static final String SEEK_BY_TIME = "SeekByTime";

	public static final String MPEGPSAC3 = "MPEGAC3";
	public static final String MPEGTSAC3 = "MPEGTSAC3";
	public static final String WMV = "WMV";
	public static final String LPCM = "LPCM";
	public static final String WAV = "WAV";
	public static final String MP3 = "MP3";

	private static final String TRANSCODE_AUDIO = "TranscodeAudio";
	private static final String TRANSCODE_VIDEO = "TranscodeVideo";
	private static final String DEFAULT_VBV_BUFSIZE = "DefaultVBVBufSize";
	private static final String MUX_H264_WITH_MPEGTS = "MuxH264ToMpegTS";
	private static final String MUX_DTS_TO_MPEG = "MuxDTSToMpeg";
	private static final String WRAP_DTS_INTO_PCM = "WrapDTSIntoPCM";
	private static final String MUX_LPCM_TO_MPEG = "MuxLPCMToMpeg";
	private static final String MAX_VIDEO_BITRATE = "MaxVideoBitrateMbps";
	private static final String MAX_VIDEO_WIDTH = "MaxVideoWidth";
	private static final String MAX_VIDEO_HEIGHT = "MaxVideoHeight";
	private static final String USE_SAME_EXTENSION = "UseSameExtension";
	private static final String MIME_TYPES_CHANGES = "MimeTypesChanges";
	private static final String TRANSCODE_EXT = "TranscodeExtensions";
	private static final String STREAM_EXT = "StreamExtensions";
	private static final String H264_L41_LIMITED = "H264Level41Limited";
	private static final String TRANSCODE_AUDIO_441KHZ = "TranscodeAudioTo441kHz";
	private static final String TRANSCODED_SIZE = "TranscodedVideoFileSize";
	private static final String DLNA_PN_CHANGES = "DLNAProfileChanges";
	private static final String TRANSCODE_FAST_START = "TranscodeFastStart";
	private static final String AUTO_EXIF_ROTATE = "AutoExifRotate";
	private static final String DLNA_ORGPN_USE = "DLNAOrgPN";
	private static final String DLNA_LOCALIZATION_REQUIRED = "DLNALocalizationRequired";
	private static final String MEDIAPARSERV2 = "MediaInfo";
	private static final String MEDIAPARSERV2_THUMB = "MediaParserV2_ThumbnailGeneration";
	private static final String SUPPORTED = "Supported";
	private static final String CUSTOM_MENCODER_QUALITYSETTINGS = "CustomMencoderQualitySettings";
	private static final String DLNA_TREE_HACK = "CreateDLNATreeFaster";

	// Ditlew
	private static final String SHOW_DVD_TITLE_DURATION = "ShowDVDTitleDuration";
	private static final String CBR_VIDEO_BITRATE = "CBRVideoBitrate";
	private static final String BYTE_TO_TIMESEEK_REWIND_SECONDS = "ByteToTimeseekRewindSeconds";

	// Ditlew
	public int getByteToTimeseekRewindSeconds() {
		return getInt(BYTE_TO_TIMESEEK_REWIND_SECONDS, 0);
	}

	// Ditlew
	public int getCBRVideoBitrate() {
		return getInt(CBR_VIDEO_BITRATE, 0);
	}

	// Ditlew
	public boolean isShowDVDTitleDuration() {
		return getBoolean(SHOW_DVD_TITLE_DURATION, false);
	}

	private RendererConfiguration() throws ConfigurationException {
		this(null);
	}
	private Pattern userAgentPattern = null;
	private Pattern userAgentAddtionalPattern = null;

	public RendererConfiguration(File f) throws ConfigurationException {
		configuration = new PropertiesConfiguration();
		configuration.setListDelimiter((char) 0);
		configurationFile = f;
		if (f != null) {
			configuration.load(f);
		}
		userAgentPattern = Pattern.compile(getUserAgent(), Pattern.CASE_INSENSITIVE);
		userAgentAddtionalPattern = Pattern.compile(getUserAgentAdditionalHttpHeaderSearch(), Pattern.CASE_INSENSITIVE);
		mimes = new HashMap<String, String>();
		String mimeTypes = configuration.getString(MIME_TYPES_CHANGES, null);
		if (StringUtils.isNotBlank(mimeTypes)) {
			StringTokenizer st = new StringTokenizer(mimeTypes, "|");
			while (st.hasMoreTokens()) {
				String mime_change = st.nextToken().trim();
				int equals = mime_change.indexOf("=");
				if (equals > -1) {
					String old = mime_change.substring(0, equals).trim().toLowerCase();
					String nw = mime_change.substring(equals + 1).trim().toLowerCase();
					mimes.put(old, nw);
				}
			}
		}
		DLNAPN = new HashMap<String, String>();
		String DLNAPNchanges = configuration.getString(DLNA_PN_CHANGES, null);
		if (DLNAPNchanges != null) {
			logger.trace("Config DLNAPNchanges: " + DLNAPNchanges);
		}
		if (StringUtils.isNotBlank(DLNAPNchanges)) {
			StringTokenizer st = new StringTokenizer(DLNAPNchanges, "|");
			while (st.hasMoreTokens()) {
				String DLNAPN_change = st.nextToken().trim();
				int equals = DLNAPN_change.indexOf("=");
				if (equals > -1) {
					String old = DLNAPN_change.substring(0, equals).trim().toUpperCase();
					String nw = DLNAPN_change.substring(equals + 1).trim().toUpperCase();
					DLNAPN.put(old, nw);
				}
			}
		}
		if (f == null) {
			// the default renderer supports everything !
			configuration.addProperty(MEDIAPARSERV2, true);
			configuration.addProperty(MEDIAPARSERV2_THUMB, true);
			configuration.addProperty(SUPPORTED, "f:.+");
		}
		if (isMediaParserV2()) {
			formatConfiguration = new FormatConfiguration(configuration.getList(SUPPORTED));
		}
	}

	public String getDLNAPN(String old) {
		if (DLNAPN.containsKey(old)) {
			return DLNAPN.get(old);
		}
		return old;
	}

	public boolean supportsFormat(Format f) {
		switch (f.getType()) {
			case Format.VIDEO:
				return isVideoSupported();
			case Format.AUDIO:
				return isAudioSupported();
			case Format.IMAGE:
				return isImageSupported();
			default:
				break;
		}
		return false;
	}

	public boolean isVideoSupported() {
		return getBoolean(VIDEO, true);
	}

	public boolean isAudioSupported() {
		return getBoolean(AUDIO, true);
	}

	public boolean isImageSupported() {
		return getBoolean(IMAGE, true);
	}

	public boolean isTranscodeToWMV() {
		return getVideoTranscode().startsWith(WMV);
	}

	public boolean isTranscodeToAC3() {
		return isTranscodeToMPEGPSAC3() || isTranscodeToMPEGTSAC3();
	}

	public boolean isTranscodeToMPEGPSAC3() {
		return getVideoTranscode().startsWith(MPEGPSAC3);
	}

	public boolean isTranscodeToMPEGTSAC3() {
		return getVideoTranscode().startsWith(MPEGTSAC3);
	}

	public boolean isAutoRotateBasedOnExif() {
		return getBoolean(AUTO_EXIF_ROTATE, false);
	}

	public boolean isTranscodeToMP3() {
		return getAudioTranscode().startsWith(MP3);
	}

	public boolean isTranscodeToLPCM() {
		return getAudioTranscode().startsWith(LPCM);
	}

	public boolean isTranscodeToWAV() {
		return getAudioTranscode().startsWith(WAV);
	}

	public boolean isTranscodeAudioTo441() {
		return getBoolean(TRANSCODE_AUDIO_441KHZ, false);
	}

	public boolean isH264Level41Limited() {
		return getBoolean(H264_L41_LIMITED, false);
	}

	public boolean isTranscodeFastStart() {
		return getBoolean(TRANSCODE_FAST_START, false);
	}

	public boolean isDLNALocalizationRequired() {
		return getBoolean(DLNA_LOCALIZATION_REQUIRED, false);
	}

	public String getMimeType(String mimetype) {
		if (isMediaParserV2()) {
			if (mimetype != null && mimetype.equals(HTTPResource.VIDEO_TRANSCODE)) {
				mimetype = getFormatConfiguration().match(FormatConfiguration.MPEGPS, FormatConfiguration.MPEG2, FormatConfiguration.AC3);
				if (isTranscodeToMPEGTSAC3()) {
					mimetype = getFormatConfiguration().match(FormatConfiguration.MPEGTS, FormatConfiguration.MPEG2, FormatConfiguration.AC3);
				} else if (isTranscodeToWMV()) {
					mimetype = getFormatConfiguration().match(FormatConfiguration.WMV, FormatConfiguration.WMV, FormatConfiguration.WMA);
				}
			} else if (mimetype != null && mimetype.equals(HTTPResource.AUDIO_TRANSCODE)) {
				mimetype = getFormatConfiguration().match(FormatConfiguration.LPCM, null, null);
				if (mimetype != null) {
					if (isTranscodeAudioTo441()) {
						mimetype += ";rate=44100;channels=2";
					} else {
						mimetype += ";rate=48000;channels=2";
					}
				}
				if (isTranscodeToWAV()) {
					mimetype = getFormatConfiguration().match(FormatConfiguration.WAV, null, null);
				} else if (isTranscodeToMP3()) {
					mimetype = getFormatConfiguration().match(FormatConfiguration.MP3, null, null);
				}
			}
			return mimetype;
		}
		if (mimetype != null && mimetype.equals(HTTPResource.VIDEO_TRANSCODE)) {
			mimetype = HTTPResource.MPEG_TYPEMIME;
			if (isTranscodeToWMV()) {
				mimetype = HTTPResource.WMV_TYPEMIME;
			}
		} else if (mimetype != null && mimetype.equals(HTTPResource.AUDIO_TRANSCODE)) {
			mimetype = HTTPResource.AUDIO_LPCM_TYPEMIME;
			if (mimetype != null) {
				if (isTranscodeAudioTo441()) {
					mimetype += ";rate=44100;channels=2";
				} else {
					mimetype += ";rate=48000;channels=2";
				}
			}
			if (isTranscodeToMP3()) {
				mimetype = HTTPResource.AUDIO_MP3_TYPEMIME;
			}
			if (isTranscodeToWAV()) {
				mimetype = HTTPResource.AUDIO_WAV_TYPEMIME;
			}
		}
		if (mimes.containsKey(mimetype)) {
			return mimes.get(mimetype);
		}
		return mimetype;
	}

	public boolean matchUserAgent(String userAgent) {
		return userAgentPattern.matcher(userAgent).find();
	}

	public boolean matchAdditionalUserAgent(String userAgent) {
		return userAgentAddtionalPattern.matcher(userAgent).find();
	}

	public String getUserAgent() {
		return getString(USER_AGENT, "^DefaultUserAgent");
	}

	public String getRendererName() {
		return getString(RENDERER_NAME, Messages.getString("PMS.17"));
	}

	public String getRendererNameWithAddress() {
		String s = getString(RENDERER_NAME, Messages.getString("PMS.17"));
		if (currentRendererAddress != null) {
			s = s + " [" + currentRendererAddress.getHostAddress() + "]";
		}
		if (speedInMbits > 0) {
			s = "<html><p align=center>" + s + "<br><b>Speed: " + speedInMbits + " Mb/s</b></p></html>";
		}
		return s;
	}

	public String getRendererIcon() {
		return getString(RENDERER_ICON, "unknown.png");
	}

	public String getUserAgentAdditionalHttpHeader() {
		return getString(USER_AGENT_ADDITIONAL_HEADER, null);
	}

	public String getUserAgentAdditionalHttpHeaderSearch() {
		return getString(USER_AGENT_ADDITIONAL_SEARCH, "DefaultUserAgent");
	}

	public String getUseSameExtension(String file) {
		String s = getString(USE_SAME_EXTENSION, null);
		if (s != null) {
			s = file + "." + s;
		} else {
			s = file;
		}
		return s;
	}

	public boolean isSeekByTime() {
		return getBoolean(SEEK_BY_TIME, true);
	}

	public boolean isMuxH264MpegTS() {
		boolean muxCompatible = getBoolean(MUX_H264_WITH_MPEGTS, true);
		if (isMediaParserV2()) {
			muxCompatible = getFormatConfiguration().match(FormatConfiguration.MPEGTS, FormatConfiguration.H264, null) != null;
		}
		if (Platform.isMac() && System.getProperty("os.version") != null && System.getProperty("os.version").contains("10.4.")) {
			muxCompatible = false; // no tsMuxeR for 10.4 (yet?)
		}
		return muxCompatible;
	}

	public boolean isDTSPlayable() {
		return isMuxDTSToMpeg() || (isWrapDTSIntoPCM() && isMuxLPCMToMpeg());
	}

	public boolean isMuxDTSToMpeg() {
		if (isMediaParserV2()) {
			return getFormatConfiguration().isDTSSupported();
		}
		return getBoolean(MUX_DTS_TO_MPEG, false);
	}

	public boolean isWrapDTSIntoPCM() {
		return getBoolean(WRAP_DTS_INTO_PCM, true);
	}

	public boolean isMuxLPCMToMpeg() {
		if (isMediaParserV2()) {
			return getFormatConfiguration().isLPCMSupported();
		}
		return getBoolean(MUX_LPCM_TO_MPEG, true);
	}

	public boolean isMpeg2Supported() {
		if (isMediaParserV2()) {
			return getFormatConfiguration().isMpeg2Supported();
		}
		return isPS3();
	}

	public String getVideoTranscode() {
		return getString(TRANSCODE_VIDEO, MPEGPSAC3);
	}

	public String getAudioTranscode() {
		return getString(TRANSCODE_AUDIO, LPCM);
	}

	public boolean isDefaultVBVSize() {
		return getBoolean(DEFAULT_VBV_BUFSIZE, false);
	}

	public String getMaxVideoBitrate() {
		return getString(MAX_VIDEO_BITRATE, null);
	}

	public String getCustomMencoderQualitySettings() {
		return getString(CUSTOM_MENCODER_QUALITYSETTINGS, null);
	}

	public int getMaxVideoWidth() {
		return getInt(MAX_VIDEO_WIDTH, 0);
	}

	public int getMaxVideoHeight() {
		return getInt(MAX_VIDEO_HEIGHT, 0);
	}

	public boolean isVideoRescale() {
		return getMaxVideoWidth() > 0 && getMaxVideoHeight() > 0;
	}

	public boolean isDLNAOrgPNUsed() {
		return getBoolean(DLNA_ORGPN_USE, true);
	}

	public String getTranscodedExtensions() {
		return getString(TRANSCODE_EXT, "");
	}

	public String getStreamedExtensions() {
		return getString(STREAM_EXT, "");
	}

	public long getTranscodedSize() {
		return getLong(TRANSCODED_SIZE, 0);
	}

	private int getInt(String key, int def) {
		try {
			return configuration.getInt(key, def);
		} catch (ConversionException e) {
			return def;
		}
	}

	private long getLong(String key, int def) {
		try {
			return configuration.getLong(key, def);
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

	public String toString() {
		return getRendererName();
	}

	public boolean isMediaParserV2() {
		return getBoolean(MEDIAPARSERV2, false) && MediaInfoParser.isValid();
	}

	public boolean isMediaParserV2ThumbnailGeneration() {
		return getBoolean(MEDIAPARSERV2_THUMB, false) && MediaInfoParser.isValid();
	}

	public boolean isDLNATreeHack() {
		
		return getBoolean(DLNA_TREE_HACK, false) && MediaInfoParser.isValid();
	}
}
