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


import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTextField;

import net.pms.Messages;
import net.pms.PMS;
import net.pms.configuration.PmsConfiguration;
import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAMediaSubtitle;
import net.pms.dlna.DLNAResource;
import net.pms.formats.Format;
import net.pms.io.OutputParams;
import net.pms.io.ProcessWrapper;
import net.pms.io.ProcessWrapperImpl;
import net.pms.network.HTTPResource;
import net.pms.util.FileUtil;
import net.pms.util.ProcessUtil;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Pure FFmpeg video player.
 *
 * Design note:
 *
 * Helper methods that return lists of <code>String</code>s representing options are public
 * to facilitate composition e.g. a custom engine (plugin) that uses tsMuxeR for videos without
 * subtitles and FFmpeg otherwise needs to compose and call methods on both players.
 *
 * To avoid API churn, and to provide wiggle room for future functionality, all of these methods
 * take the same arguments as launchTranscode (and the same first four arguments as
 * finalizeTranscoderArgs) even if one or more of the parameters are unused e.g.:
 *
 *     public List<String> getAudioBitrateOptions(
 *         String filename,
 *         DLNAResource dlna,
 *         DLNAMediaInfo media,
 *         OutputParams params
 *     )
 */
public class FFMpegVideo extends Player {
	private static final Logger LOGGER = LoggerFactory.getLogger(FFMpegVideo.class);
	private JTextField ffmpeg;
	private final PmsConfiguration configuration;
	private static final String DEFAULT_QSCALE = "3";

	// FIXME we have an id() accessor for this; no need for the field to be public
	@Deprecated
	public static final String ID = "ffmpegvideo";

	/**
	 * This constructor is not used in the codebase.
	 */
	@Deprecated
	public FFMpegVideo() {
		this(PMS.getConfiguration());
	}
	
	/**
	 * Default constructor.
	 *
	 * @param configuration The PMS configuration options object
	 */
	public FFMpegVideo(PmsConfiguration configuration) {
		this.configuration = configuration;
	}

	/**
	 * Returns a list of strings representing the rescale options for this transcode i.e. the ffmpeg -vf
	 * options used to resize a video that's too wide and/or high for the specified renderer.
	 * If the renderer has no size limits, or there's no media metadata, or the video is within the renderer's
	 * size limits, an empty list is returned.
	 *
	 * @param filename The name of the file being transcoded.
	 * @param dlna The DLNA resource representing the file being transcoded.
	 * @param media the media metadata for the file being transcoded. May contain null fields (e.g. for web videos).
	 * @param params The {@link OutputParams} context object used to store miscellaneous parameters for this request.
	 * @return a {@link List} of <code>String</code>s representing the rescale options for this video,
	 * or an empty list if the video doesn't need to be resized.
	 * @since 1.81.0
	 */
	public List<String> getVideoRescaleOptions(String filename, DLNAResource dlna, DLNAMediaInfo media, OutputParams params) {
		return getRescaleOptions(params.mediaRenderer, media);
	}

	// TODO (breaking change) merge this into the method above
	@Deprecated
	public List<String> getRescaleOptions(RendererConfiguration renderer, DLNAMediaInfo media) {
		List<String> rescaleOptions = new ArrayList<String>();

		boolean isResolutionTooHighForRenderer = renderer.isVideoRescale() // renderer defines a max width/height
			&& (media != null)
			&& (
					(media.getWidth() > renderer.getMaxVideoWidth())
					||
					(media.getHeight() > renderer.getMaxVideoHeight())
			   );

		if (isResolutionTooHighForRenderer) {
			String rescaleSpec = String.format(
				// http://stackoverflow.com/a/8351875
				"scale=iw*min(%1$d/iw\\,%2$d/ih):ih*min(%1$d/iw\\,%2$d/ih),pad=%1$d:%2$d:(%1$d-iw)/2:(%2$d-ih)/2",
				renderer.getMaxVideoWidth(),
				renderer.getMaxVideoHeight()
			);

			rescaleOptions.add("-vf");
			rescaleOptions.add(rescaleSpec);
		}

		return rescaleOptions;
	}

	/**
	 * Returns a list of <code>String</code>s representing ffmpeg output options
	 * (i.e. options that define the output file's video codec, audio codec and container)
	 * compatible with the renderer's <code>TranscodeVideo</code> profile.
	 *
	 * @param filename The name of the file being transcoded.
	 * @param dlna The DLNA resource representing the file being transcoded.
	 * @param media the media metadata for the file being transcoded. May contain null fields (e.g. for web videos).
	 * @param params The {@link OutputParams} context object used to store miscellaneous parameters for this request.
	 * @return a {@link List} of <code>String</code>s representing the ffmpeg output parameters for the renderer according
	 * to its <code>TranscodeVideo</code> profile.
	 * @since 1.81.0
	 */
	public List<String> getVideoTranscodeOptions(String filename, DLNAResource dlna, DLNAMediaInfo media, OutputParams params) {
		return getTranscodeVideoOptions(params.mediaRenderer, media);
	}

