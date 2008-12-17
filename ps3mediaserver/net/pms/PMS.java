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

package net.pms;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.BindException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.UUID;

import net.pms.dlna.AudiosFeed;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.ImagesFeed;
import net.pms.dlna.RootFolder;
import net.pms.dlna.VideosFeed;
import net.pms.dlna.WebAudioStream;
import net.pms.dlna.WebVideoStream;
import net.pms.dlna.virtual.AVSyncAction;
import net.pms.dlna.virtual.AutoSubLoadingAction;
import net.pms.dlna.virtual.SkipLoopAction;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.encoders.FFMpegAudio;
import net.pms.encoders.FFMpegVideo;

import net.pms.encoders.FFMpegVideoRemux;
import net.pms.encoders.MEncoderAviSynth;
import net.pms.encoders.MEncoderVideo;
import net.pms.encoders.MEncoderWebVideo;
import net.pms.encoders.MPlayerAudio;
import net.pms.encoders.MPlayerWebAudio;
import net.pms.encoders.MPlayerWebVideoDump;
import net.pms.encoders.Player;
import net.pms.encoders.TSMuxerVideo;
import net.pms.encoders.TsMuxerAudio;
import net.pms.encoders.VideoLanAudioStreaming;
import net.pms.encoders.VideoLanVideoStreaming;
import net.pms.formats.DVRMS;
import net.pms.formats.FLAC;
import net.pms.formats.Format;
import net.pms.formats.GIF;
import net.pms.formats.ISO;
import net.pms.formats.JPG;
import net.pms.formats.M4A;
import net.pms.formats.MKV;
import net.pms.formats.MP3;
import net.pms.formats.MPG;
import net.pms.formats.OGG;
import net.pms.formats.PNG;
import net.pms.formats.TIF;
import net.pms.formats.WEB;
import net.pms.gui.DummyFrame;
import net.pms.gui.IFrame;
import net.pms.io.CacheManager;
import net.pms.io.OutputTextConsumer;
import net.pms.io.WinUtils;
import net.pms.network.HTTPServer;
import net.pms.network.ProxyServer;
import net.pms.network.UPNPHelper;

public class PMS {
	
	public static final String VERSION = "1.01";
	public static final String AVS_SEPARATOR = "\1";
	
	
	public String getMencoder_ass_scale() {
		return mencoder_ass_scale;
	}

	public void setMencoder_ass_scale(String mencoder_ass_scale) {
		this.mencoder_ass_scale = mencoder_ass_scale;
	}

	public String getMencoder_ass_margin() {
		return mencoder_ass_margin;
	}

	public void setMencoder_ass_margin(String mencoder_ass_margin) {
		this.mencoder_ass_margin = mencoder_ass_margin;
	}

	public String getMencoder_ass_outline() {
		return mencoder_ass_outline;
	}

	public void setMencoder_ass_outline(String mencoder_ass_outline) {
		this.mencoder_ass_outline = mencoder_ass_outline;
	}

	public String getMencoder_ass_shadow() {
		return mencoder_ass_shadow;
	}

	public void setMencoder_ass_shadow(String mencoder_ass_shadow) {
		this.mencoder_ass_shadow = mencoder_ass_shadow;
	}

	public String getMencoder_noass_scale() {
		return mencoder_noass_scale;
	}

	public void setMencoder_noass_scale(String mencoder_noass_scale) {
		this.mencoder_noass_scale = mencoder_noass_scale;
	}

	public String getMencoder_noass_subpos() {
		return mencoder_noass_subpos;
	}

	public void setMencoder_noass_subpos(String mencoder_noass_subpos) {
		this.mencoder_noass_subpos = mencoder_noass_subpos;
	}

	public String getMencoder_noass_blur() {
		return mencoder_noass_blur;
	}

	public void setMencoder_noass_blur(String mencoder_noass_blur) {
		this.mencoder_noass_blur = mencoder_noass_blur;
	}

	public String getMencoder_noass_outline() {
		return mencoder_noass_outline;
	}

	public void setMencoder_noass_outline(String mencoder_noass_outline) {
		this.mencoder_noass_outline = mencoder_noass_outline;
	}

	
	public void setPort(int port) {
		this.port = port;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public void setMencoder_main(String mencoder_main) {
		this.mencoder_main = mencoder_main;
	}

	/*public void setMencoder_style(String mencoder_style) {
		this.mencoder_style = mencoder_style;
	}*/

	public void setMencoder_decode(String mencoder_decode) {
		this.mencoder_decode = mencoder_decode;
	}

	private String mencoder_audiolangs;

	public String getMencoder_audiolangs() {
		return mencoder_audiolangs;
	}

	public void setMencoder_audiolangs(String mencoder_audiolangs) {
		this.mencoder_audiolangs = mencoder_audiolangs;
	}
	
	private String mencoder_sublangs;

	public String getMencoder_sublangs() {
		return mencoder_sublangs;
	}

	public void setMencoder_sublangs(String mencoder_sublangs) {
		this.mencoder_sublangs = mencoder_sublangs;
	}
	
	private String mencoder_subcp;

	public String getMencoder_subcp() {
		return mencoder_subcp;
	}

	public void setMencoder_subcp(String mencoder_subcp) {
		this.mencoder_subcp = mencoder_subcp;
	}
	
	private boolean mencoder_subfribidi;
	
	public boolean isMencoder_subfribidi() {
		return mencoder_subfribidi;
	}

	public void setMencoder_subfribidi(boolean mencoder_subfribidi) {
		this.mencoder_subfribidi = mencoder_subfribidi;
	}

	private boolean mencoder_ass = true;

	public boolean isMencoder_ass() {
		return mencoder_ass;
	}

	public void setMencoder_ass(boolean mencoder_ass) {
		this.mencoder_ass = mencoder_ass;
	}
	
	private boolean mencoder_disablesubs;

	public boolean isMencoder_disablesubs() {
		return mencoder_disablesubs;
	}

	public void setMencoder_disablesubs(boolean mencoder_disablesubs) {
		this.mencoder_disablesubs = mencoder_disablesubs;
	}

	public String getCharsetencoding() {
		return charsetencoding;
	}

	public void setCharsetencoding(String charsetencoding) {
		this.charsetencoding = charsetencoding;
	}

	public String getFolders() {
		return folders;
	}

	public void setFolders(String folders) {
		this.folders = folders;
	}

	public void setMaxMemoryBufferSize(int maxMemoryBufferSize) {
		this.maxMemoryBufferSize = maxMemoryBufferSize;
	}

	public void setTurbomode(boolean turbomode) {
		this.turbomode = turbomode;
	}

	public void setAudiochannels(int audiochannels) {
		this.audiochannels = audiochannels;
	}

	public void setAudiobitrate(int audiobitrate) {
		this.audiobitrate = audiobitrate;
	}

	public void setThumbnails(boolean thumbnails) {
		this.thumbnails = thumbnails;
	}

	private int thumbnail_seek_pos = 1;

	public int getThumbnail_seek_pos() {
		return thumbnail_seek_pos;
	}

	public void setThumbnail_seek_pos(int thumbnail_seek_pos) {
		this.thumbnail_seek_pos = thumbnail_seek_pos;
	}

	public IFrame getFrame() {
		return frame;
	}

	public int getMaxaudiobuffer() {
		return maxaudiobuffer;
	}

	public int getMinstreambuffer() {
		return minstreambuffer;
	}

	public RootFolder getRootFolder() {
		return rootFolder;
	}

	private static PMS instance = null;
	private static byte[] lock = null;
	static {
		lock = new byte[0];
		sdfHour = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);
		sdfDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
	}
	
