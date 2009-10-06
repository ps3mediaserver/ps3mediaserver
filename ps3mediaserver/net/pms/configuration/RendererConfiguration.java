package net.pms.configuration;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import net.pms.PMS;
import net.pms.io.OutputParams;
import net.pms.io.ProcessWrapperImpl;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;

import com.sun.jna.Platform;

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
	
	private InetAddress currentRendererAddress;
	private int speedInMbits;
	
	public int getSpeedInMbits() {
		return speedInMbits;
	}

	public InetAddress getCurrentRendererAddress() {
		return currentRendererAddress;
	}

	public void associateIP(InetAddress sa) {
		currentRendererAddress = sa;
		PMS.minimal("Renderer " + this + " found on this address: " + sa);
		
		// let's get that speed
		OutputParams op = new OutputParams(null);
		op.log = true;
		op.maxBufferSize = 1;
		String count = Platform.isWindows()?"-n":"-c";
		String size= Platform.isWindows()?"-l":"-s";
		final ProcessWrapperImpl pw = new ProcessWrapperImpl(new String[] {"ping", count, "3", size, "64000", sa.getHostAddress()}, op);
		Runnable r = new Runnable() {
			public void run() {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {}
				pw.stopProcess();
			}
		};
		Thread failsafe = new Thread(r);
		failsafe.start();
		pw.run();
		List<String> ls = pw.getOtherResults();
		int time = 0;
		int c = 0;
		for(String line:ls) {
			int msPos = line.indexOf(" ms");
			try {
				if (msPos > -1) {
					String timeString = line.substring(line.lastIndexOf("=", msPos)+1, msPos).trim();
					time += Double.parseDouble(timeString);
					c++;
				}
			} catch (Exception e) {
				// no big deal
			}
		}
		if (c > 0)
			time = (int) (time / c);
		if (time > 0) {
			speedInMbits = (int) (1024 / time);
			PMS.minimal("Renderer " + this + " have an estimated network speed of: " + speedInMbits + " Mb/s");
		}
	}
	
	public static void main(String args[]) throws Exception{
		new RendererConfiguration().associateIP(InetAddress.getByName("192.168.0.10"));
	}
	
	public static RendererConfiguration getRendererConfigurationBySocketAddress(InetAddress sa) {
		for(RendererConfiguration r:renderersConfs) {
			if (sa.equals(r.currentRendererAddress))
				return r;
		}
		return null;
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
	private Map<String, String> DLNAPN;
	
	public int getRank() {
		return rank;
	}
	
	public boolean isXBOX() {
		return getRendererName().toUpperCase().contains("XBOX");
	}
	
	public boolean isXBMC() {
		return getRendererName().toUpperCase().contains("XBMC");
	}
	
	public boolean isPS3() {
		return getRendererName().toUpperCase().contains("PLAYSTATION") || getRendererName().toUpperCase().contains("PS3");
	}
	
	private static final String RENDERER_NAME="RendererName";
	private static final String RENDERER_ICON="RendererIcon";
	private static final String USER_AGENT="UserAgentSearch";
	private static final String USER_AGENT_ADDITIONAL_HEADER="UserAgentAdditionalHeader";
	private static final String USER_AGENT_ADDITIONAL_SEARCH="UserAgentAdditionalHeaderSearch";
	
	private static final String VIDEO="Video";
	private static final String AUDIO="Audio";
	private static final String IMAGE="Image";
	
	
	private static final String SEEK_BY_TIME="SeekByTime";
	
	public static final String MPEGPSAC3 = "MPEGAC3";
	public static final String MPEGTSAC3 = "MPEGTSAC3";
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
	private static final String MAX_VIDEO_BITRATE="MaxVideoBitrateMbps";
	private static final String MAX_VIDEO_WIDTH="MaxVideoWidth";
	private static final String MAX_VIDEO_HEIGHT="MaxVideoHeight";
	private static final String USE_SAME_EXTENSION="UseSameExtension";
	private static final String MIME_TYPES_CHANGES="MimeTypesChanges";
	private static final String TRANSCODE_EXT="TranscodeExtensions";
	private static final String STREAM_EXT="StreamExtensions";
	private static final String H264_L41_LIMITED="H264Level41Limited";
	private static final String TRANSCODE_AUDIO_441KHZ="TranscodeAudioTo441kHz";
	private static final String TRANSCODED_SIZE="TranscodedVideoFileSize";
	private static final String DLNA_PN_CHANGES="DLNAProfileChanges";
	
	
	private RendererConfiguration() throws ConfigurationException {
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
				if (equals > -1) {
					String old = mime_change.substring(0, equals).trim().toLowerCase();
					String nw = mime_change.substring(equals+1).trim().toLowerCase();
					mimes.put(old, nw);
				}
			}
		}
		DLNAPN = new HashMap<String, String>();
		String DLNAPNchanges = configuration.getString(DLNA_PN_CHANGES, null);
		PMS.minimal("Config: " + DLNAPNchanges);
		if (StringUtils.isNotBlank(DLNAPNchanges)) {
			StringTokenizer st = new StringTokenizer(DLNAPNchanges, "|");
			while (st.hasMoreTokens()) {
				String DLNAPN_change = st.nextToken().trim();
				int equals = DLNAPN_change.indexOf("=");
				if (equals > -1) {
					String old = DLNAPN_change.substring(0, equals).trim().toUpperCase();
					String nw = DLNAPN_change.substring(equals+1).trim().toUpperCase();
					DLNAPN.put(old, nw);
				}
			}
		}
	}
	
	public String getDLNAPN(String old) {
		if (DLNAPN.containsKey(old)) {
			return DLNAPN.get(old);
		}
		return old;
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
	
	public boolean isTranscodeToMP3() {
		return getAudioTranscode().startsWith(MP3);
	}
	
	public boolean isTranscodeToPCM() {
		return getAudioTranscode().startsWith(PCM);
	}
	
	public boolean isTranscodeAudioTo441() {
		return getBoolean(TRANSCODE_AUDIO_441KHZ, false);
	}
	
	public boolean isH264Level41Limited() {
		return getBoolean(H264_L41_LIMITED, false);
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
	
	public String getRendererNameWithAddress() {
		String s = getString(RENDERER_NAME, "Unknown Renderer");
		if (currentRendererAddress != null)
			s = s + " [" + currentRendererAddress.getHostAddress() + "]";
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
		if (s != null)
			s = file + "." + s;
		else
			s = file;
		return s;
	}
	
	public boolean isSeekByTime() {
		return getBoolean(SEEK_BY_TIME, true);
	}
	
	public boolean isMuxH264MpegTS() {
		boolean muxCompatible = getBoolean(MUX_H264_WITH_MPEGTS, true);
		if (Platform.isMac() && System.getProperty("os.version") != null && System.getProperty("os.version").contains("10.4."))
			muxCompatible = false; // no tsMuxer for 10.4 (yet?)
		return muxCompatible;
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
		return getString(TRANSCODE_VIDEO, MPEGPSAC3);
	}
	
	public String getAudioTranscode() {
		return getString(TRANSCODE_AUDIO, PCM);
	}
	
	public boolean isDefaultVBVSize() {
		return getBoolean(DEFAULT_VBV_BUFSIZE, false);
	}
	
	public String getMaxVideoBitrate() {
		return getString(MAX_VIDEO_BITRATE, null);
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

}