	// TODO (breaking change) merge this into the method above
	@Deprecated
	public List<String> getTranscodeVideoOptions(RendererConfiguration renderer, DLNAMediaInfo media) {
		List<String> transcodeOptions = new ArrayList<String>();

		if (renderer.isTranscodeToWMV()) { // WMV
			transcodeOptions.add("-c:v");
			transcodeOptions.add("wmv2");

			transcodeOptions.add("-c:a");
			transcodeOptions.add("wmav2");

			transcodeOptions.add("-f");
			transcodeOptions.add("asf");
		} else { // MPEGPSAC3 or MPEGTSAC3
			transcodeOptions.add("-c:v");
			transcodeOptions.add("mpeg2video");

			transcodeOptions.add("-c:a");
			transcodeOptions.add("ac3");

			if (renderer.isTranscodeToMPEGTSAC3()) { // MPEGTSAC3
				transcodeOptions.add("-f");
				transcodeOptions.add("mpegts");
			} else { // default: MPEGPSAC3
				transcodeOptions.add("-f");
				transcodeOptions.add("vob");
			}
		}

		return transcodeOptions;
	}

	/**
	 * Returns the video bitrate spec for the current transcode according to
	 * the limits/requirements of the renderer.
	 *
	 * @param filename The name of the file being transcoded.
	 * @param dlna The DLNA resource representing the file being transcoded.
	 * @param media the media metadata for the file being transcoded. May contain null fields (e.g. for web videos).
	 * @param params The {@link OutputParams} context object used to store miscellaneous parameters for this request.
	 * @return a {@link List} of <code>String</code>s representing the video bitrate options for this transcode
	 * @since 1.81.0
	 */
	public List<String> getVideoBitrateOptions(String filename, DLNAResource dlna, DLNAMediaInfo media, OutputParams params) {
		return getVideoBitrateOptions(params.mediaRenderer, media);
	}

	// TODO (breaking change) merge this into the method above
	@Deprecated
	public List<String> getVideoBitrateOptions(RendererConfiguration renderer, DLNAMediaInfo media) { // media is currently unused
		List<String> videoBitrateOptions = new ArrayList<String>();
		String sMaxVideoBitrate = renderer.getMaxVideoBitrate(); // currently Mbit/s
		int iMaxVideoBitrate = 0;

		if (sMaxVideoBitrate != null) {
			try {
				iMaxVideoBitrate = Integer.parseInt(sMaxVideoBitrate);
			} catch (NumberFormatException nfe) {
				LOGGER.error("Can't parse max video bitrate", nfe); // XXX this should be handled in RendererConfiguration
			}
		}

		if (iMaxVideoBitrate == 0) { // unlimited: try to preserve the bitrate
			videoBitrateOptions.add("-q:v"); // video qscale
			videoBitrateOptions.add(DEFAULT_QSCALE);
		} else { // limit the bitrate FIXME untested
			// convert megabits-per-second (as per the current option name: MaxVideoBitrateMbps) to bps
			// FIXME rather than dealing with megabit vs mebibit issues here, this should be left up to the client i.e.
			// the renderer.conf unit should be bits-per-second (and the option should be renamed: MaxVideoBitrateMbps -> MaxVideoBitrate)
			videoBitrateOptions.add("-b:v");
			videoBitrateOptions.add("" + iMaxVideoBitrate * 1000 * 1000);
		}

		return videoBitrateOptions;
	}

	/**
	 * Returns the audio bitrate spec for the current transcode according to
	 * the limits/requirements of the renderer.
	 *
	 * @param filename The name of the file being transcoded.
	 * @param dlna The DLNA resource representing the file being transcoded.
	 * @param media the media metadata for the file being transcoded. May contain null fields (e.g. for web videos).
	 * @param params The {@link OutputParams} context object used to store miscellaneous parameters for this request.
	 * @return a {@link List} of <code>String</code>s representing the audio bitrate options for this transcode
	 * @since 1.81.0
	 */
	public List<String> getAudioBitrateOptions(String filename, DLNAResource dlna, DLNAMediaInfo media, OutputParams params) {
		return getAudioBitrateOptions(params.mediaRenderer, media);
	}