	private boolean ps3found;
	public boolean isPs3found() {
		return ps3found;
	}

	public void setPs3found(boolean ps3found) {
		this.ps3found = ps3found;
		if (ps3found) {
			frame.setStatusCode(0, "PS3 has been found !", "PS3_2.png");
		}
	}
	
	private boolean tsmuxer_preremux_ac3;

	public boolean isTsmuxer_preremux_ac3() {
		return tsmuxer_preremux_ac3;
	}

	public void setTsmuxer_preremux_ac3(boolean tsmuxer_preremux_ac3) {
		this.tsmuxer_preremux_ac3 = tsmuxer_preremux_ac3;
	}

	private boolean tsmuxer_preremux_pcm;
	
	public boolean isTsmuxer_preremux_pcm() {
		return tsmuxer_preremux_pcm;
	}

	public void setTsmuxer_preremux_pcm(boolean tsmuxer_preremux_pcm) {
		this.tsmuxer_preremux_pcm = tsmuxer_preremux_pcm;
	}

	private boolean mencoder_usepcm;
	
	public boolean isMencoder_usepcm() {
		return mencoder_usepcm;
	}

	public void setMencoder_usepcm(boolean mencoder_usepcm) {
		this.mencoder_usepcm = mencoder_usepcm;
	}

	private RootFolder rootFolder;
	private HTTPServer server;
	private String serverName;
	private int maxMemoryBufferSize;
	private int minMemoryBufferSize;
	private File tempFolder;
	private ArrayList<Format> extensions;
	private ArrayList<Player> players;
	private ArrayList<Player> allPlayers;
	public ArrayList<Player> getAllPlayers() {
		return allPlayers;
	}
	
	private boolean usecache;

	public boolean isUsecache() {
		return usecache;
	}

	public void setUsecache(boolean usecache) {
		this.usecache = usecache;
	}

	private boolean tsmuxer_forcefps;
	
	public boolean isTsmuxer_forcefps() {
		return tsmuxer_forcefps;
	}

	public void setTsmuxer_forcefps(boolean tsmuxer_forcefps) {
		this.tsmuxer_forcefps = tsmuxer_forcefps;
	}
	
	private boolean mencoder_fontconfig;
	
	public boolean isMencoder_fontconfig() {
		return mencoder_fontconfig;
	}

	public void setMencoder_fontconfig(boolean mencoder_fontconfig) {
		this.mencoder_fontconfig = mencoder_fontconfig;
	}

	private boolean mencoder_forcefps;

	public boolean isMencoder_forcefps() {
		return mencoder_forcefps;
	}

	public void setMencoder_forcefps(boolean mencoder_forcefps) {
		this.mencoder_forcefps = mencoder_forcefps;
	}

	private String maximumbitrate;
	public String getMaximumbitrate() {
		return maximumbitrate;
	}

	public void setMaximumbitrate(String maximumbitrate) {
		this.maximumbitrate = maximumbitrate;
	}

	private boolean mencoder_intelligent_sync = true;
	public boolean isMencoder_intelligent_sync() {
		return mencoder_intelligent_sync;
	}

	public void setMencoder_intelligent_sync(boolean mencoder_intelligent_sync) {
		this.mencoder_intelligent_sync = mencoder_intelligent_sync;
	}

	private ProxyServer proxyServer;
	private boolean turbomode;
	private String charsetencoding = "ISO-8859-1";
	
	public String getEncoding() {
		return charsetencoding;
	}

	public boolean isTurbomode() {
		return turbomode;
	}

	public ProxyServer getProxy() {
		return proxyServer;
	}

	public static SimpleDateFormat sdfDate;
	public static SimpleDateFormat sdfHour;
	
	public static final int DEBUG = 0;
	public static final int INFO = 1;
	public static final int MINIMAL = 2;
	
	private boolean transcode_block_multiple_connections;
	
	public boolean isTranscode_block_multiple_connections() {
		return transcode_block_multiple_connections;
	}

	public void setTranscode_block_multiple_connections(
			boolean transcode_block_multiple_connections) {
		this.transcode_block_multiple_connections = transcode_block_multiple_connections;
	}

	int level = 2;
	public void setLevel(int level) {
		this.level = level;
	}

	public int getLevel() {
		return level;
	}

	private PrintWriter pw;
	
	public ArrayList<Process> currentProcesses = new ArrayList<Process>();
	
	private PMS() {
	}
	
	IFrame frame;
	
	private void initGFX() {
		
		//String frameClass = "net.pms.gui.SwingFrame";
		//String frameClass = "net.pms.gui.SWTFrame";
		String frameClass = "net.pms.newgui.LooksFrame";
		if (System.getProperty("frameclass") != null)
			frameClass = System.getProperty("frameclass").toString();
		try {
			frame = (IFrame) Class.forName(frameClass).newInstance();
		} catch (Exception e) {
			PMS.error(null, e);
		}
	
	}
	/*
	private String audioengines;
		
	public String getAudioengines() {
		return audioengines;
	}

	public void setAudioengines(String audioengines) {
		this.audioengines = audioengines;
	}
	
	public String webvideoengines;

	public String getWebvideoengines() {
		return webvideoengines;
	}

	public void setWebvideoengines(String webvideoengines) {
		this.webvideoengines = webvideoengines;
	}
*/
	private boolean hidevideosettings;
	
	public boolean isHidevideosettings() {
		return hidevideosettings;
	}

	public void setHidevideosettings(boolean hidevideosettings) {
		this.hidevideosettings = hidevideosettings;
	}

	private String alternativeffmpegPath;
	
