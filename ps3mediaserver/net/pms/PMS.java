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

import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.logging.LogManager;

import org.apache.commons.configuration.ConfigurationException;

import com.sun.jna.Platform;

import net.pms.configuration.PmsConfiguration;
import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.AudiosFeed;
import net.pms.dlna.DLNAMediaDatabase;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.ImagesFeed;
import net.pms.dlna.RootFolder;
import net.pms.dlna.VideosFeed;
import net.pms.dlna.WebAudioStream;
import net.pms.dlna.WebVideoStream;
import net.pms.dlna.virtual.MediaLibrary;
import net.pms.dlna.virtual.VirtualVideoAction;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.encoders.FFMpegAudio;
import net.pms.encoders.FFMpegVideo;

import net.pms.encoders.FFMpegDVRMSRemux;
import net.pms.encoders.MEncoderAviSynth;
import net.pms.encoders.MEncoderVideo;
import net.pms.encoders.MEncoderWebVideo;
import net.pms.encoders.MPlayerAudio;
import net.pms.encoders.MPlayerWebAudio;
import net.pms.encoders.MPlayerWebVideoDump;
import net.pms.encoders.Player;
import net.pms.encoders.RAWThumbnailer;
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
import net.pms.formats.RAW;
import net.pms.formats.TIF;
import net.pms.formats.WEB;
import net.pms.gui.DummyFrame;
import net.pms.gui.IFrame;
import net.pms.io.OutputParams;
import net.pms.io.ProcessWrapperImpl;
import net.pms.io.WinUtils;
import net.pms.network.HTTPServer;
import net.pms.network.ProxyServer;
import net.pms.network.UPNPHelper;
import net.pms.newgui.LooksFrame;
import net.pms.update.AutoUpdater;
import net.pms.util.PMSUtil;
import net.pms.util.ProcessUtil;
import net.pms.util.SystemErrWrapper;

import net.pms.xmlwise.Plist;
import java.util.Map;
import java.util.HashMap;
import net.pms.dlna.RealFile;
import java.net.URI;
import java.net.URLDecoder;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

public class PMS {
	
	private static final String UPDATE_SERVER_URL = "http://ps3mediaserver.googlecode.com/svn/trunk/ps3mediaserver/update.data"; //$NON-NLS-1$
	public static final String VERSION = "1.20"; //$NON-NLS-1$
	public static final String AVS_SEPARATOR = "\1"; //$NON-NLS-1$

	// TODO(tcox):  This shouldn't be static
	private static PmsConfiguration configuration;

	public IFrame getFrame() {
		return frame;
	}

	public RootFolder getRootFolder(RendererConfiguration renderer) {
		// something to do here for multiple directories views for each renderer
		return rootFolder;
	}

