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
package net.pms.encoders;

import java.awt.ComponentOrientation;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.pms.Messages;
import net.pms.configuration.PmsConfiguration;
import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAResource;
import net.pms.formats.Format;
import net.pms.io.OutputParams;
import net.pms.io.PipeProcess;
import net.pms.io.ProcessWrapper;
import net.pms.io.ProcessWrapperImpl;
import net.pms.network.HTTPResource;
import net.pms.util.FileUtil;
import net.pms.util.FormLayoutUtil;
import net.pms.util.PlayerUtil;
import net.pms.util.ProcessUtil;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.sun.jna.Platform;

// FIXME (breaking change): VLCWebVideo doesn't customize any of this, so everything should be *private*
// TODO (when transcoding to MPEG-2): handle non-MPEG-2 compatible input framerates

/**
 * Use VLC as a backend transcoder. Note that 0.x and 1.x versions are
 * unsupported (and probably will crash). Only the latest version will be
 * supported
 *
 * @author Leon Blakey <lord.quackstar@gmail.com>
 */
public class VLCVideo extends Player {
	private static final Logger logger = LoggerFactory.getLogger(VLCVideo.class);
	protected final PmsConfiguration configuration;
	public static final String ID = "vlctranscoder";
	protected JTextField scale;
	protected JCheckBox experimentalCodecs;
	protected JCheckBox audioSyncEnabled;
	protected JTextField sampleRate;
	protected JCheckBox sampleRateOverride;
	protected JTextField extraParams;