	public String getAlternativeffmpegPath() {
		return alternativeffmpegPath;
	}

	public void setAlternativeffmpegPath(String alternativeffmpegPath) {
		this.alternativeffmpegPath = alternativeffmpegPath;
	}
	
	private String flacPath;

	public String getFlacPath() {
		return flacPath;
	}

	public void setFlacPath(String flacPath) {
		this.flacPath = flacPath;
	}

	private String ffmpegPath;
	public String getFFmpegPath() {
		return ffmpegPath;
	}
	
	private String mencoderPath;
	public String getMEncoderPath() {
		return mencoderPath;
	}
	
	private boolean skiploopfilter;
	
	public boolean isSkiploopfilter() {
		return skiploopfilter;
	}

	public void setSkipLoopFilter(boolean enable) {
		skiploopfilter = enable;
	}
	
	private String engines;
	public String getEngines() {
		return engines;
	}

	public void setEngines(String engines) {
		this.engines = engines;
	}

	private ArrayList<String> enginesAsList;
	/*private ArrayList<String> audioenginesAsList;
	public ArrayList<String> getAudioenginesAsList() {
		return audioenginesAsList;
	}

	public void setAudioenginesAsList(ArrayList<String> audioenginesAsList) {
		this.audioenginesAsList = audioenginesAsList;
	}

	private ArrayList<String> webaudioenginesAsList;
	private ArrayList<String> webvideoenginesAsList;

	public ArrayList<String> getWebvideoenginesAsList() {
		return webvideoenginesAsList;
	}

	public void setWebvideoenginesAsList(ArrayList<String> webvideoenginesAsList) {
		this.webvideoenginesAsList = webvideoenginesAsList;
	}

	public ArrayList<String> getWebaudioenginesAsList() {
		return webaudioenginesAsList;
	}

	public void setWebaudioenginesAsList(ArrayList<String> webenginesAsList) {
		this.webaudioenginesAsList = webenginesAsList;
	}*/

	public void setEnginesAsList(ArrayList<String> enginesAsList) {
		this.enginesAsList = enginesAsList;
	}

	public ArrayList<String> getEnginesAsList() {
		return enginesAsList;
	}
	
	private boolean minimized;
	
	public boolean isMinimized() {
		return minimized;
	}

	public void setMinimized(boolean minimized) {
		this.minimized = minimized;
	}

	private String mplayerPath;
	public String getMPlayerPath() {
		return mplayerPath;
	}
	
	private String mkfifoPath;
	public String getMKfifoPath() {
		return mkfifoPath;
	}
	
	private String tsmuxerPath;
	
	public String getTsmuxerPath() {
		return tsmuxerPath;
	}

	public void setTsmuxerPath(String tsmuxerPath) {
		this.tsmuxerPath = tsmuxerPath;
	}

	private String vlcPath;
	public String getVlcPath() {
		return vlcPath;
	}

	private int audiochannels;
	
	public int getAudiochannels() {
		return audiochannels;
	}
	
	private int audiobitrate;

	public int getAudiobitrate() {
		return audiobitrate;
	}

	private boolean windows;
	public boolean isWindows() {
		return windows;
	}
	
	boolean thumbnails;

	public boolean isThumbnails() {
		return thumbnails;
	}

	private boolean usesubs = true;
	public void setUsesubs(boolean usesubs) {
		this.usesubs = usesubs;
	}

	public boolean isUsesubs() {
		return usesubs;
	}
	
	private String mencoder_ass_scale;
	private String mencoder_ass_margin;
	private String mencoder_ass_outline;
	private String mencoder_ass_shadow;
	private String mencoder_noass_scale;
	private String mencoder_noass_subpos;
	private String mencoder_noass_blur;
	private String mencoder_noass_outline;

	private int maxaudiobuffer;
	private int minstreambuffer;
	private int port = 5001;
	public int getPort() {
		return port;
	}

	private int nbcores;
	
	public int getNbcores() {
		return nbcores;
	}

	public void setNbcores(int nbcores) {
		this.nbcores = nbcores;
	}

	private int proxy;
	private String ffmpeg = "-threads 2 -g 1 -qscale 1 -qmin 2";
	public void setFfmpeg(String ffmpeg) {
		this.ffmpeg = ffmpeg;
	}

	private String mplayer;
	private String mencoder_main = "";
	//private String mencoder_style = "";
	private String mencoder_decode ="" ;
	public String getMencoder_decode() {
		return mencoder_decode;
	}

	/*public String getMencoder_style() {
		return mencoder_style;
	}*/

	

	public String getMencoderMainSettings() {
		return mencoder_main;
	}

	public String getMplayerSettings() {
		return mplayer;
	}

	private String folders = "";
	private boolean filebuffer;
	
	String hostname;
	
	public boolean isForceMPlayer() {
		return false;
	}
	public String getHostname() {
		return hostname;
	}

	public boolean isFilebuffer() {
		return filebuffer;
	}

	public String getFfmpegSettings() {
		return ffmpeg;
	}
	
	private boolean mencoder_nooutofsync;
	
	public boolean isMencoder_nooutofsync() {
		return mencoder_nooutofsync;
	}

	public void setMencoder_nooutofsync(boolean mencoder_nooutofsync) {
		this.mencoder_nooutofsync = mencoder_nooutofsync;
	}

	private boolean avisynth_convertfps = true;
	
	public void setAvisynth_convertfps(boolean avisynth_convertfps) {
		this.avisynth_convertfps = avisynth_convertfps;
	}

	public boolean isAvisynth_convertfps() {
		return avisynth_convertfps;
	}
	
	private String avisynth_script;
	
	public String getAvisynth_script() {
		return avisynth_script;
	}

	public void setAvisynth_script(String avisynth_script) {
		this.avisynth_script = avisynth_script;
	}

	private boolean isTrue(String s) {
		return (s.toLowerCase().equals("yes") || s.toLowerCase().equals("ok") || s.toLowerCase().equals("y") || s.toLowerCase().equals("true"));
	}
	