	private static PMS instance = null;
	private static byte[] lock = null;
	static {
		lock = new byte[0];
		sdfHour = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US); //$NON-NLS-1$
		sdfDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US); //$NON-NLS-1$
	}
	
	private ArrayList<RendererConfiguration> foundRenderers = new ArrayList<RendererConfiguration>();
	
	public void setRendererfound(RendererConfiguration mediarenderer) {
		if (!foundRenderers.contains(mediarenderer)) {
			foundRenderers.add(mediarenderer);
			frame.addRendererIcon(mediarenderer.getRank(), mediarenderer.getRendererNameWithAddress(), mediarenderer.getRendererIcon());
			if (mediarenderer.isPS3())
				frame.setStatusCode(0, Messages.getString("PMS.5"), "clients/PS3_256.png"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		/*if (mediarenderer == HTTPResource.PS3) {
			frame.setStatusCode(0, Messages.getString("PMS.5"), "PS3_2.png"); //$NON-NLS-1$ //$NON-NLS-2$
		} else if (mediarenderer == HTTPResource.XBOX && !foundRenderers.contains(HTTPResource.PS3)) {
			frame.setStatusCode(0, "Xbox found", "xbox360.png"); //$NON-NLS-1$ //$NON-NLS-2$
		}*/
		
	}
	
	private RootFolder rootFolder;
	private HTTPServer server;
	private String serverName;
	private ArrayList<Format> extensions;
	private ArrayList<Player> players;
	private ArrayList<Player> allPlayers;
	public ArrayList<Player> getAllPlayers() {
		return allPlayers;
	}
	
	private ProxyServer proxyServer;
	
	public ProxyServer getProxy() {
		return proxyServer;
	}

	public static SimpleDateFormat sdfDate;
	public static SimpleDateFormat sdfHour;
	
	public static final int DEBUG = 0;
	public static final int INFO = 1;
	public static final int MINIMAL = 2;
	
	private PrintWriter pw;
	
	public ArrayList<Process> currentProcesses = new ArrayList<Process>();
	
	private PMS() {
	}
	
	IFrame frame;
	
	public boolean isWindows() {
		return Platform.isWindows();
	}

	private int proxy;
	
	private WinUtils registry;
	
	public WinUtils getRegistry() {
		return registry;
	}

	/*private boolean checkProcessExistence(String name, boolean error, String...params) throws Exception {
		PMS.info("launching: " + params[0]); //$NON-NLS-1$
		
		try {
			final Process process = Runtime.getRuntime().exec(params);
		
			OutputTextConsumer stderrConsumer = new OutputTextConsumer(process.getErrorStream(), false);
			stderrConsumer.start();
			
			OutputTextConsumer outConsumer = new OutputTextConsumer(process.getInputStream(), false);
			outConsumer.start();
			
			Runnable r = new Runnable() {
				public void run() {
					try {
						process.waitFor();
					} catch (InterruptedException e) {
					}
				}
			};
			Thread checkThread = new Thread(r);
			checkThread.start();
			checkThread.join(2000);
			checkThread.interrupt();
			checkThread = null;
			
			if (params[0].equals("vlc") && stderrConsumer.getResults().get(0).startsWith("VLC")) //$NON-NLS-1$ //$NON-NLS-2$
				return true;
			if (params[0].equals("ffmpeg") && stderrConsumer.getResults().get(0).startsWith("FF")) //$NON-NLS-1$ //$NON-NLS-2$
				return true;
			int exit = 0;
			process.exitValue();
			if (exit != 0) {
				if (error)
					PMS.minimal("[" + exit + "] Cannot launch " + name + " / Check the presence of " + new File(params[0]).getAbsolutePath() + " ..."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				return false;
			}
			return true;
		} catch (Exception e) {
			if (error)
				PMS.error("Cannot launch " + name + " / Check the presence of " + new File(params[0]).getAbsolutePath() + " ...", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return false;
		}
	}*/
	
	@SuppressWarnings("unused")
	private PrintStream stderr = System.err;  
	
	private DLNAMediaDatabase database;
	
	public synchronized DLNAMediaDatabase getDatabase() {
		if (PMS.configuration.getUseCache()) {
			if (database == null) {
				database = new DLNAMediaDatabase("medias"); //$NON-NLS-1$
				database.init(false);
				/*try {
					Server server = Server.createWebServer(null);
					server.start();
					PMS.minimal("Starting H2 console on port " + server.getPort());
				} catch (SQLException e) {
					e.printStackTrace();
				}*/
			}
			return database;
		}
		return null;
	}
	
	
	private boolean init () throws Exception {
		
		
		registry = new WinUtils();
					
		File debug = null;
		try {
			debug = new File("debug.log"); //$NON-NLS-1$
			pw = new PrintWriter(new FileWriter(debug)); //$NON-NLS-1$
		} catch (Throwable e) {
			PMS.minimal("Error in accessing debug.log..."); //$NON-NLS-1$
			pw = null;
		} finally {
			if (pw == null) {
				PMS.minimal("Using temp folder for debug.log..."); //$NON-NLS-1$
				debug = new File(configuration.getTempFolder(), "debug.log"); //$NON-NLS-1$
				pw = new PrintWriter(new FileWriter(debug)); //$NON-NLS-1$
			}
		}
		
		AutoUpdater autoUpdater = new AutoUpdater(UPDATE_SERVER_URL, PMS.VERSION);
		if (System.getProperty("console") == null) {//$NON-NLS-1$
			frame = new LooksFrame(autoUpdater, configuration);
			autoUpdater.pollServer();
		} else {
			System.out.println("GUI environment no available"); //$NON-NLS-1$
			System.out.println("Switching to console mode"); //$NON-NLS-1$
			frame = new DummyFrame();
		}
		
		frame.setStatusCode(0, Messages.getString("PMS.130"), "connect_no-256.png"); //$NON-NLS-1$ //$NON-NLS-2$
		
		proxy = -1;
		
		
		
		minimal("Starting Java PS3 Media Server v" + PMS.VERSION); //$NON-NLS-1$
		minimal("by shagrath / 2008-2009"); //$NON-NLS-1$
		minimal("http://ps3mediaserver.blogspot.com"); //$NON-NLS-1$
		minimal("http://code.google.com/p/ps3mediaserver"); //$NON-NLS-1$
		minimal(""); //$NON-NLS-1$
		minimal("Java " + System.getProperty("java.version") + "-" + System.getProperty("java.vendor")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		minimal("OS " + System.getProperty("os.name") + " " + System.getProperty("os.arch")  + " " + System.getProperty("os.version")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		minimal("Encoding: " + System.getProperty("file.encoding")); //$NON-NLS-1$ //$NON-NLS-2$
		minimal("Temp folder: " + configuration.getTempFolder()); //$NON-NLS-1$
		
		RendererConfiguration.loadRendererConfigurations();
		
		//System.out.println(System.getProperties().toString().replace(',', '\n'));
				
		
		/*
		if (!checkProcessExistence("FFmpeg", true, configuration.getFfmpegPath(), "-h")) //$NON-NLS-1$ //$NON-NLS-2$
			configuration.disableFfmpeg();
		//if (forceMPlayer) {
			if (!checkProcessExistence("MPlayer", true, configuration.getMplayerPath())) //$NON-NLS-1$
				configuration.disableMplayer();
			if (!checkProcessExistence("MEncoder", true, configuration.getMencoderPath(), "-oac", "help")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				configuration.disableMEncoder();
			}
		//}
		if (!checkProcessExistence("VLC", false, configuration.getVlcPath(), "-I", "dummy", isWindows()?"--dummy-quiet":"-Z", "vlc://quit")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			configuration.disableVlc();
		}*/
		
		// check the existence of Vsfilter.dll
		if (registry.isAvis() && registry.getAvsPluginsDir() != null) {
			PMS.minimal("Found AviSynth plugins dir: " + registry.getAvsPluginsDir().getAbsolutePath()); //$NON-NLS-1$
			File vsFilterdll = new File(registry.getAvsPluginsDir(), "VSFilter.dll"); //$NON-NLS-1$
			if (!vsFilterdll.exists()) {
				PMS.minimal("!!! It seems VSFilter.dll is not installed into the AviSynth plugins dir ! It could be troublesome for playing subtitled videos with AviSynth !!!"); //$NON-NLS-1$
			}
		}
		
		if (registry.getVlcv() != null && registry.getVlcp() != null) {
			PMS.minimal("Found VideoLAN version " + registry.getVlcv() + " at: " + registry.getVlcp()); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		// force use of specific dvr ms muxer when it's installed in the right place
		File dvrsMsffmpegmuxer = new File("win32/dvrms/ffmpeg_MPGMUX.exe"); //$NON-NLS-1$
		if (dvrsMsffmpegmuxer.exists())
			configuration.setFfmpegAlternativePath(dvrsMsffmpegmuxer.getAbsolutePath());
		
		// disable jaudiotagger logging
		LogManager.getLogManager().readConfiguration(new ByteArrayInputStream("org.jaudiotagger.level=OFF".getBytes())); //$NON-NLS-1$
		
		// wrap System.err
		System.setErr(new PrintStream(new SystemErrWrapper(), true));
		
		extensions = new ArrayList<Format>();
		players = new ArrayList<Player>();
		allPlayers = new ArrayList<Player>();
		server = new HTTPServer(configuration.getServerPort());
		
		registerExtensions();
		registerPlayers();
		
		manageRoot();
		
		boolean binding = false;
		try {
			binding = server.start();
		} catch (BindException b) {
			
			PMS.minimal("FATAL ERROR : Unable to bind on port: " + configuration.getServerPort() + " cause: " + b.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
			PMS.minimal("Maybe another process is running or hostname is wrong..."); //$NON-NLS-1$
			
		}
		new Thread() {
			public void run () {
				try {
					Thread.sleep(7000);
				} catch (InterruptedException e) {}
				boolean ps3found = false;
				for(RendererConfiguration r:foundRenderers) {
					if (r.isPS3())
						ps3found = true;
				}
				if (!ps3found) {
					if (foundRenderers.size() == 0)
						frame.setStatusCode(0, Messages.getString("PMS.0"), "messagebox_critical-256.png"); //$NON-NLS-1$ //$NON-NLS-2$
					else {
						frame.setStatusCode(0, "Another media renderer other than PS3 has been detected... This software is tuned for PS3 but may work with your renderer", "messagebox_warning-256.png");
					}
					
				}
			}
		}.start();
		if (!binding) {
			return false;
		}
		if (proxy > 0) {
			minimal("Starting HTTP Proxy Server on port: " + proxy); //$NON-NLS-1$
			proxyServer = new ProxyServer(proxy);
		}
		
		getDatabase();
		
		//UPNPHelper.sendByeBye();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					UPNPHelper.sendByeBye();
					PMS.info("Forcing shutdown of all active processes"); //$NON-NLS-1$
					for(Process p:currentProcesses) {
						try {
							p.exitValue();
						} catch (IllegalThreadStateException ise) {
							PMS.debug("Forcing shutdown of process " + p); //$NON-NLS-1$
							ProcessUtil.destroy(p);
						}
					}
					PMS.get().getServer().stop();
					UPNPHelper.shutDownListener();
					if (pw != null)
						pw.close();
					Thread.sleep(500);
				} catch (Exception e) { }
			}
		});
		//debug("Waiting 500 milliseconds...");
		//Thread.sleep(500);
		UPNPHelper.sendAlive();
		debug("Waiting 250 milliseconds..."); //$NON-NLS-1$
		Thread.sleep(250);
		UPNPHelper.listen();
		
		return true;
	}
	
	private void manageRoot() throws IOException {
		File files [] = loadFoldersConf(configuration.getFolders());
		if (files == null || files.length == 0)
			files = File.listRoots();
		if (PMS.get().isWindows()) {
			
		}
		rootFolder = new RootFolder();
		rootFolder.browse(files);
		
		
		File webConf = new File("WEB.conf"); //$NON-NLS-1$
		if (webConf.exists()) {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(webConf), "UTF-8")); //$NON-NLS-1$
				String line = null;
				while ((line=br.readLine()) != null) {
					line = line.trim();
					if (line.length() > 0 && !line.startsWith("#") && line.indexOf("=") > -1) { //$NON-NLS-1$ //$NON-NLS-2$
						String key = line.substring(0, line.indexOf("=")); //$NON-NLS-1$
						String value = line.substring(line.indexOf("=")+1); //$NON-NLS-1$
						String keys [] = parseFeedKey((String) key);
						if (keys[0].equals("imagefeed") || keys[0].equals("audiofeed") || keys[0].equals("videofeed") || keys[0].equals("audiostream") || keys[0].equals("videostream")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
							
							String values [] = parseFeedValue((String) value);
							DLNAResource parent = null;
							if (keys[1] != null) {
								StringTokenizer st = new StringTokenizer(keys[1], ","); //$NON-NLS-1$
								DLNAResource currentRoot = rootFolder;
								while (st.hasMoreTokens()) {
									String folder = st.nextToken();
									parent = currentRoot.searchByName(folder);
									if (parent == null) {
										parent = new VirtualFolder(folder, ""); //$NON-NLS-1$
										currentRoot.addChild(parent);
									}
									currentRoot = parent;
								}
							}
							if (parent == null)
								parent = rootFolder;
							if (keys[0].equals("imagefeed")) { //$NON-NLS-1$
								parent.addChild(new ImagesFeed(values[0]));
							} else if (keys[0].equals("videofeed")) { //$NON-NLS-1$
								parent.addChild(new VideosFeed(values[0]));
							} else if (keys[0].equals("audiofeed")) { //$NON-NLS-1$
								parent.addChild(new AudiosFeed(values[0]));
							} else if (keys[0].equals("audiostream")) { //$NON-NLS-1$
								parent.addChild(new WebAudioStream(values[0], values[1], values[2]));
							} else if (keys[0].equals("videostream")) { //$NON-NLS-1$
								parent.addChild(new WebVideoStream(values[0], values[1], values[2]));
							}
						}
					}
				}
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
				PMS.minimal("Unexpected error in WEB.conf: " + e.getMessage()); //$NON-NLS-1$
			}
		}


		if (Platform.isMac()) {
 			if (PMS.getConfiguration().getIphotoEnabled()) {
 				addiPhotoFolder();
 			}
 			if (PMS.getConfiguration().getItunesEnabled()) {
 				addiTunesFolder();
 			}
		}
	
		addMediaLibraryFolder();
	
		addVideoSettingssFolder();
		
		rootFolder.closeChildren(0, false);
	}


	@SuppressWarnings("unchecked")
	public void addiPhotoFolder() {
		if (Platform.isMac()) {

			Map<String, Object> iPhotoLib;
			ArrayList ListofRolls;
			HashMap Roll;
			HashMap PhotoList;
			HashMap Photo;
			ArrayList RollPhotos;

			try {
 				Process prc = Runtime.getRuntime().exec("defaults read com.apple.iapps iPhotoRecentDatabases");  
 				BufferedReader in = new BufferedReader(  
							new InputStreamReader(prc.getInputStream()));  
 				String line = null;  
 				if ((line = in.readLine()) != null) {
 					line = in.readLine();		//we want the 2nd line
 					line = line.trim();		//remove extra spaces	
 					line = line.substring(1, line.length() - 1); // remove quotes and spaces
 				}
 				in.close();
 				URI tURI = new URI(line);
 				iPhotoLib = Plist.load(URLDecoder.decode(tURI.toURL().getFile(), System.getProperty("file.encoding")));    // loads the (nested) properties.
				PhotoList = (HashMap) iPhotoLib.get("Master Image List");	// the list of photos
				ListofRolls = (ArrayList) iPhotoLib.get("List of Rolls");	// the list of events (rolls)
				VirtualFolder vf = new VirtualFolder("iPhoto Library",null); //$NON-NLS-1$
				for (Object item : ListofRolls) {
					Roll = (HashMap) item;
					VirtualFolder rf = new VirtualFolder(Roll.get("RollName").toString(),null); //$NON-NLS-1$
					RollPhotos = (ArrayList) Roll.get("KeyList");	// list of photos in an event (roll)
					for (Object p : RollPhotos) {
						Photo = (HashMap) PhotoList.get(p);
						RealFile file = new RealFile(new File(Photo.get("ImagePath").toString()));
	       	                         	rf.addChild(file);
					}
					vf.addChild(rf); //$NON-NLS-1$
	 			}
				rootFolder.addChild(vf);	
			} catch (Exception e) {
				PMS.error("Something wrong with the iPhoto Library scan: ",e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                	}
		}
	}

	@SuppressWarnings("unchecked")
	public void addiTunesFolder() {
		if (Platform.isMac()) {
			Map<String, Object> iTunesLib;
			ArrayList Playlists;
       	         	HashMap Playlist;
                	HashMap Tracks;
            		HashMap Track;
	                ArrayList PlaylistTracks;

	                try {
 				Process prc = Runtime.getRuntime().exec("defaults read com.apple.iapps iTunesRecentDatabases");
                                BufferedReader in = new BufferedReader(
                                                        new InputStreamReader(prc.getInputStream()));
                                String line = null;
                                if ((line = in.readLine()) != null) {
                                        line = in.readLine();           //we want the 2nd line
                                        line = line.trim();             //remove extra spaces
                                        line = line.substring(1, line.length() - 1); // remove quotes and spaces
                                }
                                in.close();
                                URI tURI = new URI(line);
 				iTunesLib = Plist.load(URLDecoder.decode(tURI.toURL().getFile(), System.getProperty("file.encoding")));     // loads the (nested) properties.
	                        Tracks = (HashMap) iTunesLib.get("Tracks");       // the list of tracks
	                        Playlists = (ArrayList) iTunesLib.get("Playlists");       // the list of Playlists
	                        VirtualFolder vf = new VirtualFolder("iTunes Library",null); //$NON-NLS-1$
	                        for (Object item : Playlists) {
	                                Playlist = (HashMap) item;
	                                VirtualFolder pf = new VirtualFolder(Playlist.get("Name").toString(),null); //$NON-NLS-1$
	                                PlaylistTracks = (ArrayList) Playlist.get("Playlist Items");   // list of tracks in a playlist
					if (PlaylistTracks != null) {
	                                	for (Object t : PlaylistTracks) {
							HashMap td = (HashMap) t;
							Track = (HashMap) Tracks.get(td.get("Track ID").toString());
 							if (Track.get("Location").toString().startsWith("file://")) {
 								URI tURI2 = new URI(Track.get("Location").toString());
 								RealFile file = new RealFile(new File(URLDecoder.decode(tURI2.toURL().getFile(), "UTF-8")));
 	       	                                 		pf.addChild(file);
 							}	                                	}
					}
	                                vf.addChild(pf); //$NON-NLS-1$
	                        }
	                        rootFolder.addChild(vf);
	                } catch (Exception e) {
	                        PMS.error("Something wrong with the iTunes Library scan: ",e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	                }
		}
	}
	
	public void addVideoSettingssFolder() {
		if (!PMS.configuration.getHideVideoSettings()) {
			VirtualFolder vf = new VirtualFolder(Messages.getString("PMS.37"), null); //$NON-NLS-1$
			VirtualFolder vfSub = new VirtualFolder(Messages.getString("PMS.8"), null); //$NON-NLS-1$
			vf.addChild(vfSub);
			
			vf.addChild(new VirtualVideoAction(Messages.getString("PMS.3"), configuration.isMencoderNoOutOfSync()) { //$NON-NLS-1$
				public boolean enable() {
					configuration.setMencoderNoOutOfSync(!configuration.isMencoderNoOutOfSync());
					return configuration.isMencoderNoOutOfSync();
				}
			});
			
			vf.addChild(new VirtualVideoAction(Messages.getString("PMS.14"), configuration.isMencoderMuxWhenCompatible()) {  //$NON-NLS-1$
				public boolean enable() {
					configuration.setMencoderMuxWhenCompatible(!configuration.isMencoderMuxWhenCompatible());
					
					return  configuration.isMencoderMuxWhenCompatible();
				}
			});
			
			vf.addChild(new VirtualVideoAction(Messages.getString("PMS.4"), configuration.isMencoderYadif()) { //$NON-NLS-1$
				public boolean enable() {
					configuration.setMencoderYadif(!configuration.isMencoderYadif());
					
					return  configuration.isMencoderYadif();
				}
			});
			
			vfSub.addChild(new VirtualVideoAction(Messages.getString("PMS.10"), configuration.isMencoderDisableSubs()) { //$NON-NLS-1$
				public boolean enable() {
					boolean oldValue = configuration.isMencoderDisableSubs();
					boolean newValue = ! oldValue;
					configuration.setMencoderDisableSubs( newValue );
					return newValue;
				}
			});
			
			vfSub.addChild(new VirtualVideoAction(Messages.getString("PMS.6"), configuration.getUseSubtitles()) { //$NON-NLS-1$
				public boolean enable() {
					boolean oldValue = configuration.getUseSubtitles();
					boolean newValue = ! oldValue;
					configuration.setUseSubtitles( newValue );
					return newValue;
				}
			});
			
			vfSub.addChild(new VirtualVideoAction(Messages.getString("MEncoderVideo.36"), configuration.isMencoderAssDefaultStyle()) { //$NON-NLS-1$
				public boolean enable() {
					boolean oldValue = configuration.isMencoderAssDefaultStyle();
					boolean newValue = ! oldValue;
					configuration.setMencoderAssDefaultStyle( newValue );
					return newValue;
				}
			});
			
			vf.addChild(new VirtualVideoAction(Messages.getString("PMS.7"), configuration.getSkipLoopFilterEnabled()) { //$NON-NLS-1$
				public boolean enable() {
					configuration.setSkipLoopFilterEnabled( !configuration.getSkipLoopFilterEnabled() );
					return configuration.getSkipLoopFilterEnabled();
				}
			});
			
			vf.addChild(new VirtualVideoAction(Messages.getString("LooksFrame.12"), true) { //$NON-NLS-1$
				public boolean enable() {
					try {
						PMS.get().reset();
					} catch (IOException e) {}
					return true;
				}
			});
			//vf.closeChildren(0, false);
			rootFolder.addChild(vf);
		}
	}
	
	private boolean mediaLibraryAdded = false;
	private MediaLibrary library;
	
	public MediaLibrary getLibrary() {
		return library;
	}

	public boolean addMediaLibraryFolder() {
		if (PMS.configuration.getUseCache() && !mediaLibraryAdded) {
			library = new MediaLibrary();
			if (!PMS.configuration.isHideMediaLibraryFolder())
				rootFolder.addChild(library);
			mediaLibraryAdded = true;
			return true;
		}
		return false;
	}
	
	public boolean installWin32Service() {
		PMS.minimal(Messages.getString("PMS.41")); //$NON-NLS-1$
		String cmdArray [] = new String[] { "win32/service/wrapper.exe", "-r", "wrapper.conf" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		OutputParams output = new OutputParams(PMS.configuration);
		output.noexitcheck = true;
		ProcessWrapperImpl pwuninstall = new ProcessWrapperImpl(cmdArray, output);
		pwuninstall.run();
		cmdArray = new String[] { "win32/service/wrapper.exe", "-i", "wrapper.conf" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		ProcessWrapperImpl pwinstall = new ProcessWrapperImpl(cmdArray, new OutputParams(PMS.configuration));
		pwinstall.run();
		return pwinstall.isSuccess();
	}
	
	private String [] parseFeedKey(String entry) {
		StringTokenizer st = new StringTokenizer(entry, "."); //$NON-NLS-1$
		String results [] = new String [2];
		int i = 0;
		while (st.hasMoreTokens()) {
			results[i++] = st.nextToken();
		}
		return results;
	}
	
	private String [] parseFeedValue(String entry) {
		StringTokenizer st = new StringTokenizer(entry, ","); //$NON-NLS-1$
		String results [] = new String [3];
		int i = 0;
		while (st.hasMoreTokens()) {
			results[i++] = st.nextToken();
		}
		return results;
	}
	
	private void registerExtensions() {
		extensions.add(new WEB());
		extensions.add(new MKV());
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
		extensions.add(new RAW());
	}
	
	private void registerPlayers() {
		assertThat(configuration, notNullValue());
		if (Platform.isWindows())
			registerPlayer(new FFMpegVideo());
		registerPlayer(new FFMpegAudio(configuration));
		registerPlayer(new MEncoderVideo(configuration));
		if (Platform.isWindows())
			registerPlayer(new MEncoderAviSynth(configuration));
		registerPlayer(new MPlayerAudio(configuration));
		registerPlayer(new MEncoderWebVideo(configuration));
		registerPlayer(new MPlayerWebVideoDump(configuration));
		registerPlayer(new MPlayerWebAudio(configuration));
		registerPlayer(new TSMuxerVideo(configuration));
		registerPlayer(new TsMuxerAudio(configuration));
		registerPlayer(new VideoLanAudioStreaming(configuration));
		registerPlayer(new VideoLanVideoStreaming(configuration));
		if (Platform.isWindows())
			registerPlayer(new FFMpegDVRMSRemux());
		registerPlayer(new RAWThumbnailer());
		frame.addEngines();
	}
	
	private void registerPlayer(Player p) {
		allPlayers.add(p);
		boolean ok = false;
		if (Player.NATIVE.equals(p.executable()))
			ok = true;
		else {
			if (isWindows()) {
				if (p.executable() == null) {
					minimal("Executable of transcoder profile " + p + " not found!"); //$NON-NLS-1$ //$NON-NLS-2$
					return;
				}
				File executable = new File(p.executable());
				File executable2 = new File(p.executable() + ".exe"); //$NON-NLS-1$
				
				if (executable.exists() || executable2.exists())
					ok = true;
				else {
					minimal("Executable of transcoder profile " + p + " not found!"); //$NON-NLS-1$ //$NON-NLS-2$
					return;
				}
				if (p.avisynth()) {
					ok = false;
					if (registry.isAvis()) {
						ok = true;
					} else {
						minimal("AVISynth not found! Transcoder profile " + p + " will not be used!"); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
			} else if (!p.avisynth()) {
				ok = true;
			}
		}
		if (ok) {
			minimal("Registering transcoding engine " + p /*+ (p.avisynth()?(" with " + (forceMPlayer?"MPlayer":"AviSynth")):"")*/); //$NON-NLS-1$
			players.add(p);
		}
	}
	
	public File [] loadFoldersConf(String folders) throws IOException {
		if (folders == null || folders.length() == 0)
			return null;
		ArrayList<File> directories = new ArrayList<File>();
		StringTokenizer st = new StringTokenizer(folders, ","); //$NON-NLS-1$
		while (st.hasMoreTokens()) {
			String line = st.nextToken();
			File file = new File(line);
			if (file.exists()) {
				if (file.isDirectory()) {
					directories.add(file);
				} else
					PMS.error("File " + line + " is not a directory!", null); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				PMS.error("File " + line + " does not exists!", null); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		File f [] = new File[directories.size()];
		for(int j=0;j<f.length;j++)
			f[j] = directories.get(j);
		return f;
	}

	
	public void reset() throws IOException {
		debug("Waiting 1 seconds..."); //$NON-NLS-1$
		UPNPHelper.sendByeBye();
		server.stop();
		server = null;
		mediaLibraryAdded = false;
		manageRoot();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		server = new HTTPServer(configuration.getServerPort());
		server.start();
		frame.setReloadable(false);
		UPNPHelper.sendAlive();
	}
	
	public static void debug(String msg) {
		if (instance != null)
			instance.message(DEBUG, msg);
		else
			System.out.println(msg);
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
				String lev = "DEBUG "; //$NON-NLS-1$
				if (l == 1)
					lev = "INFO  "; //$NON-NLS-1$
				if (l == 2)
					lev = "TRACE "; //$NON-NLS-1$
				
				
				message = "[" + name + "] " + lev + sdfHour.format(new Date(System.currentTimeMillis())) + " " + message;  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				if (l == 2)
					System.out.println(message);
				if (l >= configuration.getLoggingLevel()) {
					
					if (frame != null) {
						frame.append(message.trim() + "\n"); //$NON-NLS-1$
					}
				}
				if (pw != null) {
					pw.println(message);
					pw.flush();
				}
			}
		
	}
	
	private void message(String error, Throwable t) {
		
			String name = Thread.currentThread().getName();
			if (error != null) {
				String throwableMsg = ""; //$NON-NLS-1$
				if (t != null)
					throwableMsg = ": " + t.getMessage(); //$NON-NLS-1$
				error = "[" + name + "] " + sdfHour.format(new Date(System.currentTimeMillis())) + " " + error + throwableMsg; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			if (error != null) {
				if (pw != null)
					pw.println(error);
				if (frame != null) {
					frame.append(error.trim() + "\n"); //$NON-NLS-1$
				}
			}
			if (t != null && pw != null) {
				t.printStackTrace(pw);
				t.printStackTrace();
			}
			if (pw != null)
				pw.flush();
			
			if (error != null)
				System.err.println(error);
		
	}

	private String uuid;
	
	public String usn() {
		if (uuid == null) {
			boolean uuidBasedOnMAC = false;
			NetworkInterface ni = null;
			try {
				if (PMS.getConfiguration().getServerHostname() != null && PMS.getConfiguration().getServerHostname().length() > 0) {
					try {
						ni = NetworkInterface.getByInetAddress(InetAddress.getByName(PMS.getConfiguration().getServerHostname()));
					} catch (Exception e) {}
				} else if ( PMS.get().getServer().getNi() != null) {
					ni = PMS.get().getServer().getNi();
				}
				if (ni != null) {
				
					byte[] addr = PMSUtil.getHardwareAddress(ni); // return null when java.net.preferIPv4Stack=true
					if (addr != null) {
						uuid = UUID.nameUUIDFromBytes(addr).toString();
						uuidBasedOnMAC = true;
					} else
						PMS.minimal("Unable to retrieve MAC address for UUID creation: using a random one..."); //$NON-NLS-1$
				}
			} catch (Throwable e) {
				PMS.minimal("Switching to random UUID cause there's an error in getting UUID from MAC address: " + e.getMessage()); //$NON-NLS-1$
			}
			
			if (!uuidBasedOnMAC) {
				UUID u = UUID.randomUUID();
				uuid = u.toString();
			}
			PMS.minimal("Using following UUID: " + uuid); //$NON-NLS-1$
		}
		return "uuid:" + uuid ; //$NON-NLS-1$ //$NON-NLS-2$
		//return "uuid:1234567890TOTO::";
	}
	
	public String getServerName() {
		if (serverName == null) {
			StringBuffer sb = new StringBuffer();
			sb.append(System.getProperty("os.name").replace(" ", "_")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			sb.append("-"); //$NON-NLS-1$
			sb.append(System.getProperty("os.arch").replace(" ", "_")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			sb.append("-"); //$NON-NLS-1$
			sb.append(System.getProperty("os.version").replace(" ", "_")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			sb.append(", UPnP/1.0, PMS/" + VERSION); //$NON-NLS-1$
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
							PMS.minimal("It's ready! You should see the server appears on XMB"); //$NON-NLS-1$
						else
							PMS.minimal("Some serious errors occurs..."); //$NON-NLS-1$
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return instance;
	}
	
	public Format getAssociatedExtension(String filename) {
		PMS.debug("Search extension for " + filename); //$NON-NLS-1$
		for(Format ext:extensions) {
			if (ext.match(filename)) {
				PMS.debug("Found 1! " + ext.getClass().getName()); //$NON-NLS-1$
				return ext.duplicate();
			}
		}
		return null;
	}
	
	public Player getPlayer(Class<? extends Player> profileClass, Format ext) {
		for(Player p:players) {
			if (p.getClass().equals(profileClass) && p.type() == ext.getType() && !p.excludeFormat(ext))
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
	
	public static void main(String args[]) throws IOException, ConfigurationException {
		if (args.length > 0) {
			for(int a=0;a<args.length;a++) {
				if (args[a].equals("console")) //$NON-NLS-1$
					System.setProperty("console", "true"); //$NON-NLS-1$ //$NON-NLS-2$
				else if (args[a].equals("nativelook")) //$NON-NLS-1$
					System.setProperty("nativelook", "true"); //$NON-NLS-1$ //$NON-NLS-2$
				else if (args[a].equals("scrollbars")) //$NON-NLS-1$
					System.setProperty("scrollbars", "true"); //$NON-NLS-1$ //$NON-NLS-2$
				else if (args[a].equals("noconsole")) //$NON-NLS-1$
					System.setProperty("noconsole", "true"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		try {
			Toolkit.getDefaultToolkit();
			if (GraphicsEnvironment.isHeadless() && System.getProperty("noconsole") == null) //$NON-NLS-1$
				System.setProperty("console", "true"); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (Throwable t) {
			System.err.println("Toolkit error: " + t.getMessage()); //$NON-NLS-1$
			if (System.getProperty("noconsole") == null) //$NON-NLS-1$
				System.setProperty("console", "true"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		configuration = new PmsConfiguration();
		PMS.get();
		try {
			// let's allow us time to show up serious errors in the GUI before quitting
			Thread.sleep(60000);
		} catch (InterruptedException e) { }
	}

	public HTTPServer getServer() {
		return server;
	}

	public ArrayList<Format> getExtensions() {
		return extensions;
	}

	public ArrayList<Player> getPlayers() {
		return players;
	}
	
	public void save() {
		try {
			configuration.save();
		} catch (ConfigurationException e) {
			error("Could not save configuration", e); //$NON-NLS-1$
		}
	}

	public static PmsConfiguration getConfiguration() {
		assertThat(configuration, notNullValue());
		return configuration;
	}

}
