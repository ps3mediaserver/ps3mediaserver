/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2008-2012 A.Brochard
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

import net.pms.configuration.PmsConfiguration;
import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAResource;
import net.pms.formats.Format;
import net.pms.formats.FormatFactory;
import net.pms.formats.WEB;
import net.pms.io.OutputParams;
import net.pms.io.PipeProcess;
import net.pms.io.ProcessWrapper;
import net.pms.io.ProcessWrapperImpl;
import net.pms.util.PlayerUtil;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class FFmpegWebVideo extends FFmpegVideo {
	private static final Logger LOGGER = LoggerFactory.getLogger(FFmpegWebVideo.class);
	private static List<String> protocols;
	private static boolean init = false;
	private boolean convertMmsToMmsh = false;

	// FIXME we have an id() accessor for this; no need for the field to be public
	@Deprecated
	public static final String ID = "ffmpegwebvideo";

	@Override
	public JComponent config() {
		return null;
	}

	@Override
	public String id() {
		return ID;
	}

	@Override
	public PlayerPurpose getPurpose() {
		return PlayerPurpose.VIDEO_WEB_STREAM_PLAYER;
	}

	@Override
	public boolean isTimeSeekable() {
		return false;
	}

	public FFmpegWebVideo(PmsConfiguration configuration) {
		super(configuration);

		if (!init) {
			protocols = FFmpegOptions.getSupportedProtocols(configuration);

			// XXX see workaround below
			if (!protocols.contains("mms") && protocols.contains("mmsh")) {
				convertMmsToMmsh = true;
				protocols.add("mms");
			}

			LOGGER.debug("FFmpeg supported protocols: {}", protocols);

			// Register protocols as a WEB format
			final String[] ffmpegProtocols = new String[protocols.size()];
			protocols.toArray(ffmpegProtocols);
			FormatFactory.getExtensions().add(0, new WEB() {
				@Override
				public String[] getId() {
					return ffmpegProtocols;
				}

				@Override
				public String toString() {
					return "FFMPEG.WEB";
				}
			});

			init = true;
		}
	}

	@Override
	public synchronized ProcessWrapper launchTranscode(
		DLNAResource dlna,
		DLNAMediaInfo media,
		OutputParams params
	) throws IOException {
		params.minBufferSize = params.minFileSize;
		params.secondread_minsize = 100000;
		RendererConfiguration renderer = params.mediaRenderer;
		String filename = dlna.getSystemName();

		// XXX work around an ffmpeg bug: http://ffmpeg.org/trac/ffmpeg/ticket/998
		if (convertMmsToMmsh) {
			if (filename.startsWith("mms:")) {
				filename = "mmsh:" + filename.substring(4);
			}
		}

		FFmpegOptions customOptions = new FFmpegOptions();

		// (HTTP) header options
		if (params.header != null && params.header.length > 0) {
			String hdr = new String(params.header);
			customOptions.addAll(parseOptions(hdr));
		}

		// renderer options
		if (isNotEmpty(renderer.getCustomFFmpegOptions())) {
			customOptions.addAll(parseOptions(renderer.getCustomFFmpegOptions()));
		}

		// basename of the named pipe:
		// ffmpeg -loglevel warning -threads nThreads -i URL -threads nThreads -transcode-video-options /path/to/fifoName
		String fifoName = String.format(
			"ffmpegwebvideo_%d_%d",
			Thread.currentThread().getId(),
			System.currentTimeMillis()
		);

		// This process wraps the command that creates the named pipe
		PipeProcess pipe = new PipeProcess(fifoName);
		pipe.deleteLater(); // delete the named pipe later; harmless if it isn't created
		ProcessWrapper mkfifo_process = pipe.getPipeProcess();

		// Start the process as early as possible
		mkfifo_process.runInNewThread();

		params.input_pipes[0] = pipe;

		// Build the command line
		final List<String> cmdList = new ArrayList<String>();
		cmdList.add(executable().replace("/", "\\\\"));

		// XXX squashed bug - without this, ffmpeg hangs waiting for a confirmation
		// that it can write to a file that already exists i.e. the named pipe
		cmdList.add("-y");

		cmdList.add("-loglevel");

		if (LOGGER.isTraceEnabled()) { // Set -loglevel in accordance with LOGGER setting
			cmdList.add("info"); // Could be changed to "verbose" or "debug" if "info" level is not enough
		} else {
			cmdList.add("warning");
		}

		int nThreads = configuration.getNumberOfCpuCores();

		// Decoder threads
		cmdList.add("-threads");
		cmdList.add("" + nThreads);

		// Add global and input-file custom options, if any
		if (!customOptions.isEmpty()) {
			customOptions.transferGlobals(cmdList);
			customOptions.transferInputFileOptions(cmdList);
		}

		cmdList.add("-i");
		cmdList.add(filename);

		cmdList.addAll(getVideoFilterOptions(dlna, media, params));

		// Encoder threads
		cmdList.add("-threads");
		cmdList.add("" + nThreads);

		// Add the output options (-f, -acodec, -vcodec)
		cmdList.addAll(getVideoTranscodeOptions(dlna, media, params));

		// Add video bitrate options
		cmdList.addAll(getVideoBitrateOptions(dlna, media, params));

		// Add audio bitrate options
		cmdList.addAll(getAudioBitrateOptions(dlna, media, params));

		// Add any remaining custom options
		if (!customOptions.isEmpty()) {
			customOptions.transferAll(cmdList);
		}

		// Output file
		cmdList.add(pipe.getInputPipe());

		// Convert the command list to an array
		String[] cmdArray = new String[cmdList.size()];
		cmdList.toArray(cmdArray);

		// Hook to allow plugins to customize this command line
		cmdArray = finalizeTranscoderArgs(
			filename,
			dlna,
			media,
			params,
			cmdArray
		);

		// Now launch FFmpeg
		ProcessWrapperImpl pw = new ProcessWrapperImpl(cmdArray, params);
		pw.attachProcess(mkfifo_process); // Clean up the mkfifo process when the transcode ends

		// Give the mkfifo process a little time
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			LOGGER.error("Thread interrupted while waiting for named pipe to be created", e);
		}

		// Launch the transcode command...
		pw.runInNewThread();
		// ...and wait briefly to allow it to start
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			LOGGER.error("Thread interrupted while waiting for transcode to start", e);
		}

		return pw;
	}

	@Override
	public String name() {
		return "FFmpeg Web Video";
	}

	// TODO remove this when it's removed from Player
	@Deprecated
	@Override
	public String[] args() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isCompatible(DLNAResource dlna) {
		if (!PlayerUtil.isWebVideo(dlna)) {
			return false;
		}

		String uri = dlna.getSystemName();
		return protocols.contains(StringUtils.substringBefore(uri, ":"));
	}
}
