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

import net.pms.PMS;
import net.pms.configuration.PmsConfiguration;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAResource;
import net.pms.formats.Format;
import net.pms.io.OutputParams;
import net.pms.io.PipeProcess;
import net.pms.io.ProcessWrapper;
import net.pms.io.ProcessWrapperImpl;
import net.pms.network.HTTPResource;
import net.pms.util.PlayerUtil;

import javax.swing.*;
import java.io.IOException;

public class MPlayerWebVideoDump extends MPlayerAudio {
	public MPlayerWebVideoDump(PmsConfiguration configuration) {
		super(configuration);
	}
	public static final String ID = "mplayervideodump";

	@Override
	public JComponent config() {
		return null;
	}

	@Override
	public int purpose() {
		return VIDEO_WEBSTREAM_PLAYER;
	}

	@Override
	public String id() {
		return ID;
	}

	@Override
	public ProcessWrapper launchTranscode(
		String filename,
		DLNAResource dlna,
		DLNAMediaInfo media,
		OutputParams params
	) throws IOException {
		params.minBufferSize = params.minFileSize;
		params.secondread_minsize = 100000;
		params.waitbeforestart = 6000;
		params.maxBufferSize = PMS.getConfiguration().getMaxAudioBuffer();
		PipeProcess pipeProcess = new PipeProcess("mplayer_web_video" + System.currentTimeMillis());

		String mPlayerDefaultAudioArgs[] = new String[]{
			PMS.getConfiguration().getMplayerPath(),
			filename,
			"-nocache",
			"-prefer-ipv4",
			"-dumpstream",
			"-quiet",
			"-dumpfile",
			pipeProcess.getInputPipe()
		};

		params.input_pipes[0] = pipeProcess;
		ProcessWrapper mkfifo_process = pipeProcess.getPipeProcess();

		mPlayerDefaultAudioArgs = finalizeTranscoderArgs(
			filename,
			dlna,
			media,
			params,
			mPlayerDefaultAudioArgs
		);

		ProcessWrapperImpl pw = new ProcessWrapperImpl(mPlayerDefaultAudioArgs, params);
		pw.attachProcess(mkfifo_process);
		mkfifo_process.runInNewThread();

		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
		}

		pipeProcess.deleteLater();
		pw.runInNewThread();

		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
		}

		return pw;
	}

	@Override
	public String mimeType() {
		return HTTPResource.VIDEO_TRANSCODE;
	}

	@Override
	public String name() {
		return "MPlayer Web Video";
	}

	@Override
	public int type() {
		return Format.VIDEO;
	}

	@Override
	public boolean isTimeSeekable() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isCompatible(DLNAResource resource) {
		return PlayerUtil.isWebVideo(resource);
	}
}
