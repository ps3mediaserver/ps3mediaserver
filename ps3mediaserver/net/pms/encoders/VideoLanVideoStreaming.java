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
import net.pms.dlna.DLNAMediaInfo;
import net.pms.formats.Format;
import net.pms.io.OutputParams;
import net.pms.io.PipeProcess;
import net.pms.io.ProcessWrapper;
import net.pms.io.ProcessWrapperImpl;

public class VideoLanVideoStreaming extends Player {

	public static final String ID = "vlcvideo"; //$NON-NLS-1$
	
	@Override
	public int purpose() {
		return VIDEO_WEBSTREAM_PLAYER;
	}
	
	@Override
	public String id() {
		return ID;
	}
	
	@Override
	public String[] args() {
		return new String[] { };
	}

	@Override
	public String name() {
		return "VideoLan Video Streaming"; //$NON-NLS-1$
	}

	@Override
	public int type() {
		return Format.VIDEO;
	}

	@Override
	public String mimeType() {
		return "video/mpeg"; //$NON-NLS-1$
	}

	@Override
	public String executable() {
		if (PMS.get().getVlcPath() == null)
			return null;
		return PMS.get().getVlcPath();
	}
	
	protected String getEncodingArgs() {
		return "vcodec=mp2v,vb=4096,acodec=mp3,ab=128,channels=2"; //$NON-NLS-1$
	}
	
	protected String getMux() {
		return "ts"; //$NON-NLS-1$
	}

	@Override
	public ProcessWrapper launchTranscode(String fileName, DLNAMediaInfo media, OutputParams params) throws IOException {
		
		
		PipeProcess tsPipe = new PipeProcess("VLC" + System.currentTimeMillis()); //$NON-NLS-1$
		params.input_pipes[0] = tsPipe;
		
		
		params.minBufferSize = params.minFileSize;
		params.secondread_minsize = 100000;
		
		String cmdArray [] = new String [6];
		cmdArray[0] = executable();
		cmdArray[1] = "-I"; //$NON-NLS-1$
		cmdArray[2] = "dummy"; //$NON-NLS-1$
		String trans = "#transcode{" + getEncodingArgs() + "}:duplicate{dst=std{access=file,mux=" + getMux() + ",dst=\"" +tsPipe.getInputPipe() + "\"}}"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		if (PMS.get().isWindows()) {
			cmdArray[3] = "--dummy-quiet"; //$NON-NLS-1$
			cmdArray[4] = fileName;
			cmdArray[5] = ":sout=" + trans; //$NON-NLS-1$
		} else {
			cmdArray[3] = fileName;
			cmdArray[4] = "--sout"; //$NON-NLS-1$
			cmdArray[5] = trans;
		}
				
		ProcessWrapperImpl pw = new ProcessWrapperImpl(cmdArray, params);
		
		ProcessWrapper pipe_process = tsPipe.getPipeProcess();
		pw.attachProcess(pipe_process);
		pipe_process.runInNewThread();
		try {
			Thread.sleep(150);
		} catch (InterruptedException e) { }
		tsPipe.deleteLater();
		
		pw.runInNewThread();
		return pw;
	}

	@Override
	public JComponent config() {
		// TODO Auto-generated method stub
		return null;
	}

}
