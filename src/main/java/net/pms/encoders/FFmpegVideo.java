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

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import net.pms.Messages;
import net.pms.PMS;
import net.pms.configuration.PmsConfiguration;
import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAMediaSubtitle;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.InputFile;
import net.pms.formats.Format;
import net.pms.formats.v2.SubtitleUtils;
import net.pms.io.*;
import net.pms.network.HTTPResource;
import net.pms.util.PlayerUtil;
import net.pms.util.ProcessUtil;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.*;

import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/*
 * Pure FFmpeg video player.
 *
 * Design note:
 *
 * Helper methods that return lists of <code>String</code>s representing
 * options are public to facilitate composition e.g. a custom engine (plugin)
 * that uses tsMuxeR for videos without subtitles and FFmpeg otherwise needs to
 * compose and call methods on both players.
 *
 * To avoid API churn, and to provide wiggle room for future functionality, all
 * of these methods take the same arguments as launchTranscode (and the same
 * first four arguments as finalizeTranscoderArgs) even if one or more of the
 * parameters are unused e.g.:
 *
 *     public List<String> getAudioBitrateOptions(
 *         DLNAResource dlna,
 *         DLNAMediaInfo media,
 *         OutputParams params
 *     )
 */
public class FFmpegVideo extends FFmpegBase {
	private static final Logger logger = LoggerFactory.getLogger(FFmpegVideo.class);
	private static final String DEFAULT_QSCALE = "3";
	private static final String SUB_DIR = "subs";
	private static PmsConfiguration configuration;

	private boolean dtsRemux;
	private boolean ac3Remux;
	private boolean videoRemux;

	private JCheckBox multiThreadingCheckBox;
	private JCheckBox videoRemuxCheckBox;

	@Deprecated
	public FFmpegVideo() {
		this(PMS.getConfiguration());
	}
	
	public FFmpegVideo(PmsConfiguration configuration) {
		super(configuration);
		this.configuration = configuration;
	}

	// FIXME we have an id() accessor for this; no need for the field to be public
	@Deprecated
	public static final String ID = "ffmpegvideo";

	/**
	 * Returns a list of strings representing the rescale options for this transcode i.e. the ffmpeg -vf
	 * options used to show subtitles in SSA/ASS format and resize a video that's too wide and/or high for the specified renderer.
	 * If the renderer has no size limits, or there's no media metadata, or the video is within the renderer's
	 * size limits, an empty list is returned.
	 *
	 * @param dlna The DLNA resource representing the file being transcoded.
	 * @param media the media metadata for the video being streamed. May contain unset/null values (e.g. for web videos).
	 * @param params The {@link net.pms.io.OutputParams} context object used to store miscellaneous parameters for this request.
	 * @return a {@link List} of <code>String</code>s representing the rescale options for this video,
	 * or an empty list if the video doesn't need to be resized.
	 */
	public List<String> getVideoFilterOptions(DLNAResource dlna, DLNAMediaInfo media, OutputParams params) throws IOException {
		List<String> options = new ArrayList<String>();
		String subsOption = null;
		String padding = null;
		final RendererConfiguration renderer = params.mediaRenderer;

		DLNAMediaSubtitle tempSubs = null;
		if (!isDisableSubtitles(params)) {
			tempSubs = getSubtitles(params);
		}

		final boolean isResolutionTooHighForRenderer = renderer.isVideoRescale() // renderer defines a max width/height
				&& (media != null && media.isMediaparsed())
				&& ((media.getWidth() > renderer.getMaxVideoWidth())
					|| (media.getHeight() > renderer.getMaxVideoHeight()));

		if (tempSubs != null) {
			StringBuilder s = new StringBuilder();
			CharacterIterator it = new StringCharacterIterator(ProcessUtil.getShortFileNameIfWideChars(tempSubs.getExternalFile().getAbsolutePath()));

			for (char ch = it.first(); ch != CharacterIterator.DONE; ch = it.next()) {
				switch (ch) {
					case ':':
						s.append("\\\\:");
						break;
					case '\\':
						s.append("/");
						break;
					case ']':
						s.append("\\]");
						break;
					case '[':
						s.append("\\[");
						break;
					default:
						s.append(ch);
				}
			}

			String subsFile = s.toString();
			subsFile = subsFile.replace(",", "\\,");
			subsOption = "subtitles=" + subsFile;
		}

		if (renderer.isPadVideoWithBlackBordersTo169AR() && renderer.isRescaleByRenderer()) {
			if (media != null
				&& media.isMediaparsed()
				&& media.getHeight() != 0
				&& (media.getWidth() / (double) media.getHeight()) >= (16 / (double) 9)) {
				padding = "pad=iw:iw/(16/9):0:(oh-ih)/2";
			} else {
				padding = "pad=ih*(16/9):ih:(ow-iw)/2:0";
			}
		}

		String rescaleSpec = null;

		if (isResolutionTooHighForRenderer || (renderer.isPadVideoWithBlackBordersTo169AR() && !renderer.isRescaleByRenderer())) {
			rescaleSpec = String.format(
				// http://stackoverflow.com/a/8351875
				"scale=iw*min(%1$d/iw\\,%2$d/ih):ih*min(%1$d/iw\\,%2$d/ih),pad=%1$d:%2$d:(%1$d-iw)/2:(%2$d-ih)/2",
				renderer.getMaxVideoWidth(),
				renderer.getMaxVideoHeight()
			);
		}

		String overrideVF = renderer.getFFmpegVideoFilterOverride();

		if (rescaleSpec != null || padding != null || overrideVF != null || subsOption != null) {
			options.add("-vf");
			StringBuilder filterParams = new StringBuilder();

			if (overrideVF != null) {
				filterParams.append(overrideVF);
				if (subsOption != null) {
					filterParams.append(", ");
				}
			} else {
				if (rescaleSpec != null) {
					filterParams.append(rescaleSpec);
					if (subsOption != null || padding != null) {
						filterParams.append(", ");
					}
				}

				if (padding != null && rescaleSpec == null) {
					filterParams.append(padding);
					if (subsOption != null) {
						filterParams.append(", ");
					}
				}
			}

			if (subsOption != null) {
				filterParams.append(subsOption);
			}

			options.add(filterParams.toString());
		}

		return options;
	}

