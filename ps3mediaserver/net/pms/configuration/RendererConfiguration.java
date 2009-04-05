package net.pms.configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import net.pms.PMS;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;

public class RendererConfiguration {
	
	/*
	 * Static section
	 */
	
	private static ArrayList<RendererConfiguration> renderersConfs;
	private static RendererConfiguration defaultConf;
	public static RendererConfiguration getDefaultConf() {
		return defaultConf;
	}

	public static void loadRendererConfigurations()  {
		renderersConfs = new ArrayList<RendererConfiguration>();
		try {
			defaultConf = new RendererConfiguration();
		} catch (ConfigurationException e) {}
		File renderersDir = new File("renderers");
		if (renderersDir.exists()) {
			File confs [] = renderersDir.listFiles();
			int rank = 1;
			for(File f:confs) {
				if (f.getName().endsWith(".conf")) {
					try {
						PMS.minimal("Loading configuration file: " + f.getName());
						RendererConfiguration r = new RendererConfiguration(f);
						r.rank = rank++;
						renderersConfs.add(r);
					} catch (ConfigurationException ce) {
						PMS.minimal("Error in loading configuration of: " + f.getAbsolutePath());
					}
					
				}
			}
		}
	}
	
	public static RendererConfiguration getRendererConfiguration(int mediarenderer) {
		for(RendererConfiguration r:renderersConfs) {
			if (r.rank == mediarenderer)
				return r;
		}
		return defaultConf;
	}
	
	public static RendererConfiguration getRendererConfigurationByUA(String userAgentString) {
		for(RendererConfiguration r:renderersConfs) {
			if (r.matchUserAgent(userAgentString))
				return r;
		}
		return null;
	}
	
	public static RendererConfiguration getRendererConfigurationByUAAHH(String header) {
		for(RendererConfiguration r:renderersConfs) {
			if (StringUtils.isNotBlank(r.getUserAgentAdditionalHttpHeader()) && header.startsWith(r.getUserAgentAdditionalHttpHeader())) {
				String value = header.substring(header.indexOf(":", r.getUserAgentAdditionalHttpHeader().length())+1);
				if (r.matchAdditionalUserAgent(value))
					return r;
			}
		}
		return null;
	}
	
	/*
	 * 
	 */
	
	private PropertiesConfiguration configuration;
	private int rank;
	private Map<String, String> mimes;
	
	public int getRank() {
		return rank;
	}
	
	public boolean isXBOX() {
		return getRendererName().toUpperCase().contains("XBOX");
	}
	
	public boolean isPS3() {
		return getRendererName().toUpperCase().contains("PLAYSTATION") || getRendererName().toUpperCase().contains("PS3");
	}
	
	private static final String RENDERER_NAME="RendererName";
	private static final String RENDERER_ICON="RendererIcon";
	private static final String USER_AGENT="UserAgentSearch";
	private static final String USER_AGENT_ADDITIONAL_HEADER="UserAgentAdditionalHeader";
	private static final String USER_AGENT_ADDITIONAL_SEARCH="UserAgentAdditionalHeaderSearch";
	
	private static final String SEEK_BY_TIME="SeekByTime";
	
	public static final String MPEGAC3 = "MPEGAC3";
	public static final String WMV = "WMV";
	
	public static final String PCM = "PCM";
	public static final String MP3 = "MP3";
	
	private static final String TRANSCODE_AUDIO="TranscodeAudio";
	private static final String TRANSCODE_VIDEO="TranscodeVideo";
	private static final String DEFAULT_VBV_BUFSIZE="DefaultVBVBufSize";
	private static final String MUX_H264_WITH_MPEGTS="MuxH264ToMpegTS";
	private static final String MUX_DTS_TO_MPEG="MuxDTSToMpeg";
	private static final String WRAP_DTS_INTO_PCM="WrapDTSIntoPCM";
	private static final String MUX_LPCM_TO_MPEG="MuxLPCMToMpeg";
	private static final String MAX_BITRATE="MaxBitrateMbps";
	
	private static final String MIME_TYPES_CHANGES="MimeTypesChanges";
	private static final String TRANSCODE_EXT="TranscodeExtensions";
	private static final String STREAM_EXT="StreamExtensions";
	
	
	public RendererConfiguration() throws ConfigurationException {
		this(null);
	}
	
	private Pattern userAgentPattern = null;
	private Pattern userAgentAddtionalPattern = null;
	
	public RendererConfiguration(File f) throws ConfigurationException {
		configuration = new PropertiesConfiguration();
		configuration.setListDelimiter((char)0);
		if (f != null)
			configuration.load(f);
		userAgentPattern = Pattern.compile(getUserAgent(), Pattern.CASE_INSENSITIVE);
		userAgentAddtionalPattern = Pattern.compile(getUserAgentAdditionalHttpHeaderSearch(), Pattern.CASE_INSENSITIVE);
		mimes = new HashMap<String, String>();
		String mimeTypes = configuration.getString(MIME_TYPES_CHANGES, null);
		if (StringUtils.isNotBlank(mimeTypes)) {
			StringTokenizer st = new StringTokenizer(mimeTypes, "|");
			while (st.hasMoreTokens()) {
				String mime_change = st.nextToken().trim();
				int equals = mime_change.indexOf("=");
				if (equals > 0) {
					String old = mime_change.substring(0, equals);
					String nw = mime_change.substring(equals+1);
					mimes.put(old, nw);
				}
			}
		}
	}
	
	public boolean isTranscodeToWMV() {
		return getVideoTranscode().startsWith(WMV);
	}
	
	public boolean isTranscodeToMPEGAC3() {
		return getVideoTranscode().startsWith(MPEGAC3);
	}
	
	public boolean isTranscodeToMP3() {
		return getAudioTranscode().startsWith(MP3);
	}
	
	public boolean isTranscodeToPCM() {
		return getAudioTranscode().startsWith(PCM);
	}
	
	public String getMimeType(String old) {
		if (mimes.containsKey(old)) {
			return mimes.get(old);
		}
		return old;
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
		return getString(RENDERER_NAME, "Unknown Renderer");
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
	
	public boolean isSeekByTime() {
		return getBoolean(SEEK_BY_TIME, true);
	}
	
	public boolean isMuxH264MpegTS() {
		return getBoolean(MUX_H264_WITH_MPEGTS, true);
	}
	
	public boolean isDTSPlayable() {
		return isMuxDTSToMpeg() || (isWrapDTSIntoPCM() && isMuxLPCMToMpeg());
	}
	
	public boolean isMuxDTSToMpeg() {
		return getBoolean(MUX_DTS_TO_MPEG, false);
	}
	
	public boolean isWrapDTSIntoPCM() {
		return getBoolean(WRAP_DTS_INTO_PCM, true);
	}
	
	public boolean isMuxLPCMToMpeg() {
		return getBoolean(MUX_LPCM_TO_MPEG, true);
	}
	
	public String getVideoTranscode() {
		return getString(TRANSCODE_VIDEO, MPEGAC3);
	}
	
	public String getAudioTranscode() {
		return getString(TRANSCODE_AUDIO, PCM);
	}
	
	public boolean isDefaultVBVSize() {
		return getBoolean(DEFAULT_VBV_BUFSIZE, false);
	}
	
	public int getMaxBitrate() {
		return getInt(MAX_BITRATE, 0);
	}
	
	public String getTranscodedExtensions() {
		return getString(TRANSCODE_EXT, "");
	}
	
	public String getStreamedExtensions() {
		return getString(STREAM_EXT, "");
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

	public String toString() {
		return getRendererName();
	}

}
