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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.BindException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.LogManager;

import net.pms.configuration.Build;
import net.pms.configuration.PmsConfiguration;
import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.DLNAMediaDatabase;
import net.pms.dlna.RootFolder;
import net.pms.dlna.virtual.MediaLibrary;
import net.pms.encoders.FFMpegAudio;
import net.pms.encoders.FFMpegDVRMSRemux;
import net.pms.encoders.FFMpegVideo;
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
import net.pms.external.ExternalFactory;
import net.pms.external.ExternalListener;
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
import net.pms.io.OutputTextConsumer;
import net.pms.io.ProcessWrapperImpl;
import net.pms.io.WinUtils;
import net.pms.logging.LoggingConfigFileLoader;
import net.pms.network.HTTPServer;
import net.pms.network.ProxyServer;
import net.pms.network.UPNPHelper;
import net.pms.newgui.GeneralTab;
import net.pms.newgui.LooksFrame;
import net.pms.newgui.ProfileChooser;
import net.pms.update.AutoUpdater;
import net.pms.util.PMSUtil;
import net.pms.util.ProcessUtil;
import net.pms.util.SystemErrWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.configuration.ConfigurationException;
import com.sun.jna.Platform;

public class PMS {
	private static final String SCROLLBARS = "scrollbars"; //$NON-NLS-1$
	private static final String NATIVELOOK = "nativelook"; //$NON-NLS-1$
	private static final String CONSOLE = "console"; //$NON-NLS-1$
	private static final String NOCONSOLE = "noconsole"; //$NON-NLS-1$
	private static final String PROFILES = "profiles"; //$NON-NLS-1$

	/**
	 * Update URL used in the {@link AutoUpdater}.
	 */
	private static final String UPDATE_SERVER_URL = "http://ps3mediaserver.googlecode.com/svn/trunk/ps3mediaserver/update2.data"; //$NON-NLS-1$
	/**
	 * Version showed in the UPnP XML descriptor and logs.
	 */
	public static final String VERSION = "1.40.0"; //$NON-NLS-1$
	public static final String AVS_SEPARATOR = "\1"; //$NON-NLS-1$

	// (innot): The logger used for all logging.
	private static final Logger logger = LoggerFactory.getLogger(PMS.class);
	// TODO(tcox):  This shouldn't be static
	private static PmsConfiguration configuration;

	/**Returns a pointer to the main PMS GUI.
	 * @return {@link IFrame} Main PMS window.
	 */
	public IFrame getFrame() {
		return frame;
	}

	/**getRootFolder returns the Root Folder for a given renderer. There could be the case
	 * where a given media renderer needs a different root structure.
	 * @param renderer {@link RendererConfiguration} is the renderer for which to get the RootFolder structure. If <b>null</b>, then
	 * the default renderer is used.
	 * @return {@link RootFolder} The root folder structure for a given renderer
	 */
	public RootFolder getRootFolder(RendererConfiguration renderer) {
		// something to do here for multiple directories views for each renderer
		if (renderer == null) {
			renderer = RendererConfiguration.getDefaultConf();
		}
		return renderer.getRootFolder();
	}
	/**
	 * Pointer to a running PMS server.
	 */
	private static PMS instance = null;
	/**
	 * Semaphore used in order to not create two PMS instances at the same time.
	 */
	private static byte[] lock = null;