	/**
	 * Returns a list of <code>String</code>s representing ffmpeg output
	 * options (i.e. options that define the output file's video codec,
	 * audio codec and container) compatible with the renderer's
	 * <code>TranscodeVideo</code> profile.
	 *
	 * @param dlna The DLNA resource representing the file being transcoded.
	 * @param media the media metadata for the video being streamed. May contain unset/null values (e.g. for web videos).
	 * @param params The {@link net.pms.io.OutputParams} context object used to store miscellaneous parameters for this request.
	 * @return a {@link List} of <code>String</code>s representing the FFmpeg output parameters for the renderer according
	 * to its <code>TranscodeVideo</code> profile.
	 */
	public synchronized List<String> getVideoTranscodeOptions(DLNAResource dlna, DLNAMediaInfo media, OutputParams params) {
		List<String> options = new ArrayList<String>();
		final String filename = dlna.getSystemName();
		final RendererConfiguration renderer = params.mediaRenderer;

		if (renderer.isTranscodeToWMV() && !renderer.isXBOX()) { // WMV
			options.add("-c:v");
			options.add("wmv2");

			options.add("-c:a");
			options.add("wmav2");

			options.add("-f");
			options.add("asf");
		} else { // MPEGPSAC3, MPEGTSAC3 or H264TSAC3
			if (isAc3Remux()) {
				// AC-3 remux
				options.add("-c:a");
				options.add("copy");
			} else if (isDtsRemux()) {
				// Audio is added in a separate process later
				options.add("-an");
			} else if (type() == Format.AUDIO) {
				// Skip
			} else {
				options.add("-c:a");
				options.add("ac3");
			}

			InputFile newInput = null;
			if (filename != null) {
				newInput = new InputFile();
				newInput.setFilename(filename);
				newInput.setPush(params.stdin);
			}

			// Output video codec
			if (media.isMediaparsed()
					&& params.sid == null
					&& ((newInput != null && media.isVideoWithinH264LevelLimits(newInput, params.mediaRenderer))
						|| !params.mediaRenderer.isH264Level41Limited())
					&& media.isMuxable(params.mediaRenderer)
					&& configuration.isFFmpegMuxWhenCompatible()
					&& params.mediaRenderer.isMuxH264MpegTS()) {

				options.add("-c:v");
				options.add("copy");
				options.add("-bsf");
				options.add("h264_mp4toannexb");
				options.add("-fflags");
				options.add("+genpts");
				// Set correct container aspect ratio if remuxed video track has different AR
				// TODO does not work with ffmpeg 1.2
				// https://ffmpeg.org/trac/ffmpeg/ticket/2046
				// possible solution http://forum.doom9.org/showthread.php?t=152419
				//
				// if (media.isAspectRatioMismatch()) {
				//	options.add("-aspect");
				//	options.add(media.getAspectRatioContainer());
				// }

				setVideoRemux(true);
			} else if (renderer.isTranscodeToH264TSAC3()) {
				options.add("-c:v");
				options.add("libx264");
				options.add("-crf");
				options.add("20");
				options.add("-preset");
				options.add("superfast");
			} else if (!isDtsRemux()) {
				options.add("-c:v");
				options.add("mpeg2video");
			}

			// Output file format
			options.add("-f");
			if (isDtsRemux()) {
				if (isVideoRemux()) {
					options.add("rawvideo");
				} else {
					options.add("mpeg2video");
				}
			} else if (renderer.isTranscodeToMPEGTSAC3() || renderer.isTranscodeToH264TSAC3() || isVideoRemux()) { // MPEGTSAC3
				options.add("mpegts");
			} else { // default: MPEGPSAC3
				options.add("vob");
			}
		}

		return options;
	}

