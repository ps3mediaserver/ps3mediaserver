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

import java.io.IOException;

import javax.swing.JComponent;

import net.pms.PMS;
import net.pms.configuration.PmsConfiguration;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.formats.Format;
import net.pms.io.OutputParams;
import net.pms.io.PipeProcess;
import net.pms.io.ProcessWrapper;
import net.pms.io.ProcessWrapperImpl;
import net.pms.network.HTTPResource;

public class MPlayerWebVideoDump extends MPlayerAudio {

	public MPlayerWebVideoDump(PmsConfiguration configuration) {
		super(configuration);
	}

	public static final String ID = "mplayervideodump"; //$NON-NLS-1$
	
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
	public ProcessWrapper launchTranscode(String fileName, DLNAMediaInfo media,
			OutputParams params) throws IOException {
		params.minBufferSize = params.minFileSize;
		params.secondread_minsize = 100000;
		params.waitbeforestart = 6000;
		params.maxBufferSize = PMS.getConfiguration().getMaxAudioBuffer();
		PipeProcess audioP = new PipeProcess("mplayer_webvid" + System.currentTimeMillis()); //$NON-NLS-1$
			
		String mPlayerdefaultAudioArgs [] = new String [] { PMS.getConfiguration().getMplayerPath(), fileName, "-nocache", "-dumpstream", "-quiet", "-dumpfile", audioP.getInputPipe() }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		params.input_pipes [0]= audioP;
			
		ProcessWrapper mkfifo_process = audioP.getPipeProcess();
		
		ProcessWrapperImpl pw = new ProcessWrapperImpl(mPlayerdefaultAudioArgs, params);
		pw.attachProcess(mkfifo_process);
		mkfifo_process.runInNewThread();
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) { }
		
		audioP.deleteLater();
		pw.runInNewThread();
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) { }
		return pw;
	}

	@Override
	public String mimeType() {
		return HTTPResource.VIDEO_TRANSCODE; //$NON-NLS-1$
	}

	@Override
	public String name() {
		return "MPlayer Video Dump"; //$NON-NLS-1$
	}

	@Override
	public int type() {
		return Format.VIDEO;
	}
	
	@Override
	public boolean isTimeSeekable() {
		return false;
	}
}