	static {
		lock = new byte[0];
		sdfHour = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US); //$NON-NLS-1$
		sdfDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US); //$NON-NLS-1$
	}
	/**
	 * Array of {@link RendererConfiguration} that have been found by PMS.
	 */
	private final ArrayList<RendererConfiguration> foundRenderers = new ArrayList<RendererConfiguration>();

	/**Adds a {@link RendererConfiguration} to the list of media renderers found. The list is being used, for
	 * example, to give the user a graphical representation of the found media renderers.
	 * @param mediarenderer {@link RendererConfiguration}
	 */
	public void setRendererfound(RendererConfiguration mediarenderer) {
		if (!foundRenderers.contains(mediarenderer) && !mediarenderer.isFDSSDP()) {
			foundRenderers.add(mediarenderer);
			frame.addRendererIcon(mediarenderer.getRank(), mediarenderer.getRendererNameWithAddress(), mediarenderer.getRendererIcon());
			if (mediarenderer.isPS3()) {
				frame.setStatusCode(0, Messages.getString("PMS.5"), "clients/ps3slim_220.png"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		/*if (mediarenderer == HTTPResource.PS3) {
		frame.setStatusCode(0, Messages.getString("PMS.5"), "PS3_2.png"); //$NON-NLS-1$ //$NON-NLS-2$
		} else if (mediarenderer == HTTPResource.XBOX && !foundRenderers.contains(HTTPResource.PS3)) {
		frame.setStatusCode(0, "Xbox found", "xbox360.png"); //$NON-NLS-1$ //$NON-NLS-2$
		}*/
	}
	/**
	 * HTTP server that serves the XML files needed by UPnP server and the media files.
	 */
	private HTTPServer server;
	/**
	 * User friendly name for the server.
	 */
	private String serverName;
	private ArrayList<Format> extensions;
	/**
	 * List of registered {@link Player}s.
	 */
	private ArrayList<Player> players;
	private ArrayList<Player> allPlayers;

	/**
	 * @return ArrayList of {@link Player}s.
	 */
	public ArrayList<Player> getAllPlayers() {
		return allPlayers;
	}
	private ProxyServer proxyServer;

	public ProxyServer getProxy() {
		return proxyServer;
	}
	public static SimpleDateFormat sdfDate;
	public static SimpleDateFormat sdfHour;
	public ArrayList<Process> currentProcesses = new ArrayList<Process>();

	private PMS() {
	}
	/**
	 * {@link IFrame} object that represents PMS GUI.
	 */
	IFrame frame;

	/**
	 * @see Platform#isWindows()
	 */
	public boolean isWindows() {
		return Platform.isWindows();
	}
	private int proxy;
	/**Interface to Windows specific functions, like Windows Registry. registry is set by {@link #init()}.
	 * @see WinUtils
	 */
	private WinUtils registry;

	/**
	 * @see WinUtils
	 */
	public WinUtils getRegistry() {
		return registry;
	}

	/**Executes a new Process and creates a fork that waits for its results. 
	 * TODO:Extend explanation on where this is being used.
	 * @param name Symbolic name for the process to be launched, only used in the trace log
	 * @param error (boolean) Set to true if you want PMS to add error messages to the trace pane
	 * @param workDir (File) optional working directory to run the process in
	 * @param params (array of Strings) array containing the command to call and its arguments
	 * @return Returns true if the command exited as expected
	 * @throws Exception TODO: Check which exceptions to use
	 */
	private boolean checkProcessExistence(String name, boolean error, File workDir, String... params) throws Exception {
		logger.debug("launching: " + params[0]); //$NON-NLS-1$

		try {
			ProcessBuilder pb = new ProcessBuilder(params);
			if (workDir != null) {
				pb.directory(workDir);
			}
			final Process process = pb.start();

			OutputTextConsumer stderrConsumer = new OutputTextConsumer(process.getErrorStream(), false);
			stderrConsumer.start();

			OutputTextConsumer outConsumer = new OutputTextConsumer(process.getInputStream(), false);
			outConsumer.start();

			Runnable r = new Runnable() {
				public void run() {
					ProcessUtil.waitFor(process);
				}
			};

			Thread checkThread = new Thread(r);
			checkThread.start();
			checkThread.join(60000);
			checkThread.interrupt();
			checkThread = null;

			// XXX no longer used
			if (params[0].equals("vlc") && stderrConsumer.getResults().get(0).startsWith("VLC")) //$NON-NLS-1$ //$NON-NLS-2$
			{
				return true;
			}
			// XXX no longer used
			if (params[0].equals("ffmpeg") && stderrConsumer.getResults().get(0).startsWith("FF")) //$NON-NLS-1$ //$NON-NLS-2$
			{
				return true;
			}
			int exit = process.exitValue();
			if (exit != 0) {
				if (error) {
					logger.info("[" + exit + "] Cannot launch " + name + " / Check the presence of " + params[0] + " ..."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				}
				return false;
			}
			return true;
		} catch (Exception e) {
			if (error) {
				logger.error("Cannot launch " + name + " / Check the presence of " + params[0] + " ...", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			return false;
		}
	}

	/**
	 * @see System#err
	 */
	@SuppressWarnings("unused")
	private final PrintStream stderr = System.err;

	/**Main resource database that supports search capabilities. Also known as media cache.
	 * @see DLNAMediaDatabase
	 */
	private DLNAMediaDatabase database;

	private void initializeDatabase() {
		database = new DLNAMediaDatabase("medias"); //$NON-NLS-1$
		database.init(false);
	}

	/**Used to get the database. Needed in the case of the Xbox 360, that requires a database.
	 * for its queries.
	 * @return (DLNAMediaDatabase) a reference to the database instance or <b>null</b> if one isn't defined
	 * (e.g. if the cache is disabled).
	 */
	public synchronized DLNAMediaDatabase getDatabase() {
		return database;
	}

	/**Initialisation procedure for PMS.
	 * @return true if the server has been initialized correctly. false if the server could
	 * not be set to listen on the UPnP port.
	 * @throws Exception
	 */
	private boolean init() throws Exception {
		AutoUpdater autoUpdater = null;

		if (Build.isUpdatable()) {
			String serverURL = Build.getUpdateServerURL();
			autoUpdater = new AutoUpdater(serverURL, VERSION);
		}

		registry = new WinUtils();

		if (System.getProperty(CONSOLE) == null) {
			frame = new LooksFrame(autoUpdater, configuration);
		} else {
			System.out.println("GUI environment not available"); //$NON-NLS-1$
			System.out.println("Switching to console mode"); //$NON-NLS-1$
			frame = new DummyFrame();
		}

		frame.setStatusCode(0, Messages.getString("PMS.130"), "connect_no-220.png"); //$NON-NLS-1$ //$NON-NLS-2$
		proxy = -1;

		logger.info("Starting PS3 Media Server " + VERSION); //$NON-NLS-1$
		logger.info("by shagrath / 2008-2011"); //$NON-NLS-1$
		logger.info("http://ps3mediaserver.org"); //$NON-NLS-1$
		logger.info("http://code.google.com/p/ps3mediaserver"); //$NON-NLS-1$
		logger.info("http://ps3mediaserver.blogspot.com"); //$NON-NLS-1$
		logger.info(""); //$NON-NLS-1$
		logger.info("Java: " + System.getProperty("java.version") + "-" + System.getProperty("java.vendor")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		logger.info("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.arch") + " " + System.getProperty("os.version")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		logger.info("Encoding: " + System.getProperty("file.encoding")); //$NON-NLS-1$ //$NON-NLS-2$

		String cwd = new File("").getAbsolutePath();
		logger.info("Working directory: " + cwd);

		logger.info("Temp folder: " + configuration.getTempFolder()); //$NON-NLS-1$
		logger.info("Logging config file: " + LoggingConfigFileLoader.getConfigFilePath()); //$NON-NLS-1$

		HashMap<String, String> lfps = LoggingConfigFileLoader.getLogFilePaths();

		if (lfps != null && lfps.size() > 0) {
			if (lfps.size() == 1) {
				Entry<String, String> entry = lfps.entrySet().iterator().next();
				logger.info(String.format("%s: %s", entry.getKey(), entry.getValue()));
			} else {
				logger.info("Logging to multiple files:");
				Iterator<Entry<String, String>> logsIterator = lfps.entrySet().iterator();
				Entry<String, String> entry;
				while(logsIterator.hasNext()) {
					entry = logsIterator.next();
					logger.info(String.format("%s: %s", entry.getKey(), entry.getValue()));
				}
			}
		}

		logger.info(""); //$NON-NLS-1$

		logger.info("Profile directory: " + configuration.getProfileDirectory());
		String profilePath = configuration.getProfilePath();
		logger.info("Profile path: " + profilePath);

		File profileFile =  new File(profilePath);

		if (profileFile.exists()) {
			String status = String.format("%s%s",
				profileFile.canRead()    ? "r" : "-",
				profileFile.canWrite()   ? "w" : "-"
			);
			logger.info("Profile status: " + status);
		} else {
			logger.info("Profile status: no such file");
		}

		logger.info("Profile name: " + configuration.getProfileName()); //$NON-NLS-1$
		logger.info(""); //$NON-NLS-1$

		RendererConfiguration.loadRendererConfigurations();

		logger.info("Checking MPlayer font cache. It can take a minute or so.");
		checkProcessExistence("MPlayer", true, null, configuration.getMplayerPath(), "dummy");
		if (isWindows()) {
			checkProcessExistence("MPlayer", true, configuration.getTempFolder(), configuration.getMplayerPath(), "dummy");
		}
		logger.info("Done!");

		// check the existence of Vsfilter.dll
		if (registry.isAvis() && registry.getAvsPluginsDir() != null) {
			logger.info("Found AviSynth plugins dir: " + registry.getAvsPluginsDir().getAbsolutePath()); //$NON-NLS-1$
			File vsFilterdll = new File(registry.getAvsPluginsDir(), "VSFilter.dll"); //$NON-NLS-1$
			if (!vsFilterdll.exists()) {
				logger.info("VSFilter.dll is not in the AviSynth plugins directory. This can cause problems when trying to play subtitled videos with AviSynth"); //$NON-NLS-1$
			}
		}

		if (registry.getVlcv() != null && registry.getVlcp() != null) {
			logger.info("Found VideoLAN version " + registry.getVlcv() + " at: " + registry.getVlcp()); //$NON-NLS-1$ //$NON-NLS-2$
		}

		//check if Kerio is installed
		if (registry.isKerioFirewall()) {
			//todo: Warning message
		}

		// force use of specific dvr ms muxer when it's installed in the right place
		File dvrsMsffmpegmuxer = new File("win32/dvrms/ffmpeg_MPGMUX.exe"); //$NON-NLS-1$
		if (dvrsMsffmpegmuxer.exists()) {
			configuration.setFfmpegAlternativePath(dvrsMsffmpegmuxer.getAbsolutePath());
		}

		// disable jaudiotagger logging
		LogManager.getLogManager().readConfiguration(new ByteArrayInputStream("org.jaudiotagger.level=OFF".getBytes())); //$NON-NLS-1$

		// wrap System.err
		System.setErr(new PrintStream(new SystemErrWrapper(), true));

		extensions = new ArrayList<Format>();
		players = new ArrayList<Player>();
		allPlayers = new ArrayList<Player>();
		server = new HTTPServer(configuration.getServerPort());

		registerExtensions();

		/*
		 * XXX: keep this here (i.e. after registerExtensions and before registerPlayers) so that plugins
		 * can register custom players correctly (e.g. in the GUI) and/or add/replace custom formats
		 *
		 * XXX: if a plugin requires initialization/notification even earlier than
		 * this, then a new external listener implementing a new callback should be added
		 * e.g. StartupListener.registeredExtensions()
		 */
		try {
			ExternalFactory.lookup();
		} catch (Exception e) {
			logger.error("Error loading plugins", e);
		}

		// a static block in Player doesn't work (i.e. is called too late).
		// this must always be called *after* the plugins have loaded.
		// here's as good a place as any
		Player.initializeFinalizeTranscoderArgsListeners();
		registerPlayers();

		boolean binding = false;

		try {
			binding = server.start();
		} catch (BindException b) {
			logger.info("FATAL ERROR: Unable to bind on port: " + configuration.getServerPort() + ", because: " + b.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
			logger.info("Maybe another process is running or the hostname is wrong."); //$NON-NLS-1$
		}

		new Thread() {
			@Override
			public void run() {
				try {
					Thread.sleep(7000);
				} catch (InterruptedException e) {
				}
				boolean ps3found = false;
				for (RendererConfiguration r : foundRenderers) {
					if (r.isPS3()) {
						ps3found = true;
					}
				}
				if (!ps3found) {
					if (foundRenderers.isEmpty()) {
						frame.setStatusCode(0, Messages.getString("PMS.0"), "messagebox_critical-220.png"); //$NON-NLS-1$ //$NON-NLS-2$
					} else {
						frame.setStatusCode(0, Messages.getString("PMS.15"), "messagebox_warning-220.png"); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
			}
		}.start();

		if (!binding) {
			return false;
		}

		if (proxy > 0) {
			logger.info("Starting HTTP Proxy Server on port: " + proxy); //$NON-NLS-1$
			proxyServer = new ProxyServer(proxy);
		}

		// initialize the media library / cache
		if (configuration.getUseCache()) {
			initializeDatabase(); // XXX: this must be done *before* new MediaLibrary -> new MediaLibraryFolder
			mediaLibrary = new MediaLibrary();
			logger.info("A tiny media library admin interface is available at: http://" + server.getHost() + ":" + server.getPort() + "/console/home");
		}

		// XXX: this must be called:
		//     a) *after* loading plugins i.e. plugins register root folders then RootFolder.discoverChildren adds them
		//     b) *after* mediaLibrary is initialized, if enabled (above)
		getRootFolder(RendererConfiguration.getDefaultConf());

		frame.serverReady();

		//UPNPHelper.sendByeBye();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					for (ExternalListener l : ExternalFactory.getExternalListeners()) {
						l.shutdown();
					}
					UPNPHelper.shutDownListener();
					UPNPHelper.sendByeBye();
					logger.debug("Forcing shutdown of all active processes"); //$NON-NLS-1$
					for (Process p : currentProcesses) {
						try {
							p.exitValue();
						} catch (IllegalThreadStateException ise) {
							logger.trace("Forcing shutdown of process: " + p); //$NON-NLS-1$
							ProcessUtil.destroy(p);
						}
					}
					get().getServer().stop();
					Thread.sleep(500);
				} catch (Exception e) {
				}
			}
		});

		UPNPHelper.sendAlive();
		logger.trace("Waiting 250 milliseconds..."); //$NON-NLS-1$
		Thread.sleep(250);
		UPNPHelper.listen();

		return true;
	}
	
	private MediaLibrary mediaLibrary;

	/**Returns the MediaLibrary used by PMS.
	 * @return (MediaLibrary) Used mediaLibrary, if any. null if none is in use.
	 */
	public MediaLibrary getLibrary() {
		return mediaLibrary;
	}

	/**Executes the needed commands in order to make PMS a Windows service that starts whenever the machine is started.
	 * This function is called from the Network tab.
	 * @return true if PMS could be installed as a Windows service.
	 * @see GeneralTab#build()
	 */
	public boolean installWin32Service() {
		logger.info(Messages.getString("PMS.41")); //$NON-NLS-1$
		String cmdArray[] = new String[]{"win32/service/wrapper.exe", "-r", "wrapper.conf"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		OutputParams output = new OutputParams(configuration);
		output.noexitcheck = true;
		ProcessWrapperImpl pwuninstall = new ProcessWrapperImpl(cmdArray, output);
		pwuninstall.run();
		cmdArray = new String[]{"win32/service/wrapper.exe", "-i", "wrapper.conf"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		ProcessWrapperImpl pwinstall = new ProcessWrapperImpl(cmdArray, new OutputParams(configuration));
		pwinstall.run();
		return pwinstall.isSuccess();
	}

	/**Add a known set of extensions to the extensions list.
	 * @see PMS#init()
	 */
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

	/**Register a known set of audio/video transcoders (known as {@link Player}s). Used in PMS#init().
	 * @see PMS#init()
	 */
	private void registerPlayers() {
		if (Platform.isWindows()) {
			registerPlayer(new FFMpegVideo());
		}
		registerPlayer(new FFMpegAudio(configuration));
		registerPlayer(new MEncoderVideo(configuration));
		if (Platform.isWindows()) {
			registerPlayer(new MEncoderAviSynth(configuration));
		}
		registerPlayer(new MPlayerAudio(configuration));
		registerPlayer(new MEncoderWebVideo(configuration));
		registerPlayer(new MPlayerWebVideoDump(configuration));
		registerPlayer(new MPlayerWebAudio(configuration));
		registerPlayer(new TSMuxerVideo(configuration));
		registerPlayer(new TsMuxerAudio(configuration));
		registerPlayer(new VideoLanAudioStreaming(configuration));
		registerPlayer(new VideoLanVideoStreaming(configuration));
		if (Platform.isWindows()) {
			registerPlayer(new FFMpegDVRMSRemux());
		}
		registerPlayer(new RAWThumbnailer());
		frame.addEngines();
	}

	/**Adds a single {@link Player} to the list of Players. Used by {@link PMS#registerPlayers()}.
	 * @param p (Player) to be added to the list
	 * @see Player
	 * @see PMS#registerPlayers()
	 */
	public void registerPlayer(Player p) {
		allPlayers.add(p);
		boolean ok = false;
		if (Player.NATIVE.equals(p.executable())) {
			ok = true;
		} else {
			if (isWindows()) {
				if (p.executable() == null) {
					logger.info("Executable of transcoder profile " + p + " not found"); //$NON-NLS-1$ //$NON-NLS-2$
					return;
				}
				File executable = new File(p.executable());
				File executable2 = new File(p.executable() + ".exe"); //$NON-NLS-1$

				if (executable.exists() || executable2.exists()) {
					ok = true;
				} else {
					logger.info("Executable of transcoder profile " + p + " not found"); //$NON-NLS-1$ //$NON-NLS-2$
					return;
				}
				if (p.avisynth()) {
					ok = false;
					if (registry.isAvis()) {
						ok = true;
					} else {
						logger.info("Transcoder profile " + p + " will not be used because AviSynth was not found"); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
			} else if (!p.avisynth()) {
				ok = true;
			}
		}
		if (ok) {
			logger.info("Registering transcoding engine: " + p /*+ (p.avisynth()?(" with " + (forceMPlayer?"MPlayer":"AviSynth")):"")*/); //$NON-NLS-1$
			players.add(p);
		}
	}

	/**Transforms a comma separated list of directory entries into an array of {@link String}.
	 * Checks that the directory exists and is a valid directory.
	 * @param folders {@link String} Comma separated list of directories.
	 * @param log whether to output log information
	 * @return {@link File}[] Array of directories.
	 * @throws IOException
	 * @see {@link PMS#manageRoot(RendererConfiguration)}
	 */

	// this is called *way* too often (e.g. a dozen times with 1 renderer and 1 shared folder),
	// so log it by default so we can fix it.
	// BUT it's also called when the GUI is initialized (to populate the list of shared folders),
	// and we don't want this message to appear *before* the PMS banner, so allow that call to suppress logging
	public File[] loadFoldersConf(String folders, boolean log) throws IOException {
		if (folders == null || folders.length() == 0) {
			return null;
		}
		ArrayList<File> directories = new ArrayList<File>();
		String[] foldersArray = folders.split(","); //$NON-NLS-1$
		for (String folder : foldersArray) {
			// unescape embedded commas. note: backslashing isn't safe as it conflicts with
			// Windows path separators:
			// http://ps3mediaserver.org/forum/viewtopic.php?f=14&t=8883&start=250#p43520
			folder = folder.replaceAll("&comma;", ","); //$NON-NLS-1$ //$NON-NLS-2$
			if (log) {
				logger.trace("Checking shared folder: " + folder); //$NON-NLS-1$
			}
			File file = new File(folder);
			if (file.exists()) {
				if (!file.isDirectory()) {
					logger.warn("The file " + folder + " is not a directory! Please remove it from your Shared folders list on the Navigation/Share Settings tab"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			} else {
				logger.warn("The directory " + folder + " does not exist. Please remove it from your Shared folders list on the Navigation/Share Settings tab"); //$NON-NLS-1$ //$NON-NLS-2$
			}

			// add the file even if there are problems so that the user can update the shared folders as required.
			directories.add(file);
		}
		File f[] = new File[directories.size()];
		directories.toArray(f);
		return f;
	}

	/**Restarts the server. The trigger is either a button on the main PMS window or via
	 * an action item added via {@link PMS#addVideoSettingssFolder(RendererConfiguration).
	 * @throws IOException
	 */
	// XXX: don't try to optimize this by reusing the same server instance.
	// see the comment above HTTPServer.stop()
	public void reset() throws IOException {
		logger.trace("Waiting 1 second..."); //$NON-NLS-1$
		UPNPHelper.sendByeBye();
		server.stop();
		server = null;
		RendererConfiguration.resetAllRenderers();
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

	// Cannot remove these methods because of backwards compatibility;
	// none of the PMS code uses it, but some plugins still do.
	
	/**
	 * @deprecated Use the SLF4J logging API instead.
	 * Adds a message to the debug stream, or {@link System#out} in case the
	 * debug stream has not been set up yet.
	 * @param msg {@link String} to be added to the debug stream.
	 */
	public static void debug(String msg) {
		logger.trace(msg);
	}

	/**
	 * @deprecated Use the SLF4J logging API instead.
	 * Adds a message to the info stream.
	 * @param msg {@link String} to be added to the info stream.
	 */
	public static void info(String msg) {
		logger.debug(msg);
	}

	/**
	 * @deprecated Use the SLF4J logging API instead.
	 * Adds a message to the minimal stream. This stream is also
	 * shown in the Trace tab.
	 * @param msg {@link String} to be added to the minimal stream.
	 */
	public static void minimal(String msg) {
		logger.info(msg);
	}

	/**
	 * @deprecated Use the SLF4J logging API instead.
	 * Adds a message to the error stream. This is usually called by
	 * statements that are in a try/catch block.
	 * @param msg {@link String} to be added to the error stream
	 * @param t {@link Throwable} comes from an {@link Exception} 
	 */
	public static void error(String msg, Throwable t) {
		logger.error(msg, t);
	}
	
	/**Universally Unique Identifier used in the UPnP server.
	 * 
	 */
	private String uuid;

	/**Creates a new {@link #uuid} for the UPnP server to use. Tries to follow the RFCs for creating the UUID based on the link MAC address.
	 * Defaults to a random one if that method is not available.
	 * @return {@link String} with an Universally Unique Identifier.
	 */
	public String usn() {
		if (uuid == null) {
			boolean uuidBasedOnMAC = false;
			NetworkInterface ni = null;
			try {
				if (configuration.getServerHostname() != null && configuration.getServerHostname().length() > 0) {
					try {
						ni = NetworkInterface.getByInetAddress(InetAddress.getByName(configuration.getServerHostname()));
					} catch (Exception e) {
					}
				} else if (get().getServer().getNi() != null) {
					ni = get().getServer().getNi();
				}
				if (ni != null) {

					byte[] addr = PMSUtil.getHardwareAddress(ni); // return null when java.net.preferIPv4Stack=true
					if (addr != null) {
						uuid = UUID.nameUUIDFromBytes(addr).toString();
						uuidBasedOnMAC = true;
					} else {
						logger.info("Unable to retrieve MAC address for UUID creation: using a random one..."); //$NON-NLS-1$
					}
				}
			} catch (Throwable e) {
				logger.info("Switching to random UUID cause there's an error in getting UUID from MAC address: " + e.getMessage()); //$NON-NLS-1$
			}

			if (!uuidBasedOnMAC) {
				if (ni != null && (ni.getDisplayName() != null || ni.getName() != null)) {
					uuid = UUID.nameUUIDFromBytes((ni.getDisplayName() != null ? ni.getDisplayName() : (ni.getName() != null ? ni.getName() : "dummy")).getBytes()).toString(); //$NON-NLS-1$
				} else {
					uuid = UUID.randomUUID().toString();
				}
			}
			logger.info("Using the following UUID: " + uuid); //$NON-NLS-1$
		}
		return "uuid:" + uuid; //$NON-NLS-1$ //$NON-NLS-2$
		//return "uuid:1234567890TOTO::";
	}

	/**Returns the user friendly name of the UPnP server. 
	 * @return {@link String} with the user friendly name.
	 */
	public String getServerName() {
		if (serverName == null) {
			StringBuilder sb = new StringBuilder();
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

	/**Returns the PMS instance.
	 * @return {@link PMS}
	 */
	public static PMS get() {
		assert instance != null;
		return instance;
	}

	private static void createInstance() {
		instance = new PMS();

		try {
			if (instance.init()) {
				logger.info("The server should now appear on your renderer"); //$NON-NLS-1$
			} else {
				logger.error("A serious error occurred during PMS init"); //$NON-NLS-1$
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param filename
	 * @return
	 */
	public Format getAssociatedExtension(String filename) {
		logger.trace("Search extension for " + filename); //$NON-NLS-1$
		for (Format ext : extensions) {
			if (ext.match(filename)) {
				logger.trace("Found 1! " + ext.getClass().getName()); //$NON-NLS-1$
				return ext.duplicate();
			}
		}
		return null;
	}

	public Player getPlayer(Class<? extends Player> profileClass, Format ext) {
		for (Player p : players) {
			if (p.getClass().equals(profileClass) && p.type() == ext.getType() && !p.excludeFormat(ext)) {
				return p;
			}
		}
		return null;
	}

	public ArrayList<Player> getPlayers(ArrayList<Class<? extends Player>> profileClasses, int type) {
		ArrayList<Player> compatiblePlayers = new ArrayList<Player>();
		for (Player p : players) {
			if (profileClasses.contains(p.getClass()) && p.type() == type) {
				compatiblePlayers.add(p);
			}
		}
		return compatiblePlayers;
	}

	public static void main(String args[]) throws IOException, ConfigurationException {
		boolean displayProfileChooser = false;
		boolean headless = true;

		if (args.length > 0) {
			for (int a = 0; a < args.length; a++) {
				if (args[a].equals(CONSOLE)) {
					System.setProperty(CONSOLE, Boolean.toString(true));
				} else if (args[a].equals(NATIVELOOK)) {
					System.setProperty(NATIVELOOK, Boolean.toString(true));
				} else if (args[a].equals(SCROLLBARS)) {
					System.setProperty(SCROLLBARS, Boolean.toString(true));
				} else if (args[a].equals(NOCONSOLE)) {
					System.setProperty(NOCONSOLE, Boolean.toString(true));
				} else if (args[a].equals(PROFILES)) {
					displayProfileChooser = true;
				}
			}
		}

		try {
			Toolkit.getDefaultToolkit();
			if (GraphicsEnvironment.isHeadless()) {
				if (System.getProperty(NOCONSOLE) == null) {
					System.setProperty(CONSOLE, Boolean.toString(true));
				}
			} else {
				headless = false;
			}
		} catch (Throwable t) {
			System.err.println("Toolkit error: " + t.getMessage());
			if (System.getProperty(NOCONSOLE) == null) {
				System.setProperty(CONSOLE, Boolean.toString(true));
			}
		}

		if (!headless && displayProfileChooser) {
			ProfileChooser.display();
		}

		try {
			configuration = new PmsConfiguration();
		} catch (Throwable t) {
			System.err.println("Configuration error: " + t.getMessage());
		}

		assert configuration != null;

		// Load the (optional) logback config file. This has to be called after 'new PmsConfiguration'
		// as the logging starts immediately and some filters need the PmsConfiguration.
		LoggingConfigFileLoader.load();

		// create the PMS instance returned by get()
		createInstance(); 

		try {
			// let's allow us time to show up serious errors in the GUI before quitting
			Thread.sleep(60000);
		} catch (InterruptedException e) {}
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
			logger.error("Could not save configuration", e); //$NON-NLS-1$
		}
	}

	public static PmsConfiguration getConfiguration() {
		return configuration;
	}
}