	/**
	 * Returns the video bitrate spec for the current transcode according
	 * to the limits/requirements of the renderer.
	 *
	 * @param dlna The DLNA resource representing the file being transcoded.
	 * @param media the media metadata for the video being streamed. May contain unset/null values (e.g. for web videos).
	 * @param params The {@link net.pms.io.OutputParams} context object used to store miscellaneous parameters for this request.
	 * @return a {@link List} of <code>String</code>s representing the video bitrate options for this transcode
	 */
	public List<String> getVideoBitrateOptions(DLNAResource dlna, DLNAMediaInfo media, OutputParams params) { // media is currently unused
		List<String> options = new ArrayList<String>();
		String sMaxVideoBitrate = params.mediaRenderer.getMaxVideoBitrate(); // currently Mbit/s
		int iMaxVideoBitrate = 0;

		if (sMaxVideoBitrate != null) {
			try {
				iMaxVideoBitrate = Integer.parseInt(sMaxVideoBitrate);
			} catch (NumberFormatException nfe) {
				logger.error("Can't parse max video bitrate", nfe); // XXX this should be handled in RendererConfiguration
			}
		}

		if (iMaxVideoBitrate == 0) { // unlimited: try to preserve the bitrate
			options.add("-q:v"); // video qscale
			options.add(DEFAULT_QSCALE);
		} else { // limit the bitrate FIXME untested
			// convert megabits-per-second (as per the current option name: MaxVideoBitrateMbps) to bps
			// FIXME rather than dealing with megabit vs mebibit issues here, this should be left up to the client i.e.
			// the renderer.conf unit should be bits-per-second (and the option should be renamed: MaxVideoBitrateMbps -> MaxVideoBitrate)
			options.add("-maxrate");
			options.add("" + iMaxVideoBitrate * 1000 * 1000);
		}

		return options;
	}

	/**
	 * Returns the audio bitrate spec for the current transcode according
	 * to the limits/requirements of the renderer.
	 *
	 * @param dlna The DLNA resource representing the file being transcoded.
	 * @param media the media metadata for the video being streamed. May contain unset/null values (e.g. for web videos).
	 * @param params The {@link net.pms.io.OutputParams} context object used to store miscellaneous parameters for this request.
	 * @return a {@link List} of <code>String</code>s representing the audio bitrate options for this transcode
	 */
	public List<String> getAudioBitrateOptions(DLNAResource dlna, DLNAMediaInfo media, OutputParams params) {
		List<String> options = new ArrayList<String>();

		options.add("-q:a");
		options.add(DEFAULT_QSCALE);

		return options;
	}

	/**
	 * Returns the audio channel (-ac) options.
	 *
	 * @param dlna The DLNA resource representing the file being transcoded.
	 * @param media the media metadata for the file being transcoded. May contain null fields (e.g. for web videos).
	 * @param params The {@link net.pms.io.OutputParams} context object used to store miscellaneous parameters for this request.
	 * @return The list of audio channel options.
	 * @since 1.81.0
	 */

	public List<String> getAudioChannelOptions(DLNAResource dlna, DLNAMediaInfo media, OutputParams params) {
		List<String> options = new ArrayList<String>();
		int ac = -1; // -1: don't change the number of audio channels
		int nChannels = params.aid == null ? -1 : params.aid.getAudioProperties().getNumberOfChannels();

		if (nChannels == -1) { // unknown (e.g. web video)
			ac = 2; // works fine if the video has < 2 channels
		} else if (nChannels > 2) {
			int maxOutputChannels = configuration.getAudioChannelCount();

			if (maxOutputChannels <= 2) {
				ac = maxOutputChannels;
			} else if (params.mediaRenderer.isTranscodeToWMV()) {
				// http://www.ps3mediaserver.org/forum/viewtopic.php?f=6&t=16590
				// XXX WMA Pro (wmapro) supports > 2 channels, but ffmpeg doesn't have an encoder for it
				ac = 2;
			}
		}

		if (ac != -1) {
			options.add("-ac");
			options.add("" + ac);
		}

		return options;
	}

	@Override
	public PlayerPurpose getPurpose() {
		return PlayerPurpose.VIDEO_FILE_PLAYER;
	}

	@Override
	// TODO make this static so it can replace ID, instead of having both
	public String id() {
		return ID;
	}

	@Override
	public boolean isTimeSeekable() {
		return true;
	}

	public String initialString() {
		String threads = "";
		if (configuration.isFfmpegMultithreading()) {
			threads = " -threads " + configuration.getNumberOfCpuCores();
		}
		return threads;
	}