	// TODO (breaking change) merge this into the method above
	@Deprecated
	public List<String> getAudioBitrateOptions(RendererConfiguration renderer, DLNAMediaInfo media) {
		List<String> audioBitrateOptions = new ArrayList<String>();

		audioBitrateOptions.add("-q:a");
		audioBitrateOptions.add(DEFAULT_QSCALE);

		return audioBitrateOptions;
	}

	/**
	 * Returns options for burning in subtitles, if enabled and available.
	 *
	 * @param filename The name of the file being transcoded.
	 * @param dlna The DLNA resource representing the file being transcoded.
	 * @param media the media metadata for the file being transcoded. May contain null fields (e.g. for web videos).
	 * @param params The {@link OutputParams} context object used to store miscellaneous parameters for this request.
	 * @return The list of subtitle options.
	 * @since 1.81.0
	 */
	public List<String> getVideoSubtitleOptions(String filename, DLNAResource dlna, DLNAMediaInfo media, OutputParams params) {
		RendererConfiguration renderer = params.mediaRenderer;
		List<String> subtitleOptions = new ArrayList<String>();
		String externalSubtitlesFileName = null;

		if (params.sid != null) {
			if (params.sid.isExternal()) {
				// External subtitle file
				if (params.sid.isExternalFileUtf16()) {
					try {
						// Convert UTF-16 -> UTF-8
						File convertedSubtitles = new File(configuration.getTempFolder(), "utf8_" + params.sid.getExternalFile().getName());
						FileUtil.convertFileFromUtf16ToUtf8(params.sid.getExternalFile(), convertedSubtitles);
						externalSubtitlesFileName = ProcessUtil.getShortFileNameIfWideChars(convertedSubtitles.getAbsolutePath());
					} catch (IOException e) {
						LOGGER.debug("Error converting file from UTF-16 to UTF-8", e);
						externalSubtitlesFileName = ProcessUtil.getShortFileNameIfWideChars(params.sid.getExternalFile().getAbsolutePath());
					}
				} else {
					externalSubtitlesFileName = ProcessUtil.getShortFileNameIfWideChars(params.sid.getExternalFile().getAbsolutePath());
				}

				// Burn in subtitles with the subtitles filter (available since ffmpeg 1.1)
				subtitleOptions.add("-vf");
				subtitleOptions.add("subtitles=" + externalSubtitlesFileName);
			}
			// TODO: Handle embedded subtitles
		}

		return subtitleOptions;
	}

	/**
	 * Returns the audio channel (-ac) options.
	 *
	 * @param filename The name of the file being transcoded.
	 * @param dlna The DLNA resource representing the file being transcoded.
	 * @param media the media metadata for the file being transcoded. May contain null fields (e.g. for web videos).
	 * @param params The {@link OutputParams} context object used to store miscellaneous parameters for this request.
	 * @return The list of audio channel options.
	 * @since 1.81.0
	 */

	public List<String> getAudioChannelOptions(String filename, DLNAResource dlna, DLNAMediaInfo media, OutputParams params) {
		List<String> audioChannelOptions = new ArrayList<String>();
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
			audioChannelOptions.add("-ac");
			audioChannelOptions.add("" + ac);
		}

