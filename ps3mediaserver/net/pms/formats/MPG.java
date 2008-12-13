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
package net.pms.formats;

import java.util.ArrayList;

import net.pms.PMS;
import net.pms.encoders.FFMpegVideo;
import net.pms.encoders.MEncoderAviSynth;
import net.pms.encoders.MEncoderVideo;
import net.pms.encoders.Player;
import net.pms.encoders.TSMuxerVideo;


public class MPG extends Format {
	
	@Override
	public ArrayList<Class<? extends Player>> getProfiles() {
		if (PMS.get().getEnginesAsList() == null || PMS.get().getEnginesAsList().size() == 0 || PMS.get().getEnginesAsList().contains("none"))
			return null;
		ArrayList<Class<? extends Player>> a = new ArrayList<Class<? extends Player>>();
		for(String engine:PMS.get().getEnginesAsList()) {
			if (engine.equals(MEncoderVideo.ID))
				a.add(MEncoderVideo.class);
			else if (engine.equals(MEncoderAviSynth.ID) && PMS.get().getRegistry().isAvis())
				a.add(MEncoderAviSynth.class);
			else if (engine.equals(FFMpegVideo.ID) && PMS.get().getRegistry().isAvis())
				a.add(FFMpegVideo.class);
			else if (engine.equals(TSMuxerVideo.ID)/* && PMS.get().isWindows()*/)
				a.add(TSMuxerVideo.class);
		}
		return a;
	}

	@Override
	public boolean transcodable() {
		return true;
	}

	public MPG() {
		type = VIDEO;
	}

	@Override
	public String[] getId() {
		return new String [] { "mpg", "mpeg", "ts", "m2t", "m2ts", "mts", "mp4", "avi", "wmv", "wm", "vob", "divx" };
	}

	@Override
	public boolean ps3compatible() {
		return true;
	}
	

}
