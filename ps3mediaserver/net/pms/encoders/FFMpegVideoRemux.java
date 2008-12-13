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
import net.pms.io.ProcessWrapper;
import net.pms.io.ProcessWrapperImpl;

public class FFMpegVideoRemux extends Player {
	
	public static final String ID = "ffmpegdvrmsremux";
	
	@Override
	public int purpose() {
		return MISC_PLAYER;
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
	public boolean avisynth() {
		return false;
	}

	
	public FFMpegVideoRemux() {
	}

	@Override
	public String name() {
		return "FFmpeg DVR-MS Remux";
	}

	@Override
	public int type() {
		return Format.VIDEO;
	}
	
	protected String [] getDefaultArgs() {
		return new String [] { "-f", "vob", "-vcodec", "copy", "-acodec", "copy" };
	}

	@Override
	public String[] args() {
		return getDefaultArgs();
			
	}

	@Override
	public String mimeType() {
		return "video/mpeg";
	}

	@Override
	public String executable() {
		return PMS.get().getFFmpegPath();
	}

	@Override
	public ProcessWrapper launchTranscode(String fileName, DLNAMediaInfo media, OutputParams params) throws IOException {
		return getFFMpegTranscode(fileName, media, params);
	}

	protected ProcessWrapperImpl getFFMpegTranscode(String fileName, DLNAMediaInfo media, OutputParams params) throws IOException {
		
		
		
		String cmdArray [] = new String [8+args().length];
		cmdArray[0] = executable();
		cmdArray[1] = "-title";
		cmdArray[2] = "dummy";
		
		/*cmdArray[3] = "-f";
		cmdArray[4] = "asf";
		if (params.forceType != null) {
			cmdArray[4] = params.forceType;
		}*/
		cmdArray[3] = "-i";
		cmdArray[4] = fileName;
		cmdArray[5] = "-title";
		cmdArray[6] = "dummy";
		if (params.timeseek > 0 && !PMS.get().isForceMPlayer() && !mplayer()) {
			cmdArray[5] = "-ss";
			cmdArray[6] = "" + params.timeseek;
			 params.timeseek = 0;
		}
		for(int i=0;i<args().length;i++)
			cmdArray[7+i] = args()[i];
		
		cmdArray[cmdArray.length-1] = "pipe:";
			
		ProcessWrapperImpl pw = new ProcessWrapperImpl(cmdArray, params);
			
		pw.runInNewThread();
		return pw;
	}

	@Override
	public JComponent config() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
