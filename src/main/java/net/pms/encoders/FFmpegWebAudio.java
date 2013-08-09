/*
 * PS3 Media Server, for streaming any media to your PS3.
 * Copyright (C) 2008-2013 A.Brochard
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
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAResource;
import net.pms.io.OutputParams;
import net.pms.io.ProcessWrapper;
import net.pms.io.ProcessWrapperImpl;
import net.pms.util.PlayerUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FFmpegWebAudio extends FFmpegAudio {
	private static final Logger logger = LoggerFactory.getLogger(FFmpegWebAudio.class);
	private final PmsConfiguration configuration;
	private final FFmpegProtocols protocols;
	public static final String ID = "ffmpegwebaudio";

	public FFmpegWebAudio(PmsConfiguration configuration) {
		super(configuration);
		this.configuration = configuration;
		this.protocols = getProtocols();
	}

	// use FFmpegWebAudio(PmsConfiguration)
	@Deprecated
	public FFmpegWebAudio(PmsConfiguration configuration, FFmpegProtocols protocols) {
		this(configuration);
	}

	@Override
	public JComponent config() {
		return null;
	}

	@Override
	public PlayerPurpose getPurpose() {
		return PlayerPurpose.AUDIO_WEB_STREAM_PLAYER;
	}

	@Override
	public String id() {
		return ID;
	}

	@Override
	public boolean isTimeSeekable() {
		return false;
	}

	@Override
	public String name() {
		return "FFmpeg Web Audio";
	}

	@Override
	public ProcessWrapper launchTranscode(
		DLNAResource dlna,
		DLNAMediaInfo media,
		OutputParams params
	) throws IOException {
		final String filename = protocols.getFilename(dlna.getSystemName());
		params.maxBufferSize = configuration.getMaxAudioBuffer();
		params.waitbeforestart = 6000;
		params.manageFastStart();

		int nThreads = configuration.getNumberOfCpuCores();
		List<String> cmdList = new ArrayList<String>();

		cmdList.add(executable());
		cmdList.addAll(getGlobalOptions(logger));

		// decoder threads
		cmdList.add("-threads");
		cmdList.add("" + nThreads);

		cmdList.add("-i");
		cmdList.add(filename);

		// encoder threads
		cmdList.add("-threads");
		cmdList.add("" + nThreads);

		// Add the output options (-f, -ab, -ar)
		cmdList.addAll(getAudioTranscodeOptions(dlna, media, params));
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

	@Override
	public boolean isCompatible(DLNAResource dlna) {
		if (!PlayerUtil.isWebAudio(dlna)) {
			return false;
		}

		String protocol = dlna.getFormat().getMatchedExtension();
		return protocols.isSupportedProtocol(protocol);
	}
}