	@Override
	public String name() {
		return "FFmpeg";
	}

	@Override
	public int type() {
		return Format.VIDEO;
	}

	// unused; return this array for backwards-compatibility
	@Deprecated
	protected String[] getDefaultArgs() {
		List<String> defaultArgsList = new ArrayList<String>();

		defaultArgsList.add("-loglevel");
		defaultArgsList.add("warning");

		String[] defaultArgsArray = new String[defaultArgsList.size()];
		defaultArgsList.toArray(defaultArgsArray);

		return defaultArgsArray;
	}

	private int[] getVideoBitrateConfig(String bitrate) {
		int bitrates[] = new int[2];

		if (bitrate.contains("(") && bitrate.contains(")")) {
			bitrates[1] = Integer.parseInt(bitrate.substring(bitrate.indexOf("(") + 1, bitrate.indexOf(")")));
	}

		if (bitrate.contains("(")) {
			bitrate = bitrate.substring(0, bitrate.indexOf("(")).trim();
		}

		if (isBlank(bitrate)) {
			bitrate = "0";
		}

		bitrates[0] = (int) Double.parseDouble(bitrate);

		return bitrates;
	}

	@Override
	@Deprecated
	public String[] args() {
		return getDefaultArgs(); // unused; return this array for for backwards compatibility
	}

	@Override
	public String mimeType() {
		return HTTPResource.VIDEO_TRANSCODE;
	}

