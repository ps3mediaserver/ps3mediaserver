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

import net.pms.dlna.DLNAMediaInfo;
import net.pms.formats.Format;
import net.pms.io.OutputParams;
import net.pms.io.ProcessWrapper;

public abstract class Player {
	
	public static final int VIDEO_SIMPLEFILE_PLAYER = 0; 
	public static final int AUDIO_SIMPLEFILE_PLAYER = 1;
	public static final int VIDEO_WEBSTREAM_PLAYER = 2;
	public static final int AUDIO_WEBSTREAM_PLAYER = 3;
	public static final int MISC_PLAYER = 4;
	public static final String NATIVE = "NATIVE";

	public boolean avisynth() {
		return false;
	}
	
	public boolean excludeFormat(Format extension) {
		return false;
	}
	
	public abstract int purpose();
	public abstract JComponent config();
	public abstract String id();
	public abstract String name();
	public abstract int type();
	public abstract String [] args();
	public abstract String mimeType();
	public abstract String executable();
	public boolean isTimeSeekable() {
		return false;
	}
	public abstract ProcessWrapper launchTranscode(String fileName, DLNAMediaInfo media, OutputParams params) throws IOException;
	
	public String toString() {
		return name();
	}

}