	public VLCVideo(PmsConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public PlayerPurpose getPurpose() {
		return PlayerPurpose.VIDEO_FILE_PLAYER;
	}

	@Override
	public String id() {
		return ID;
	}

	@Override
	public boolean isTimeSeekable() {
		return true;
	}

	@Override
	public String[] args() {
		return new String[]{};
	}

	@Override
	public String name() {
		return "VLC Video";
	}

	@Override
	public int type() {
		return Format.VIDEO;
	}

	@Override
	public String mimeType() {
		// I think?
		return HTTPResource.VIDEO_TRANSCODE;
	}

	@Override
	public String executable() {
		return configuration.getVlcPath();
	}

	@Override
	public boolean isCompatible(DLNAResource resource) {
		// only handle local video - web video is handled by VLCWebVideo
		return PlayerUtil.isVideo(resource)
			&& !PlayerUtil.isWebVideo(resource);
	}

	/**
	 * Pick codecs for VLC based on formats the renderer supports.
	 *
	 * @param renderer The {@link RendererConfiguration}.
	 * @return The codec configuration
	 */
	protected CodecConfig genConfig(RendererConfiguration renderer) {
		CodecConfig codecConfig = new CodecConfig();
		if (renderer.isTranscodeToWMV()) {
			// Assume WMV = XBox = all media renderers with this flag
			logger.debug("Using XBox WMV codecs");
			codecConfig.videoCodec = "wmv2";
			codecConfig.audioCodec = "wma";
			codecConfig.container = "asf";
		} else { // Default codecs for DLNA standard
			codecConfig.videoCodec = "mp2v";
			// XXX a52 (AC-3) causes the audio to cut out after
			// a while (5, 10, and 45 minutes have been spotted)
			// with versions as recent as 2.0.5. MP2 works without
			// issue, so we use that as a workaround for now.
			// codecConfig.audioCodec = "a52";
			codecConfig.audioCodec = "mp2a";

			if (renderer.isTranscodeToMPEGTSAC3()) {
				logger.debug("Using standard DLNA codecs with an MPEG-PS container");
				codecConfig.container = "ts";
			} else {
				logger.debug("Using standard DLNA codecs with an MPEG-TS (default) container");
				codecConfig.container = "ps";
			}
		}

		logger.trace("Using " + codecConfig.videoCodec + ", " + codecConfig.audioCodec + ", " + codecConfig.container);

		// Audio sample rate handling
		if (sampleRateOverride.isSelected()) {
			codecConfig.sampleRate = Integer.valueOf(sampleRate.getText());
		}

		// This has caused garbled audio, so only enable when told to
		if (audioSyncEnabled.isSelected()) {
			codecConfig.extraTrans.put("audio-sync", "");
		}

		return codecConfig;
	}

	protected static class CodecConfig {
		String videoCodec;
		String audioCodec;
		String container;
		String extraParams;
		HashMap<String, Object> extraTrans = new HashMap<String, Object>();
		int sampleRate;
	}

	protected Map<String, Object> getEncodingArgs(CodecConfig codecConfig) {
		// See: http://www.videolan.org/doc/streaming-howto/en/ch03.html
		// See: http://wiki.videolan.org/Codec
		Map<String, Object> args = new HashMap<String, Object>();

		// Codecs to use
		args.put("vcodec", codecConfig.videoCodec);
		args.put("acodec", codecConfig.audioCodec);

		// Bitrate in kbit/s (TODO: Use global option?)
		args.put("vb", "4096");
		args.put("ab", "128");

		// Video scaling
		args.put("scale", scale.getText());

		// Audio Channels
		args.put("channels", 2);

		// Static sample rate
		args.put("samplerate", codecConfig.sampleRate);

		// Recommended on VLC DVD encoding page
		args.put("keyint", 16);

		// Recommended on VLC DVD encoding page
		args.put("strict-rc", null);

		// Stream subtitles to client
		// args.add("scodec=dvbs");
		// args.add("senc=dvbsub");

		// Hardcode subtitles into video
		args.put("soverlay", null);

		// enable multi-threading
		args.put("threads", "" + configuration.getNumberOfCpuCores());

		// Add extra args
		args.putAll(codecConfig.extraTrans);

		return args;
	}

	@Override
	public ProcessWrapper launchTranscode(
		DLNAResource dlna,
		DLNAMediaInfo media,
		OutputParams params
	) throws IOException {
		final String filename = dlna.getSystemName();
		boolean isWindows = Platform.isWindows();
		setAudioAndSubs(filename, media, params, configuration);

		// Make sure we can play this
		CodecConfig codecConfig = genConfig(params.mediaRenderer);

		PipeProcess tsPipe = new PipeProcess("VLC" + System.currentTimeMillis() + "." + codecConfig.container);
		ProcessWrapper pipe_process = tsPipe.getPipeProcess();

		// XXX it can take a long time for Windows to create a named pipe
		// (and mkfifo can be slow if /tmp isn't memory-mapped), so start this as early as possible
		pipe_process.runInNewThread();
		tsPipe.deleteLater();

		params.input_pipes[0] = tsPipe;
		params.minBufferSize = params.minFileSize;
		params.secondread_minsize = 100000;

		List<String> cmdList = new ArrayList<String>();
		cmdList.add(executable());
		cmdList.add("-I");
		cmdList.add("dummy");

		// XXX hardware acceleration causes issues with some videos
		// on VLC 2.0.5, so disable it by default.
		// Note: it's enabled by default in 2.0.5 (and possibly
		// earlier), so, if not enabled, it needs to be explicitly
		// disabled

		// These options do not exist in VLC 2.0.7 on Mac OS X
		if (!Platform.isMac()) {
			if (configuration.isVideoHardwareAcceleration()) {
				logger.warn("VLC hardware acceleration support is an experimental feature. Please disable it before reporting issues.");
				cmdList.add("--ffmpeg-hw");
			} else {
				cmdList.add("--no-ffmpeg-hw");
			}
		}

		// Useful for the more esoteric codecs people use
		if (experimentalCodecs.isSelected()) {
			cmdList.add("--sout-ffmpeg-strict=-2");
		}

		// Stop the DOS box from appearing on windows
		if (isWindows) {
			cmdList.add("--dummy-quiet");
		}

		// File needs to be given before sout, otherwise vlc complains
		cmdList.add(filename);

		// FIXME not sure what this hack is trying to do, but it results in no audio and no subtitles
		// Huge fake track id that shouldn't conflict with any real subtitle or audio id. Hopefully.
		String disableSuffix = "track=214748361";

		// Handle audio language
		if (params.aid != null) {
			// User specified language at the client, acknowledge it
			if (params.aid.getLang() == null || params.aid.getLang().equals("und")) {
				// VLC doesn't understand "und", so try to get audio track by ID
				cmdList.add("--audio-track=" + params.aid.getId());
			} else {
				cmdList.add("--audio-language=" + params.aid.getLang());
			}
		} else {
			// Not specified, use language from GUI
			// FIXME: VLC does not understand "loc" or "und".
			cmdList.add("--audio-language=" + configuration.getAudioLanguages());
		}

		// Handle subtitle language
		if (params.sid != null) { // User specified language at the client, acknowledge it
			if (params.sid.isExternal()) {
				String externalSubtitlesFileName = null;

				// External subtitle file
				if (params.sid.isExternalFileUtf16()) {
					try {
						// Convert UTF-16 -> UTF-8
						File convertedSubtitles = new File(configuration.getTempFolder(), "utf8_" + params.sid.getExternalFile().getName());
						FileUtil.convertFileFromUtf16ToUtf8(params.sid.getExternalFile(), convertedSubtitles);
						externalSubtitlesFileName = ProcessUtil.getShortFileNameIfWideChars(convertedSubtitles.getAbsolutePath());
					} catch (IOException e) {
						logger.debug("Error converting file from UTF-16 to UTF-8", e);
						externalSubtitlesFileName = ProcessUtil.getShortFileNameIfWideChars(params.sid.getExternalFile().getAbsolutePath());
					}
				} else {
					externalSubtitlesFileName = ProcessUtil.getShortFileNameIfWideChars(params.sid.getExternalFile().getAbsolutePath());
				}

				if (externalSubtitlesFileName != null) {
					cmdList.add("--sub-file");
					cmdList.add(externalSubtitlesFileName);
				}
			}
			else if (params.sid.getLang() != null && !params.sid.getLang().equals("und")) { // Load by ID (better)
				cmdList.add("--sub-track=" + params.sid.getId());
			} else { // VLC doesn't understand "und", but does understand a nonexistent track
				cmdList.add("--sub-" + disableSuffix);
			}
		} else if (!configuration.isDisableSubtitles()) { // Not specified, use language from GUI if enabled
			// FIXME: VLC does not understand "loc" or "und".
			cmdList.add("--sub-language=" + configuration.getSubtitlesLanguages());
		} else {
			cmdList.add("--sub-" + disableSuffix);
		}

		// Skip forward if necessary
		if (params.timeseek != 0) {
			cmdList.add("--start-time");
			cmdList.add(String.valueOf(params.timeseek));
		}

		// Generate encoding args
		String separator = "";
		StringBuilder encodingArgsBuilder = new StringBuilder();

		for (Map.Entry<String, Object> curEntry : getEncodingArgs(codecConfig).entrySet()) {
			encodingArgsBuilder.append(separator);
			encodingArgsBuilder.append(curEntry.getKey());

			if (curEntry.getValue() != null) {
				encodingArgsBuilder.append("=");
				encodingArgsBuilder.append(curEntry.getValue());
			}

			separator = ",";
		}

		// Add our transcode options
		String transcodeSpec = String.format(
				"#transcode{%s}:standard{access=file,mux=%s,dst='%s%s'}",
				encodingArgsBuilder.toString(),
				codecConfig.container,
				(isWindows ? "\\\\" : ""),
				tsPipe.getInputPipe());
		cmdList.add("--sout");
		cmdList.add(transcodeSpec);

		// Force VLC to exit when finished
		cmdList.add("vlc://quit");

		// Add any extra parameters
		if (!extraParams.getText().trim().isEmpty()) { // Add each part as a new item
			cmdList.addAll(Arrays.asList(StringUtils.split(extraParams.getText().trim(), " ")));
		}

		// Pass to process wrapper
		String[] cmdArray = new String[cmdList.size()];
		cmdList.toArray(cmdArray);
		cmdArray = finalizeTranscoderArgs(filename, dlna, media, params, cmdArray);
		logger.trace("Finalized args: " + StringUtils.join(cmdArray, " "));
		ProcessWrapperImpl pw = new ProcessWrapperImpl(cmdArray, params);
		pw.attachProcess(pipe_process);

		// TODO: Why is this here?
		try {
			Thread.sleep(150);
		} catch (InterruptedException e) {
		}

		pw.runInNewThread();
		return pw;
	}

	@Override
	public JComponent config() {
		// Apply the orientation for the locale
		Locale locale = new Locale(configuration.getLanguage());
		ComponentOrientation orientation = ComponentOrientation.getOrientation(locale);
		String colSpec = FormLayoutUtil.getColSpec("right:pref, 3dlu, pref:grow, 7dlu, right:pref, 3dlu, pref:grow", orientation);
		FormLayout layout = new FormLayout(colSpec, "");
		// Here goes my 3rd try to learn JGoodies Form
		layout.setColumnGroups(new int[][]{{1, 5}, {3, 7}});
		DefaultFormBuilder mainPanel = new DefaultFormBuilder(layout);

		mainPanel.appendSeparator(Messages.getString("VlcTrans.1"));
		mainPanel.append(experimentalCodecs = new JCheckBox(Messages.getString("VlcTrans.3"), configuration.isVlcExperimentalCodecs()), 3);
		experimentalCodecs.setContentAreaFilled(false);
		experimentalCodecs.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				configuration.setVlcExperimentalCodecs(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		mainPanel.append(audioSyncEnabled = new JCheckBox(Messages.getString("VlcTrans.4"), configuration.isVlcAudioSyncEnabled()), 3);
		audioSyncEnabled.setContentAreaFilled(false);
		audioSyncEnabled.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				configuration.setVlcAudioSyncEnabled(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		mainPanel.nextLine();

		// Developer stuff. Theoretically is temporary 
		mainPanel.appendSeparator(Messages.getString("VlcTrans.10"));

		// Add scale as a subpanel because it has an awkward layout
		mainPanel.append(Messages.getString("VlcTrans.11"));
		FormLayout scaleLayout = new FormLayout("pref,3dlu,pref", "");
		DefaultFormBuilder scalePanel = new DefaultFormBuilder(scaleLayout);
		double startingScale = Double.valueOf(configuration.getVlcScale());
		scalePanel.append(scale = new JTextField(String.valueOf(startingScale)));
		final JSlider scaleSlider = new JSlider(JSlider.HORIZONTAL, 0, 10, (int) (startingScale * 10));
		scalePanel.append(scaleSlider);
		scaleSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent ce) {
				String value = String.valueOf((double) scaleSlider.getValue() / 10);
				scale.setText(value);
				configuration.setVlcScale(value);
			}
		});
		scale.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				String typed = scale.getText();
				if (!typed.matches("\\d\\.\\d")) {
					return;
				}
				double value = Double.parseDouble(typed);
				scaleSlider.setValue((int) (value * 10));
				configuration.setVlcScale(String.valueOf(value));
			}
		});
		mainPanel.append(scalePanel.getPanel(), 3);

		// Audio sample rate
		FormLayout sampleRateLayout = new FormLayout("right:pref, 3dlu, right:pref, 3dlu, right:pref, 3dlu, left:pref", "");
		DefaultFormBuilder sampleRatePanel = new DefaultFormBuilder(sampleRateLayout);
		sampleRateOverride = new JCheckBox(Messages.getString("VlcTrans.17"), configuration.getVlcSampleRateOverride());
		sampleRatePanel.append(Messages.getString("VlcTrans.18"), sampleRateOverride);
		sampleRate = new JTextField(configuration.getVlcSampleRate(), 8);
		sampleRate.setEnabled(configuration.getVlcSampleRateOverride());
		sampleRate.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				configuration.setVlcSampleRate(sampleRate.getText());
			}
		});
		sampleRatePanel.append(Messages.getString("VlcTrans.19"), sampleRate);
		sampleRateOverride.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				boolean checked = e.getStateChange() == ItemEvent.SELECTED;
				configuration.setVlcSampleRateOverride(checked);
				sampleRate.setEnabled(checked);
			}
		});

		mainPanel.nextLine();
		mainPanel.append(sampleRatePanel.getPanel(), 7);

		// Extra options
		mainPanel.nextLine();
		mainPanel.append(Messages.getString("VlcTrans.20"), extraParams = new JTextField(), 5);

		return mainPanel.getPanel();
	}
}