	// FIXME this is a mess: the whole point of the getXOptions methods is to prevent
	// this turning into another MEncoderVideo, with disorganised kitchen-sink methods
	// that are over a thousand lines long.
	//
	// TODO: move each chunk of functionality into submethods called by a core group of
	// getXOptions methods
	@Override
	public synchronized ProcessWrapper launchTranscode(
		DLNAResource dlna,
		DLNAMediaInfo media,
		OutputParams params
	) throws IOException {
		int nThreads = configuration.getNumberOfCpuCores();
		List<String> cmdList = new ArrayList<String>();
		RendererConfiguration renderer = params.mediaRenderer;
		final String filename = dlna.getSystemName();
		setAudioAndSubs(filename, media, params, configuration);
		params.waitbeforestart = 2500;

		cmdList.add(executable());
		cmdList.addAll(getGlobalOptions(logger));

		if (params.timeseek > 0) {
			cmdList.add("-ss");
			cmdList.add("" + params.timeseek);
		}

		// decoder threads
		cmdList.add("-threads");
		cmdList.add("" + nThreads);

		final boolean isTsMuxeRVideoEngineEnabled = configuration.getEnginesAsList().contains(TsMuxeRVideo.ID);

		setAc3Remux(false);
		setDtsRemux(false);
		setVideoRemux(false);

		if (configuration.isAudioRemuxAC3() && params.aid != null && params.aid.isAC3() && renderer.isTranscodeToAC3()) {
			// AC-3 remux takes priority
			setAc3Remux(true);
		} else if (isTsMuxeRVideoEngineEnabled && configuration.isAudioEmbedDtsInPcm() && params.aid != null && params.aid.isDTS() && params.mediaRenderer.isDTSPlayable()) {
			// Now check for DTS remux
			setDtsRemux(true);
		}

		String frameRateRatio = media.getValidFps(true);
		String frameRateNumber = media.getValidFps(false);

		// Input filename
		cmdList.add("-i");
		cmdList.add(filename);

		if (media.getAudioTracksList().size() > 1) {
			// Set the video stream
			cmdList.add("-map");
			cmdList.add("0:v");

			// Set the proper audio stream
			cmdList.add("-map");
			cmdList.add("0:a:" + (media.getAudioTracksList().indexOf(params.aid)));
		}

		// Encoder threads
		cmdList.add("-threads");
		cmdList.add("" + nThreads);

		if (params.timeend > 0) {
			cmdList.add("-t");
			cmdList.add("" + params.timeend);
		}

		// add video bitrate options (-b:a)
		// cmdList.addAll(getVideoBitrateOptions(filename, dlna, media, params));

		// add audio bitrate options (-b:v)
		// cmdList.addAll(getAudioBitrateOptions(filename, dlna, media, params));

		// if the source is too large for the renderer, resize it
		// and/or add subtitles to video filter
		// FFmpeg must be compiled with --enable-libass parameter
		cmdList.addAll(getVideoFilterOptions(dlna, media, params));

		int defaultMaxBitrates[] = getVideoBitrateConfig(configuration.getMaximumBitrate());
		int rendererMaxBitrates[] = new int[2];

		if (renderer.getMaxVideoBitrate() != null) {
			rendererMaxBitrates = getVideoBitrateConfig(renderer.getMaxVideoBitrate());
		}
		
		// Give priority to the renderer's maximum bitrate setting over the user's setting
		if (rendererMaxBitrates[0] > 0 && rendererMaxBitrates[0] < defaultMaxBitrates[0]) {
			defaultMaxBitrates = rendererMaxBitrates;
		}

		if (params.mediaRenderer.getCBRVideoBitrate() == 0) {
			// Convert value from Mb to Kb
			defaultMaxBitrates[0] = 1000 * defaultMaxBitrates[0];

			// Halve it since it seems to send up to 1 second of video in advance
			defaultMaxBitrates[0] = defaultMaxBitrates[0] / 2;

			int bufSize = 1835;
			// x264 uses different buffering math than MPEG-2
			if (!renderer.isTranscodeToH264TSAC3()) {
				if (media.isHDVideo()) {
					bufSize = defaultMaxBitrates[0] / 3;
				}

				if (bufSize > 7000) {
					bufSize = 7000;
				}

				if (defaultMaxBitrates[1] > 0) {
					bufSize = defaultMaxBitrates[1];
				}

				if (params.mediaRenderer.isDefaultVBVSize() && rendererMaxBitrates[1] == 0) {
					bufSize = 1835;
				}
			}

			// Make room for audio
			if (isDtsRemux()) {
				defaultMaxBitrates[0] = defaultMaxBitrates[0] - 1510;
			} else {
				defaultMaxBitrates[0] = defaultMaxBitrates[0] - configuration.getAudioBitrate();
			}

			// Round down to the nearest Mb
			defaultMaxBitrates[0] = defaultMaxBitrates[0] / 1000 * 1000;

			// FFmpeg uses bytes for inputs instead of kbytes like MEncoder
			bufSize = bufSize * 1000;
			defaultMaxBitrates[0] = defaultMaxBitrates[0] * 1000;

			/**
			 * Level 4.1-limited renderers like the PS3 can stutter when H.264 video exceeds
			 * this bitrate
			 */
			if (renderer.isTranscodeToH264TSAC3() || isVideoRemux()) {
				if (
					params.mediaRenderer.isH264Level41Limited() &&
					defaultMaxBitrates[0] > 31250000
				) {
					defaultMaxBitrates[0] = 31250000;
				}
				bufSize = defaultMaxBitrates[0];
			}

			cmdList.add("-bufsize");
			cmdList.add("" + bufSize);

			cmdList.add("-maxrate");
			cmdList.add("" + defaultMaxBitrates[0]);
		}

		// Set audio bitrate and channel count only when doing audio transcoding
		if (!isAc3Remux() && !isDtsRemux() && !(type() == Format.AUDIO)) {
			int channels;
			if (renderer.isTranscodeToWMV() && !renderer.isXBOX()) {
				channels = 2;
			} else {
				channels = configuration.getAudioChannelCount(); // 5.1 max for AC-3 encoding
			}
			cmdList.add("-ac");
			cmdList.add("" + channels);

			cmdList.add("-ab");
			cmdList.add(configuration.getAudioBitrate() + "k");
		}

		if (params.timeseek > 0) {
			cmdList.add("-copypriorss");
			cmdList.add("0");
			cmdList.add("-avoid_negative_ts");
			cmdList.add("1");
		}

		// Add MPEG-2 quality settings
		if (!renderer.isTranscodeToH264TSAC3() && !isVideoRemux()) {
			String mpeg2Options = configuration.getMPEG2MainSettingsFFmpeg();
			String mpeg2OptionsRenderer = params.mediaRenderer.getCustomFFmpegMPEG2Options();

			// Renderer settings take priority over user settings
			if (isNotBlank(mpeg2OptionsRenderer)) {
				mpeg2Options = mpeg2OptionsRenderer;
			} else {
				if (mpeg2Options.contains("Automatic")) {
					mpeg2Options = "-g 5 -q:v 1 -qmin 2 -qmax 3";

					// It has been reported that non-PS3 renderers prefer keyint 5 but prefer it for PS3 because it lowers the average bitrate
					if (params.mediaRenderer.isPS3()) {
						mpeg2Options = "-g 25 -q:v 1 -qmin 2 -qmax 3";
					}

					if (mpeg2Options.contains("Wireless") || defaultMaxBitrates[0] < 70) {
						// Lower quality for 720p+ content
						if (media.getWidth() > 1280) {
							mpeg2Options = "-g 25 -qmax 7 -qmin 2";
						} else if (media.getWidth() > 720) {
							mpeg2Options = "-g 25 -qmax 5 -qmin 2";
						}
					}
				}
			}

			String[] customOptions = StringUtils.split(mpeg2Options);
			cmdList.addAll(new ArrayList<String>(Arrays.asList(customOptions)));
		}

		// Add the output options (-f, -c:a, -c:v, etc.)
		cmdList.addAll(getVideoTranscodeOptions(dlna, media, params));

		// Add custom options
		if (StringUtils.isNotEmpty(renderer.getCustomFFmpegOptions())) {
			parseOptions(renderer.getCustomFFmpegOptions(), cmdList);
		}

		if (!isDtsRemux()) {
			cmdList.add("pipe:");
		}

		String[] cmdArray = new String[cmdList.size()];
		cmdList.toArray(cmdArray);

		cmdArray = finalizeTranscoderArgs(
			filename,
			dlna,
			media,
			params,
			cmdArray
		);

		ProcessWrapperImpl pw = new ProcessWrapperImpl(cmdArray, params);

		if (isDtsRemux()) {
			PipeProcess pipe;
			pipe = new PipeProcess(System.currentTimeMillis() + "tsmuxerout.ts");

			TsMuxeRVideo ts = new TsMuxeRVideo(configuration);
			File f = new File(configuration.getTempFolder(), "pms-tsmuxer.meta");
			String cmd[] = new String[]{ ts.executable(), f.getAbsolutePath(), pipe.getInputPipe() };
			pw = new ProcessWrapperImpl(cmd, params);

			PipeIPCProcess ffVideoPipe = new PipeIPCProcess(System.currentTimeMillis() + "ffmpegvideo", System.currentTimeMillis() + "videoout", false, true);

			cmdList.add(ffVideoPipe.getInputPipe());

			OutputParams ffparams = new OutputParams(configuration);
			ffparams.maxBufferSize = 1;
			ffparams.stdin = params.stdin;

			String[] cmdArrayDts = new String[cmdList.size()];
			cmdList.toArray(cmdArrayDts);

			cmdArrayDts = finalizeTranscoderArgs(
				filename,
				dlna,
				media,
				params,
				cmdArrayDts
			);

			ProcessWrapperImpl ffVideo = new ProcessWrapperImpl(cmdArrayDts, ffparams);

			ProcessWrapper ff_video_pipe_process = ffVideoPipe.getPipeProcess();
			pw.attachProcess(ff_video_pipe_process);
			ff_video_pipe_process.runInNewThread();
			ffVideoPipe.deleteLater();

			pw.attachProcess(ffVideo);
			ffVideo.runInNewThread();

			PipeIPCProcess ffAudioPipe = new PipeIPCProcess(System.currentTimeMillis() + "ffmpegaudio01", System.currentTimeMillis() + "audioout", false, true);
			StreamModifier sm = new StreamModifier();
			sm.setPcm(false);
			sm.setDtsEmbed(isDtsRemux());
			sm.setSampleFrequency(48000);
			sm.setBitsPerSample(16);
			sm.setNbChannels(2);

			List<String> cmdListDTS = new ArrayList<String>();
			cmdListDTS.add(executable());
			cmdListDTS.add("-y");
			cmdListDTS.add("-ss");

			if (params.timeseek > 0) {
				cmdListDTS.add("" + params.timeseek);
			} else {
				cmdListDTS.add("0");
			}

			if (params.stdin == null) {
				cmdListDTS.add("-i");
			} else {
				cmdListDTS.add("-");
			}
			cmdListDTS.add(filename);

			if (params.timeseek > 0) {
				cmdListDTS.add("-copypriorss");
				cmdListDTS.add("0");
				cmdListDTS.add("-avoid_negative_ts");
				cmdListDTS.add("1");
			}

			cmdListDTS.add("-ac");
			cmdListDTS.add("2");

			cmdListDTS.add("-f");
			cmdListDTS.add("dts");

			cmdListDTS.add("-c:a");
			cmdListDTS.add("copy");

			cmdListDTS.add(ffAudioPipe.getInputPipe());

			String[] cmdArrayDTS = new String[cmdListDTS.size()];
			cmdListDTS.toArray(cmdArrayDTS);

			if (!params.mediaRenderer.isMuxDTSToMpeg()) { // No need to use the PCM trick when media renderer supports DTS
				ffAudioPipe.setModifier(sm);
			}

			OutputParams ffaudioparams = new OutputParams(configuration);
			ffaudioparams.maxBufferSize = 1;
			ffaudioparams.stdin = params.stdin;
			ProcessWrapperImpl ffAudio = new ProcessWrapperImpl(cmdArrayDTS, ffaudioparams);

			params.stdin = null;

			PrintWriter pwMux = new PrintWriter(f);
			pwMux.println("MUXOPT --no-pcr-on-video-pid --no-asyncio --new-audio-pes --vbr --vbv-len=500");
			String videoType = "V_MPEG-2";

			if (isVideoRemux()) {
				videoType = "V_MPEG4/ISO/AVC";
			}

			if (params.no_videoencode && params.forceType != null) {
				videoType = params.forceType;
			}

			String fps = "";
			if (params.forceFps != null) {
				fps = "fps=" + params.forceFps + ", ";
			}

			String audioType = "A_AC3";
			if (isDtsRemux()) {
				if (params.mediaRenderer.isMuxDTSToMpeg()) {
					// Renderer can play proper DTS track
					audioType = "A_DTS";
				} else {
					// DTS padded in LPCM trick
					audioType = "A_LPCM";
				}
			}

			pwMux.println(videoType + ", \"" + ffVideoPipe.getOutputPipe() + "\", " + fps + "level=4.1, insertSEI, contSPS, track=1");
			pwMux.println(audioType + ", \"" + ffAudioPipe.getOutputPipe() + "\", track=2");
			pwMux.close();

			ProcessWrapper pipe_process = pipe.getPipeProcess();
			pw.attachProcess(pipe_process);
			pipe_process.runInNewThread();

			try {
				Thread.sleep(50);
			} catch (InterruptedException e) { }

			pipe.deleteLater();
			params.input_pipes[0] = pipe;

			ProcessWrapper ff_pipe_process = ffAudioPipe.getPipeProcess();
			pw.attachProcess(ff_pipe_process);
			ff_pipe_process.runInNewThread();

			try {
				Thread.sleep(50);
			} catch (InterruptedException e) { }

			ffAudioPipe.deleteLater();
			pw.attachProcess(ffAudio);
			ffAudio.runInNewThread();
		}

		pw.runInNewThread();
		return pw;
	}