		return audioChannelOptions;
	}

	@Override
	public int purpose() {
		return VIDEO_SIMPLEFILE_PLAYER;
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

	@Override
	@Deprecated
	public boolean avisynth() {
		return false;
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
		return new String[] {
			"-vcodec",
			"mpeg2video",
			"-f",
			"vob",
			"-acodec",
			"ac3"
		};
	}

	@Override
	@Deprecated
	public String[] args() {
		return getDefaultArgs(); // unused; return this array for for backwards compatibility
	}

	private List<String> getCustomArgs() {
		List<String> customOptions = new ArrayList<String>();
		String customOptionsString = PMS.getConfiguration().getFfmpegSettings();

		if (StringUtils.isNotBlank(customOptionsString)) {
			LOGGER.debug("Custom ffmpeg output options: {}", customOptionsString);
		}

		Collections.addAll(customOptions, StringUtils.split(customOptionsString));
		return customOptions;
	}

	// XXX hardwired to false and not referenced anywhere else in the codebase
	@Deprecated
	public boolean mplayer() {
		return false;
	}

	@Override
	public String mimeType() {
		return HTTPResource.VIDEO_TRANSCODE;
	}

	@Override
	public String executable() {
		return PMS.getConfiguration().getFfmpegPath();
	}

	@Override
	public ProcessWrapper launchTranscode(
		String filename,
		DLNAResource dlna,
		DLNAMediaInfo media,
		OutputParams params
	) throws IOException {
		int nThreads = PMS.getConfiguration().getNumberOfCpuCores();
		List<String> cmdList = new ArrayList<String>();

		// Populate params with audio track and subtitles metadata
		setAudioAndSubs(filename, media, params, configuration);

		cmdList.add(executable());

		cmdList.add("-loglevel");
		cmdList.add("warning"); // XXX this should probably be configurable, for debugging

		if (params.timeseek > 0) {
			cmdList.add("-ss");
			cmdList.add("" + params.timeseek);
		}

		// decoder threads
		cmdList.add("-threads");
		cmdList.add("" + nThreads);

		cmdList.add("-i");
		cmdList.add(filename);

		// encoder threads
		cmdList.add("-threads");
		cmdList.add("" + nThreads);

		if (params.timeend > 0) {
			cmdList.add("-t");
			cmdList.add("" + params.timeend);
		}

		// add video bitrate options (-b:a)
		cmdList.addAll(getVideoBitrateOptions(filename, dlna, media, params));

		// add audio bitrate options (-b:v)
		cmdList.addAll(getAudioBitrateOptions(filename, dlna, media, params));

		// if the source is too large for the renderer, resize it (-vf scale=...)
		cmdList.addAll(getVideoRescaleOptions(filename, dlna, media, params));

		// add subtitles (-vf subtitles=...)
		cmdList.addAll(getVideoSubtitleOptions(filename, dlna, media, params));

		// add audio channels (-ac)
		cmdList.addAll(getAudioChannelOptions(filename, dlna, media, params));
		
		// add custom args
		cmdList.addAll(getCustomArgs());

		// add the output options (-f, -acodec, -vcodec)
		cmdList.addAll(getVideoTranscodeOptions(filename, dlna, media, params));

		cmdList.add("pipe:");

		String[] cmdArray = new String[ cmdList.size() ];
		cmdList.toArray(cmdArray);

		cmdArray = finalizeTranscoderArgs(
			filename,
			dlna,
			media,
			params,
			cmdArray
		);

		ProcessWrapperImpl pw = new ProcessWrapperImpl(cmdArray, params);
		pw.runInNewThread();

		return pw;
	}

	// XXX pointless redirection of launchTranscode
	// TODO remove this method and move its body into launchTranscode
	@Deprecated
	protected ProcessWrapperImpl getFFMpegTranscode(
		String filename,
		DLNAResource dlna,
		DLNAMediaInfo media,
		OutputParams params,
		String args[]
	) throws IOException {
		return (ProcessWrapperImpl) launchTranscode(filename, dlna, media, params);
	}

	@Override
	public JComponent config() {
		return config("FFMpegVideo.1");
	}

	protected JComponent config(String languageLabel) {
		FormLayout layout = new FormLayout(
			"left:pref, 0:grow",
			"p, 3dlu, p, 3dlu"
		);
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setBorder(Borders.EMPTY_BORDER);
		builder.setOpaque(false);

		CellConstraints cc = new CellConstraints();

		JComponent cmp = builder.addSeparator(Messages.getString(languageLabel), cc.xyw(2, 1, 1));
		cmp = (JComponent) cmp.getComponent(0);
		cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));

		ffmpeg = new JTextField(PMS.getConfiguration().getFfmpegSettings());
		ffmpeg.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				PMS.getConfiguration().setFfmpegSettings(ffmpeg.getText());
			}
		});

		builder.add(ffmpeg, cc.xy(2, 3));

		return builder.getPanel();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isCompatible(DLNAResource resource) {
		if (resource == null || resource.getFormat().getType() != Format.VIDEO) {
			return false;
		}

		DLNAMediaSubtitle subtitle = resource.getMediaSubtitle();

		// Check whether the subtitle actually has a language defined,
		// uninitialized DLNAMediaSubtitle objects have a null language.
		if (subtitle != null && subtitle.getLang() != null) {
			// The resource needs a subtitle, but PMS support for FFmpeg subtitles has not yet been implemented.
			return false;
		}

		try {
			String audioTrackName = resource.getMediaAudio().toString();
			String defaultAudioTrackName = resource.getMedia().getAudioTracksList().get(0).toString();

			if (!audioTrackName.equals(defaultAudioTrackName)) {
				// PMS only supports playback of the default audio track for FFmpeg
				return false;
			}
		} catch (NullPointerException e) {
			LOGGER.trace("FFmpeg cannot determine compatibility based on audio track for "
					+ resource.getSystemName());
		} catch (IndexOutOfBoundsException e) {
			LOGGER.trace("FFmpeg cannot determine compatibility based on default audio track for "
					+ resource.getSystemName());
		}

		Format format = resource.getFormat();

		if (format != null) {
			Format.Identifier id = format.getIdentifier();

			if (id.equals(Format.Identifier.MKV) || id.equals(Format.Identifier.MPG)) {
				return true;
			}
		}

		return false;
	}
}