	private void loadConf() throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(new File("PMS.conf")));
		
		//ArrayList<KeyValue> remaining = new ArrayList<KeyValue>();
		String line = null;
		while ( (line = br.readLine()) != null) {
			
			line = line.trim();
			if (!line.startsWith("#") && !line.startsWith(" ") && line.indexOf("=") > -1) {
			
				String key = line.substring(0, line.indexOf("="));
				String value = line.substring(line.indexOf("=")+1);
				if (key.equals("temp")) {
					if (value !=  null) {
						tempFolder = new File(value);
						if (tempFolder.exists() && tempFolder.isDirectory()) {
							debug("Setting temp folder to: " + tempFolder.getAbsolutePath());
						} else {
							minimal("Warning, folder " + tempFolder + " not valid");
							tempFolder = null;
						}
					}
				} else if (key.equals("vlc_path") && value.length() > 0) {
					vlcPath = value.trim();
				} else if (key.equals("port") && value.length() > 0) {
					port = Integer.parseInt(value.trim());
				} /*else if (key.equals("expert") && value.length() > 0) {
					expert = true;
				} */else if (key.equals("hostname") && value.length() > 0) {
					hostname = value.trim();
				} else if (key.equals("proxy") && value.length() > 0) {
					proxy = Integer.parseInt(value.trim());
				} else if (key.equals("minvideobuffer") && value.length() > 0) {
					minMemoryBufferSize = Integer.parseInt(value.trim());
				} else if (key.equals("maxvideobuffer") && value.length() > 0) {
					maxMemoryBufferSize = Integer.parseInt(value.trim());
				} else if (key.equals("thumbnail_seek_pos") && value.length() > 0) {
					thumbnail_seek_pos = Integer.parseInt(value.trim());
				} else if (key.equals("minwebbuffer") && value.length() > 0) {
					minstreambuffer = Integer.parseInt(value.trim());
				} else if (key.equals("maxaudiobuffer") && value.length() > 0) {
					maxaudiobuffer = Integer.parseInt(value.trim());
				} else if (key.equals("audiochannels") && value.length() > 0) {
					audiochannels = Integer.parseInt(value.trim());
				} else if (key.equals("audiobitrate") && value.length() > 0) {
					audiobitrate = Integer.parseInt(value.trim());
				} else if (key.equals("maximumbitrate") && value.length() > 0) {
					maximumbitrate = value.trim();
				} else if (key.equals("level") && value.length() > 0) {
					level = Integer.parseInt(value.trim());
				} else if (key.equals("nbcores") && value.length() > 0) {
					nbcores = Integer.parseInt(value.trim());
				} else if (key.equals("charsetencoding") && value.length() > 0) {
					charsetencoding = value.trim();
				} else if (key.equals("ffmpeg") && value.length() > 0) {
					ffmpeg = value.trim();
				} else if (key.equals("mplayer") && value.length() > 0) {
					mplayer = value.trim();
				} else if (key.equals("skiploopfilter") && value.length() > 0) {
					skiploopfilter = isTrue(value.trim());
				} else if (key.equals("mencoder_nooutofsync") && value.length() > 0) {
					mencoder_nooutofsync = isTrue(value.trim());
				} else if (key.equals("minimized") && value.length() > 0) {
					minimized = isTrue(value.trim());
				} else if (key.equals("thumbnails") && value.length() > 0) {
					thumbnails = isTrue(value.trim());
				} else if (key.equals("mencoder_forcefps") && value.length() > 0) {
					mencoder_forcefps = isTrue(value.trim());
				} else if (key.equals("mencoder_fontconfig") && value.length() > 0) {
					mencoder_fontconfig = isTrue(value.trim());
				} else if (key.equals("mencoder_subfribidi") && value.length() > 0) {
					mencoder_subfribidi = isTrue(value.trim());
				} else if (key.equals("hidevideosettings") && value.length() > 0) {
					hidevideosettings = isTrue(value.trim());
				} else if (key.equals("mencoder_intelligent_sync") && value.length() > 0) {
					mencoder_intelligent_sync = isTrue(value.trim());
				} else if (key.equals("usecache") && value.length() > 0) {
					usecache = isTrue(value.trim());
				} else if (key.equals("mencoder_ass_margin") && value.length() > 0) {
					mencoder_ass_margin= value.trim();
				} else if (key.equals("mencoder_ass_outline") && value.length() > 0) {
					mencoder_ass_outline= value.trim();
				} else if (key.equals("mencoder_ass_scale") && value.length() > 0) {
					mencoder_ass_scale= value.trim();
				} else if (key.equals("mencoder_ass_shadow") && value.length() > 0) {
					mencoder_ass_shadow= value.trim();
				} else if (key.equals("mencoder_noass_scale") && value.length() > 0) {
					mencoder_noass_scale= value.trim();
				} else if (key.equals("mencoder_noass_subpos") && value.length() > 0) {
					mencoder_noass_subpos= value.trim();
				} else if (key.equals("mencoder_noass_blur") && value.length() > 0) {
					mencoder_noass_blur= value.trim();
				} else if (key.equals("mencoder_noass_outline") && value.length() > 0) {
					mencoder_noass_outline= value.trim();
				} else if (key.equals("mencoder_encode") && value.length() > 0) {
					mencoder_main= value.trim();
				} else if (key.equals("mencoder_decode") && value.length() > 0) {
					mencoder_decode= value.trim();
				} /*else if (key.equals("mencoder_substyle") && value.length() > 0) {
					mencoder_style= value.trim();
				}*/ else if (key.equals("mencoder_audiolangs") && value.length() > 0) {
					mencoder_audiolangs = value.trim();
				} else if (key.equals("mencoder_sublangs") && value.length() > 0) {
					mencoder_sublangs = value.trim();
				} else if (key.equals("mencoder_subcp") && value.length() > 0) {
					mencoder_subcp = value.trim();
				} else if (key.equals("autoloadsrt") && value.length() > 0) {
					usesubs = isTrue(value.trim());
				} else if (key.equals("mencoder_ass") && value.length() > 0) {
					mencoder_ass = isTrue(value.trim());
				} else if (key.equals("mencoder_disablesubs") && value.length() > 0) {
					mencoder_disablesubs = isTrue(value.trim());
				} else if (key.equals("turbo") && value.length() > 0) {
					turbomode = isTrue(value.trim());
				} else if (key.equals("mencoder_usepcm") && value.length() > 0) {
					mencoder_usepcm = /*PMS.get().isWindows() &&*/ isTrue(value.trim());
				} else if (key.equals("tsmuxer_preremux_pcm") && value.length() > 0) {
					tsmuxer_preremux_pcm = isTrue(value.trim());
				} else if (key.equals("tsmuxer_preremux_ac3") && value.length() > 0) {
					tsmuxer_preremux_ac3 = isTrue(value.trim());
				} else if (key.equals("engines") && value.length() > 0) {
					engines = value.trim();
					if (!engines.equals("none")) {
						enginesAsList = new ArrayList<String>();
						StringTokenizer st = new StringTokenizer(engines, ",");
						while (st.hasMoreTokens()) {
							String engine = st.nextToken();
							enginesAsList.add(engine);
						}
					}
				} else if (key.equals("avisynth_convertfps") && value.length() > 0) {
					avisynth_convertfps = isTrue(value.trim());
				} else if (key.equals("transcode_block_multiple_connections") && value.length() > 0) {
					transcode_block_multiple_connections = isTrue(value.trim());
				} else if (key.equals("tsmuxer_forcefps") && value.length() > 0) {
					tsmuxer_forcefps = isTrue(value.trim());
				} else if (key.equals("folders") && value.length() > 0) {
					folders = value.trim();
				} else if (key.equals("avisynth_script") && value.length() > 0) {
					avisynth_script = value.trim();
				} else if (key.equals("buffertype") && value.length() > 0) {
					filebuffer = value.trim().equals("file");
				} else if (key.equals("ffmpeg_path") && value.length() > 0) {
					ffmpegPath = value.trim();
				} else if (key.equals("alternativeffmpegpath") && value.length() > 0) {
					alternativeffmpegPath = value.trim();
				} else if (key.equals("mencoder_path") && value.length() > 0) {
					mencoderPath = value.trim();
				} else if (key.equals("mplayer_path") && value.length() > 0) {
					mplayerPath = value.trim();
				} else if (key.equals("tsmuxer_path") && value.length() > 0) {
					tsmuxerPath = value.trim();
				} else if (key.equals("encoding") && value.length() > 0) {
					System.setProperty("file.encoding", value.trim());
				} 
				
			}
		}
		
		br.close();
		//return remaining;
		
	}
	
	private WinUtils registry;
	
	public WinUtils getRegistry() {
		return registry;
	}

	private boolean checkProcessExistence(String name, boolean error, String...params) throws Exception {
		PMS.info("launching: " + params[0]);
		/*ProcessBuilder pb = new ProcessBuilder(params);
		Process process = pb.start();*/
		try {
			Process process = Runtime.getRuntime().exec(params);
		
			OutputTextConsumer stderrConsumer = new OutputTextConsumer(process.getErrorStream(), false);
			stderrConsumer.start();
			
			OutputTextConsumer outConsumer = new OutputTextConsumer(process.getInputStream(), false);
			outConsumer.start();
			
			process.waitFor();
			
			if (params[0].equals("vlc") && stderrConsumer.getResults().get(0).startsWith("VLC"))
				return true;
			if (params[0].equals("ffmpeg") && stderrConsumer.getResults().get(0).startsWith("FF"))
				return true;
			int exit = 0;
			process.exitValue();
			if (exit != 0) {
				if (error)
					PMS.minimal("[" + exit + "] Cannot launch " + name + " / Check the presence of " + new File(params[0]).getAbsolutePath() + " ...");
				return false;
			}
			return true;
		} catch (Exception e) {
			if (error)
				PMS.error("Cannot launch " + name + " / Check the presence of " + new File(params[0]).getAbsolutePath() + " ...", e);
			return false;
		}
	}
	
	private boolean init () throws Exception {
		
		windows = System.getProperty("os.name").toUpperCase().startsWith("WIN");
		/*if (!windows) {
			forceMPlayer = true;
		} else {
			File mplayerFolder = new File("mplayer");
			if (mplayerFolder.exists() && mplayerFolder.isDirectory()) {
				File mplayerEXE = new File(mplayerFolder, "mencoder.exe");
				if (mplayerEXE.exists()) {
					forceMPlayer = true;
				}
			}
		}*/
		if (windows) {
			ffmpegPath = "win32/ffmpeg.exe";
			mplayerPath = "win32/mplayer.exe";
			mkfifoPath = "win32/mkfifo.exe";
			vlcPath = "videolan/vlc.exe";
			mencoderPath = "win32/mencoder.exe";
			tsmuxerPath = "win32/tsMuxeR.exe";
			flacPath = "win32/flac.exe";
		} else {
			mkfifoPath = "mkfifo";
			ffmpegPath = "ffmpeg";
			mplayerPath = "mplayer";
			vlcPath = "vlc";
			mencoderPath = "mencoder";
			tsmuxerPath = "linux/tsMuxeR";
			flacPath = "flac";
		}
		
			
		try {
			pw = new PrintWriter(new FileWriter(new File("debug.log")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		port = 5001;
		maxMemoryBufferSize = 400;
		minMemoryBufferSize = 12;
		maxaudiobuffer = 100;
		minstreambuffer = 1;
		proxy = -1;
		
		loadConf();
		
		
		if (System.getProperty("console") == null)
			initGFX();
		else
			frame = new DummyFrame();
		
		frame.setStatusCode(0, "Contacting PS3...", "connect_no-256.png");
		
		minimal("Starting Java PS3 Media Server v" + PMS.VERSION);
		minimal("by shagrath / 2008");
		minimal("http://ps3mediaserver.blogspot.com");
		minimal("");
		minimal("Java " + System.getProperty("java.version") + "-" + System.getProperty("java.vendor"));
		minimal("OS " + System.getProperty("os.name") + " " + System.getProperty("os.arch")  + " " + System.getProperty("os.version"));
		minimal("Encoding: " + System.getProperty("file.encoding"));
		
		//System.out.println(System.getProperties().toString().replace(',', '\n'));
		
		registry = new WinUtils();
		if (registry.getVlcp() != null) {
			String vlc = registry.getVlcp();
			String version = registry.getVlcv();
			if (new File(vlc).exists() && version != null) {
				PMS.info("Found VLC version " + version + " in Windows Registry: " + vlc);
				vlcPath = vlc;
			}
		}
		
		if (enginesAsList != null) {
			for(int i=enginesAsList.size()-1;i>=0;i--) {
				String engine = enginesAsList.get(i);
				if (engine.startsWith("avs") && !registry.isAvis() && PMS.get().isWindows()) {
					PMS.minimal("AviSynth in not installed ! You cannot use " + engine + " as transcoding engine !");
					enginesAsList.remove(i);
				}
			}
		}
		
		if (!checkProcessExistence("FFmpeg", true, ffmpegPath, "-h"))
			ffmpegPath = null;
		//if (forceMPlayer) {
			if (!checkProcessExistence("MPlayer", true, mplayerPath))
				mplayerPath = null;
			if (!checkProcessExistence("MEncoder", true, mencoderPath, "-oac", "help"))
				mencoderPath = null;
		//}
		if (!checkProcessExistence("VLC", false, vlcPath, "-I", "dummy", windows?"--dummy-quiet":"-Z", "vlc://quit")) {
			vlcPath = null;
		}
		/*if (!checkProcessExistence("mkfifo", true, mkfifoPath, windows?"":"--help")) {
			mkfifoPath = null;
			PMS.minimal("SERIOUS ERROR / Mkfifo mechanism not available !?");
		}*/
		
		/*if (forceMPlayer && PMS.get().isExpert())
			forceMPlayer = false;
		*/
		
		
		extensions = new ArrayList<Format>();
		players = new ArrayList<Player>();
		allPlayers = new ArrayList<Player>();
		server = new HTTPServer(port);
		
		registerExtensions();
		registerPlayers();
		
		manageRoot();
		
		boolean binding = false;
		try {
			binding = server.start();
		} catch (BindException b) {
			
			PMS.minimal("FATAL ERROR : Unable to bind on port: " + port + " cause: " + b.getMessage());
			PMS.minimal("Maybe another process is running or hostname is wrong...");
			
		}
		new Thread() {
			public void run () {
				try {
					Thread.sleep(7000);
				} catch (InterruptedException e) {}
				if (!ps3found) {
					frame.setStatusCode(0, "PS3 has not been found. Is it on? You can also check traces and/or configuration, or also the debug.log file", "messagebox_critical-256.png");
				}
			}
		}.start();
		if (!binding) {
			return false;
		}
		if (proxy > 0) {
			minimal("Starting HTTP Proxy Server on port: " + proxy);
			proxyServer = new ProxyServer(proxy);
		}
		
		if (PMS.get().isUsecache()) {
			CacheManager.openCache();
		}
		
		//UPNPHelper.sendByeBye();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					UPNPHelper.sendByeBye();
					PMS.info("Forcing shutdown of all active processes");
					for(Process p:currentProcesses) {
						try {
							p.exitValue();
						} catch (IllegalThreadStateException ise) {
							PMS.debug("Forcing shutdown of process " + p);
							p.destroy();
						}
					}
					PMS.get().getServer().stop();
					UPNPHelper.shutDownListener();
					pw.close();
					Thread.sleep(500);
				} catch (Exception e) { }
			}
		});
		//debug("Waiting 500 milliseconds...");
		//Thread.sleep(500);
		UPNPHelper.sendAlive();
		debug("Waiting 250 milliseconds...");
		Thread.sleep(250);
		UPNPHelper.listen();
		
		return true;
	}
	
	private void manageRoot() throws IOException {
		File files [] = loadFoldersConf(folders);
		if (files == null)
			files = File.listRoots();
		if (PMS.get().isWindows()) {
			
		}
		rootFolder = new RootFolder();
		rootFolder.browse(files);
		
		
		File webConf = new File("WEB.conf");
		if (webConf.exists()) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(webConf));
				String line = null;
				while ((line=br.readLine()) != null) {
					line = line.trim();
					if (line.length() > 0 && !line.startsWith("#") && line.indexOf("=") > -1) {
						String key = line.substring(0, line.indexOf("="));
						String value = line.substring(line.indexOf("=")+1);
						String keys [] = parseFeedKey((String) key);
						if (keys[0].equals("imagefeed") || keys[0].equals("audiofeed") || keys[0].equals("videofeed") || keys[0].equals("audiostream") || keys[0].equals("videostream")) {
							
							String values [] = parseFeedValue((String) value);
							DLNAResource parent = null;
							if (keys[1] != null) {
								StringTokenizer st = new StringTokenizer(keys[1], ",");
								DLNAResource currentRoot = rootFolder;
								while (st.hasMoreTokens()) {
									String folder = st.nextToken();
									parent = currentRoot.searchByName(folder);
									if (parent == null) {
										parent = new VirtualFolder(folder, "");
										currentRoot.addChild(parent);
									}
									currentRoot = parent;
								}
							}
							if (parent == null)
								parent = rootFolder;
							if (keys[0].equals("imagefeed")) {
								parent.addChild(new ImagesFeed(values[0]));
							} else if (keys[0].equals("videofeed")) {
								parent.addChild(new VideosFeed(values[0]));
							} else if (keys[0].equals("audiofeed")) {
								parent.addChild(new AudiosFeed(values[0]));
							} else if (keys[0].equals("audiostream")) {
								parent.addChild(new WebAudioStream(values[0], values[1], values[2]));
							} else if (keys[0].equals("videostream")) {
								parent.addChild(new WebVideoStream(values[0], values[1], values[2]));
							}
						}
					}
				}
			} catch (Exception e) {
				PMS.minimal("Unexpected error in WEB.conf: " + e.getMessage());
			}
		}
		
		//VirtualFolder vf = new VirtualFolder("Expert Mode Folders", null);
		//vf.setExpertMode();
		/*for(File f:files) {
			vf.addChild(new RealFile(f));
		}
		vf.closeChildren(0, false);
		rootFolder.addChild(vf);*/
		
		if (!PMS.get().isHidevideosettings()) {
		VirtualFolder vf = new VirtualFolder("#- VIDEO SETTINGS -#", null);
		/*VirtualFolder vf2 = new VirtualFolder("#- Encoding Profiles / TESTS -#", null);
		vf2.addChild(new EncodingProfileAction("Enter here to (re)enable default encoding profile", null, DEFAULT_PROFILE));
		for(String profile:otherEncodingProfiles) {
			vf2.addChild(new EncodingProfileAction("Enter here to activate encoding profile: " + profile.toUpperCase(), null, profile));
		}
		vf.addChild(vf2);*/
		vf.addChild(new SkipLoopAction("Enter this folder to enable SkipLoopFilter", null));
		vf.addChild(new SkipLoopAction("Enter this folder to disable SkipLoopFilter", null));
		vf.addChild(new AVSyncAction("Enter this folder to enable MEncoder A/V out of sync correction", null));
		vf.addChild(new AVSyncAction("Enter this folder to disable MEncoder A/V out of sync correction", null));
		vf.addChild(new AutoSubLoadingAction("Enter this folder to enable .srt/.sub Auto Loading", null));
		vf.addChild(new AutoSubLoadingAction("Enter this folder to disable .srt/.sub Auto Loading", null));
		//vf.addChild(new RefreshAction("Enter this folder to force refresh of all folders", null));
		
		/*vf.addChild(new VirtualAction("Enable/Disable SkipLoopFilter", "images/Play1Hot_256.png", "videos/action_success-512.mpg", "videos/button_cancel-512.mpg") {
			public boolean enable() {
				skiploopfilter = !skiploopfilter;
				return skiploopfilter;
			}
		});*/
		vf.closeChildren(0, false);
		rootFolder.addChild(vf);
		}
		rootFolder.closeChildren(0, false);
	}
	
	private String [] parseFeedKey(String entry) {
		StringTokenizer st = new StringTokenizer(entry, ".");
		String results [] = new String [2];
		int i = 0;
		while (st.hasMoreTokens()) {
			results[i++] = st.nextToken();
		}
		return results;
	}
	
	private String [] parseFeedValue(String entry) {
		StringTokenizer st = new StringTokenizer(entry, ",");
		String results [] = new String [3];
		int i = 0;
		while (st.hasMoreTokens()) {
			results[i++] = st.nextToken();
		}
		return results;
	}
	
	private void registerExtensions() {
		extensions.add(new MKV());
		extensions.add(new WEB());
		extensions.add(new M4A());
		extensions.add(new MP3());
		extensions.add(new ISO());
		extensions.add(new MPG());
		extensions.add(new JPG());
		extensions.add(new OGG());
		extensions.add(new PNG());
		extensions.add(new GIF());
		extensions.add(new TIF());
		extensions.add(new FLAC());
		extensions.add(new DVRMS());
	}
	
	private void registerPlayers() {
		registerPlayer(new FFMpegVideo());
		registerPlayer(new FFMpegAudio());
		registerPlayer(new MEncoderVideo());
		registerPlayer(new MEncoderAviSynth());
		registerPlayer(new MPlayerAudio());
		registerPlayer(new MEncoderWebVideo());
		registerPlayer(new MPlayerWebVideoDump());
		registerPlayer(new MPlayerWebAudio());
		registerPlayer(new TSMuxerVideo());
		registerPlayer(new TsMuxerAudio());
		registerPlayer(new VideoLanAudioStreaming());
		registerPlayer(new VideoLanVideoStreaming());
		registerPlayer(new FFMpegVideoRemux());
		
		frame.addEngines();
	}
	
	private void registerPlayer(Player p) {
		allPlayers.add(p);
		boolean ok = false;
		if (windows) {
			if (p.executable() == null) {
				minimal("Executable of transcoder profile " + p + " not found!");
				return;
			}
			File executable = new File(p.executable());
			File executable2 = new File(p.executable() + ".exe");
			
			if (executable.exists() || executable2.exists())
				ok = true;
			else {
				minimal("Executable of transcoder profile " + p + " not found!");
				return;
			}
			if (p.avisynth()) {
				ok = false;
				if (registry.isAvis()) {
					ok = true;
				} else {
					minimal("AVISynth not found! Transcoder profile " + p + " will not be used!");
				}
			}
		} else if (!p.avisynth()) {
			ok = true;
		}
		if (ok) {
			minimal("Registering transcoding engine " + p /*+ (p.avisynth()?(" with " + (forceMPlayer?"MPlayer":"AviSynth")):"")*/);
			players.add(p);
		}
	}
	
	public File [] loadFoldersConf(String folders) throws IOException {
		if (folders == null || folders.length() == 0)
			return null;
		ArrayList<File> directories = new ArrayList<File>();
		StringTokenizer st = new StringTokenizer(folders, ",");
		while (st.hasMoreTokens()) {
			String line = st.nextToken();
			File file = new File(line);
			if (file.exists()) {
				if (file.isDirectory()) {
					directories.add(file);
				} else
					PMS.error("File " + line + " is not a directory!", null);
			} else {
				PMS.error("File " + line + " does not exists!", null);
			}
		}
		File f [] = new File[directories.size()];
		for(int j=0;j<f.length;j++)
			f[j] = directories.get(j);
		return f;
	}

	
	public void reset() throws IOException {
		debug("Waiting 1 seconds...");
		UPNPHelper.sendByeBye();
		manageRoot();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		frame.setReloadable(false);
		UPNPHelper.sendAlive();
	}
	
	public static void debug(String msg) {
		instance.message(DEBUG, msg);
	}
	
	public static void info(String msg) {
		instance.message(INFO, msg);
	}
	
	public static void minimal(String msg) {
		instance.message(MINIMAL, msg);
	}
	
	public static void error(String msg, Throwable t) {
		instance.message(msg, t);
	}
	
	private void message(int l, String message) {
		
			String name = Thread.currentThread().getName();
			if (name != null && message != null) {
				String lev = "DEBUG ";
				if (l == 1)
					lev = "INFO  ";
				if (l == 2)
					lev = "TRACE ";
				
				
				message = "[" + name + "] " + lev + sdfHour.format(new Date(System.currentTimeMillis())) + " " + message; 
				if (l == 2)
					System.out.println(message);
				if (l >= level) {
					
					if (frame != null) {
						frame.append(message.trim() + "\n");
					}
				}
				pw.println(message);
				pw.flush();
			}
		
	}
	
	private void message(String error, Throwable t) {
		
			String name = Thread.currentThread().getName();
			if (error != null) {
				String throwableMsg = "";
				if (t != null)
					throwableMsg = ": " + t.getMessage();
				error = "[" + name + "] " + sdfHour.format(new Date(System.currentTimeMillis())) + " " + error + throwableMsg;
			}
			if (error != null) {
				pw.println(error);
				if (frame != null) {
					frame.append(error.trim() + "\n");
				}
			}
			if (t != null) {
				t.printStackTrace(pw);
				t.printStackTrace();
			}
			pw.flush();
			
			if (error != null)
				System.err.println(error);
		
	}

	private String uuid;
	
	public String usn() {
		if (uuid == null) {
			UUID u = UUID.randomUUID();
			uuid = u.toString();
		}
		return "uuid:" + uuid + "::";
		//return "uuid:1234567890TOTO::";
	}
	
	public String getServerName() {
		if (serverName == null) {
			StringBuffer sb = new StringBuffer();
			sb.append(System.getProperty("os.name"));
			sb.append("-");
			sb.append(System.getProperty("os.arch"));
			sb.append("-");
			sb.append(System.getProperty("os.version"));
			sb.append(" UPnP/1.0, PMS");
			serverName = sb.toString();
		}
		return serverName;
	}
	
	
	public static PMS get() {
		if (instance == null) {
			synchronized (lock) {
				if (instance == null) {
					instance = new PMS();
					try {
						if (instance.init())
							PMS.minimal("It's ready! You should see the server appears on XMB");
						else
							PMS.minimal("Some serious errors occurs...");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return instance;
	}
	
	public Format getAssociatedExtension(String filename) {
		PMS.debug("Search extension for " + filename);
		for(Format ext:extensions) {
			if (ext.match(filename)) {
				PMS.debug("Found 1! " + ext.getClass().getName());
				return ext.duplicate();
			}
		}
		return null;
	}
	
	public Player getPlayer(Class<? extends Player> profileClass, int type) {
		for(Player p:players) {
			if (p.getClass().equals(profileClass) && p.type() == type)
				return p;
		}
		return null;
	}
	
	public ArrayList<Player> getPlayers(ArrayList<Class<? extends Player>> profileClasses, int type) {
		ArrayList<Player> compatiblePlayers = new ArrayList<Player>();
		for(Player p:players) {
			if (profileClasses.contains(p.getClass()) && p.type() == type)
				compatiblePlayers.add(p);
		}
		return compatiblePlayers;
	}
	
	public static void main(String args[]) throws IOException {
		if (args.length > 0 && args[0].equals("console"))
			System.setProperty("console", "true");
		PMS.get();
		try {
			// let's allow us time to show up serious errors in the GUI before quitting
			Thread.sleep(60000);
		} catch (InterruptedException e) { }
	}

	public HTTPServer getServer() {
		return server;
	}


	public int getMaxMemoryBufferSize() {
		return maxMemoryBufferSize;
	}
	
	public String replace(String value, String toReplace, String replaceBy) {
		if(value != null) {
			int n = replaceBy.length();
			int m = toReplace.length();
			int index = n * -1;
			while(((index = value.indexOf(toReplace, index +n)) != -1)) {
				value = value.substring(0, index) + replaceBy + value.substring(index + m, value.length());
			}
		}
		return value;
	}

	public File getTempFolder() {
		if (tempFolder == null) {
			File tmp = new File(System.getProperty("java.io.tmpdir"));
			File myTMP = new File(tmp, "javaps3media");
			if (!myTMP.exists())
				myTMP.mkdir();
			
			tempFolder = myTMP;
		}
		return tempFolder;
	}

	public int getMinMemoryBufferSize() {
		return minMemoryBufferSize;
	}

	public ArrayList<Format> getExtensions() {
		return extensions;
	}

	public ArrayList<Player> getPlayers() {
		return players;
	}
	
	private String getTrue(boolean value) {
		return value?"true":"false";
	}
	
	public void save() {
		
		PrintWriter saveFile = null;
		try {
			saveFile = new PrintWriter(new FileWriter(new File("PMS.conf")));
		} catch (IOException e) {
		PMS.error(null, e);
		}
		saveFile.println("folders=" + folders);
		saveFile.println("hostname=" + (hostname!=null?hostname:""));
		saveFile.println("port=" + (port!=5001?port:""));
		saveFile.println("maxvideobuffer=" + maxMemoryBufferSize);
		saveFile.println("thumbnails=" + getTrue(thumbnails));
		saveFile.println("thumbnail_seek_pos=" + thumbnail_seek_pos);
		saveFile.println("nbcores=" + nbcores);
		saveFile.println("turbomode=" + getTrue(turbomode));
		saveFile.println("minimized=" + getTrue(minimized));
		saveFile.println("hidevideosettings=" + getTrue(hidevideosettings));
		saveFile.println("usecache=" + getTrue(usecache));
		saveFile.println("charsetencoding=" + (charsetencoding!=null?charsetencoding:""));
		saveFile.println("engines=" + engines);
		/*saveFile.println("audioengines=" + audioengines);
		saveFile.println("webaudioengines=" + webaudioengines);
		saveFile.println("webvideoengines=" + webvideoengines);*/
		saveFile.println("autoloadsrt=" + getTrue(usesubs));
		saveFile.println("avisynth_convertfps=" + getTrue(avisynth_convertfps));
		saveFile.println("avisynth_script=" + (avisynth_script!=null?avisynth_script:""));
		saveFile.println("transcode_block_multiple_connections=" + getTrue(transcode_block_multiple_connections));
		saveFile.println("tsmuxer_forcefps=" + getTrue(tsmuxer_forcefps));
		saveFile.println("tsmuxer_preremux_pcm=" + getTrue(tsmuxer_preremux_pcm));
		saveFile.println("tsmuxer_preremux_ac3=" + getTrue(tsmuxer_preremux_ac3));
		saveFile.println("audiochannels=" + audiochannels);
		saveFile.println("audiobitrate=" + audiobitrate);
		saveFile.println("maximumbitrate=" + maximumbitrate);
		saveFile.println("skiploopfilter=" + getTrue(skiploopfilter));
		saveFile.println("mencoder_fontconfig=" + getTrue(mencoder_fontconfig));
		saveFile.println("mencoder_forcefps=" + getTrue(mencoder_forcefps));
		saveFile.println("mencoder_usepcm=" + getTrue(mencoder_usepcm));
		saveFile.println("mencoder_intelligent_sync=" + getTrue(mencoder_intelligent_sync));
		saveFile.println("mencoder_decode=" + (mencoder_decode!=null?mencoder_decode:""));
		saveFile.println("mencoder_encode=" + (mencoder_main!=null?mencoder_main:""));
		saveFile.println("mencoder_nooutofsync=" + getTrue(mencoder_nooutofsync));
		saveFile.println("mencoder_audiolangs=" + (mencoder_audiolangs!=null?mencoder_audiolangs:""));
		saveFile.println("mencoder_sublangs=" + (mencoder_sublangs!=null?mencoder_sublangs:""));
		saveFile.println("mencoder_subfribidi=" + getTrue(mencoder_subfribidi));
		saveFile.println("mencoder_ass_scale=" + (mencoder_ass_scale!=null?mencoder_ass_scale:""));
		saveFile.println("mencoder_ass_margin=" + (mencoder_ass_margin!=null?mencoder_ass_margin:""));
		saveFile.println("mencoder_ass_outline=" + (mencoder_ass_outline!=null?mencoder_ass_outline:""));
		saveFile.println("mencoder_ass_shadow=" + (mencoder_ass_shadow!=null?mencoder_ass_shadow:""));
		saveFile.println("mencoder_noass_scale=" + (mencoder_noass_scale!=null?mencoder_noass_scale:""));
		saveFile.println("mencoder_noass_subpos=" + (mencoder_noass_subpos!=null?mencoder_noass_subpos:""));
		saveFile.println("mencoder_noass_blur=" + (mencoder_noass_blur!=null?mencoder_noass_blur:""));
		saveFile.println("mencoder_noass_outline=" + (mencoder_noass_outline!=null?mencoder_noass_outline:""));
		saveFile.println("mencoder_subcp=" + (mencoder_subcp!=null?mencoder_subcp:""));
		saveFile.println("mencoder_ass=" + getTrue(mencoder_ass));
		saveFile.println("mencoder_disablesubs=" + getTrue(mencoder_disablesubs));
		saveFile.println("ffmpeg=" + (ffmpeg!=null?ffmpeg:""));
		if (alternativeffmpegPath != null)
			saveFile.println("alternativeffmpegpath=" + alternativeffmpegPath);
		saveFile.flush();
		saveFile.close();
	}
}