	@Override
	public JComponent config() {
		return config("NetworkTab.5");
	}

	protected JComponent config(String languageLabel) {
		FormLayout layout = new FormLayout(
			"left:pref, 0:grow",
			"p, 3dlu, p, 3dlu, p, 3dlu, p"
		);
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setBorder(Borders.EMPTY_BORDER);
		builder.setOpaque(false);

		CellConstraints cc = new CellConstraints();

		JComponent cmp = builder.addSeparator(Messages.getString(languageLabel), cc.xyw(2, 1, 1));
		cmp = (JComponent) cmp.getComponent(0);
		cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));

		multiThreadingCheckBox = new JCheckBox(Messages.getString("MEncoderVideo.35"));
		multiThreadingCheckBox.setContentAreaFilled(false);
		if (configuration.isFfmpegMultithreading()) {
			multiThreadingCheckBox.setSelected(true);
		}
		multiThreadingCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				configuration.setFfmpegMultithreading(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		builder.add(multiThreadingCheckBox, cc.xy(2, 3));

		videoRemuxCheckBox = new JCheckBox(Messages.getString("FFmpeg.0"));
		videoRemuxCheckBox.setContentAreaFilled(false);
		if (configuration.isFFmpegMuxWhenCompatible()) {
			videoRemuxCheckBox.setSelected(true);
		}
		videoRemuxCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				configuration.setFFmpegMuxWhenCompatible(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		builder.add(videoRemuxCheckBox, cc.xy(2, 5));

		return builder.getPanel();
	}

	@Override
	public boolean isCompatible(DLNAResource dlna) {
		if (
			PlayerUtil.isVideo(dlna, Format.Identifier.MKV) ||
			PlayerUtil.isVideo(dlna, Format.Identifier.MPG)
		) {
			return true;
		} else {
			return false;
		}
	}

	protected static List<String> parseOptions(String str) {
		return str == null ? null : parseOptions(str, new ArrayList<String>());
	}

	protected static List<String> parseOptions(String str, List<String> cmdList) {
		while (str.length() > 0) {
			if (str.charAt(0) == '\"') {
				int pos = str.indexOf("\"", 1);
				if (pos == -1) {
					// No ", error
					break;
				}
				String tmp = str.substring(1, pos);
				cmdList.add(tmp.trim());
				str = str.substring(pos + 1);
				continue;
			} else {
				// New arg, find space
				int pos = str.indexOf(" ");
				if (pos == -1) {
					// No space, we're done
					cmdList.add(str);
					break;
				}
				String tmp = str.substring(0, pos);
				cmdList.add(tmp.trim());
				str = str.substring(pos + 1);
				continue;
			}
		}
		return cmdList;
	}

	/**
	 * Shift timing of external subtitles in SSA/ASS or SRT format and converts charset to UTF8 if necessary
	 *
	 * @param params The {@link net.pms.io.OutputParams} context object used to store miscellaneous parameters for this request.
	 * @return Converted subtitle file
	 * @throws IOException
	 */
	public DLNAMediaSubtitle getSubtitles(OutputParams params) throws IOException {
		DLNAMediaSubtitle tempSubs = null;

		if (params.sid.getId() == -1) {
			return null;
		}

		final File subtitleDirectory = new File(configuration.getTempFolder(), SUB_DIR + File.separator);
		if (!subtitleDirectory.exists()) {
			subtitleDirectory.mkdirs();
		}

		if (params.sid.isExternal() && SubtitleUtils.isSupportsTimeShifting(params.sid.getType())) {
			try {
				tempSubs = SubtitleUtils.shiftSubtitlesTimingWithUtfConversion(params.sid, params.timeseek);
			} catch (IOException e) {
				logger.debug("Applying timeshift caused an error: " + e);
				tempSubs = null;
			}
		}

		return tempSubs;
	}

	/**
	 * Converts external subtitles file in SRT format or extract embedded subs to default SSA/ASS format.
	 *
	 * @param filename Subtitle file in SRT format or video file with embedded subs
	 * @param media the media metadata for the video being streamed. May contain unset/null values (e.g. for web videos).
	 * @param params The {@link net.pms.io.OutputParams} context object used to store miscellaneous parameters for this request.
	 * @return Converted subtitle file in SSA/ASS format
	 */
	// FIXME this is unused
	private File extractEmbeddedSubtitleTrack(String filename, DLNAMediaInfo media, OutputParams params) throws IOException {
		final List<String> cmdList = new ArrayList<String>();
		File tempSubsFile;
		cmdList.add(configuration.getFfmpegPath());
		cmdList.addAll(getGlobalOptions(logger));

		/* TODO Use it when external subs should be converted by ffmpeg
		if (
			isNotBlank(configuration.getSubtitlesCodepage()) &&
			params.sid.isExternal() &&
			!params.sid.isExternalFileUtf8() &&
			!params.sid.getExternalFileCharacterSet().equals(configuration.getSubtitlesCodepage()) // ExternalFileCharacterSet can be null
		) {
			cmdList.add("-sub_charenc");
			cmdList.add(configuration.getSubtitlesCodepage());
		}
		*/
		cmdList.add("-i");
		cmdList.add(filename);

		if (params.sid.isEmbedded()) {
			cmdList.add("-map");
			/* TODO broken code. Consider following example file:
				Stream #0:0(eng): Video: h264 (High), yuv420p, 720x576, SAR 178:139 DAR 445:278, 25 fps, 25 tbr, 1k tbn, 50 tbc (default)
				Metadata:
				  title           : H264
				Stream #0:1(rus): Subtitle: subrip
				Metadata:
				  title           : rus
				Stream #0:2(rus): Audio: mp3, 48000 Hz, stereo, s16p, 128 kb/s
				Metadata:
				  title           : rus
				Stream #0:3(eng): Audio: mp3, 48000 Hz, stereo, s16p, 119 kb/s (default)
				Metadata:
				  title           : eng
				Stream #0:4(eng): Subtitle: subrip (default)
				Metadata:
				  title           : eng

				FFmpeg sub track ids would be completely different. We should pass real ids.
			 */
			cmdList.add("0:" + (params.sid.getId() + media.getAudioTracksList().size() + 1));
		}

		final File subtitleDirectory = new File(configuration.getTempFolder(), SUB_DIR + File.separator);
		if (!subtitleDirectory.exists()) {
			subtitleDirectory.mkdirs();
		}

		if (params.sid.isEmbedded()) {
			tempSubsFile = new File(subtitleDirectory.getAbsolutePath() + File.separator +
					getBaseName(new File(filename).getName()).replaceAll("\\W", "_") + "_" +
					new File(filename).length() + "_EMB_ID" + params.sid.getId() + ".ass");
		} else {
			tempSubsFile = new File(subtitleDirectory.getAbsolutePath() + File.separator +
					getBaseName(new File(filename).getName()).replaceAll("\\W", "_") + "_" +
					new File(filename).length() + "_EXT." + getExtension(new File(filename).getName()));
		}

		cmdList.add(tempSubsFile.getAbsolutePath());

		String[] cmdArray = new String[cmdList.size()];
		cmdList.toArray(cmdArray);

		ProcessWrapperImpl pw = new ProcessWrapperImpl(cmdArray, params);
		pw.runInNewThread();

		try {
			pw.join(); // Wait until the conversion is finished
		} catch (InterruptedException e) {
			logger.debug("Subtitle conversion finished wih error: " + e);
			return null;
		}

		return tempSubsFile;
	}

	/**
	 * Collapse the multiple internal ways of saying "subtitles are disabled" into a single method
	 * which returns true if any of the following are true:
	 *
	 *     1) configuration.isDisableSubtitles()
	 *     2) params.sid == null
	 */
	public boolean isDisableSubtitles(OutputParams params) {
		return configuration.isDisableSubtitles() || (params.sid == null);
	}

	private synchronized boolean isAc3Remux() {
		return ac3Remux;
	}

	private synchronized void setAc3Remux(boolean ac3Remux) {
		this.ac3Remux = ac3Remux;
	}

	private synchronized boolean isDtsRemux() {
		return dtsRemux;
	}

	private synchronized void setDtsRemux(boolean dtsRemux) {
		this.dtsRemux = dtsRemux;
	}

	private synchronized boolean isVideoRemux() {
		return videoRemux;
	}

	private synchronized void setVideoRemux(boolean videoRemux) {
		this.videoRemux = videoRemux;
	}
}
