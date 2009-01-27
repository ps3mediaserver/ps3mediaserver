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
		PMS r = PMS.get();
		PMS r1 = PMS.get();
		PMS r2 = PMS.get();
		if (PMS.getConfiguration().getEnginesAsList(r.getRegistry()) == null || PMS.getConfiguration().getEnginesAsList(r1.getRegistry()).size() == 0 || PMS.getConfiguration().getEnginesAsList(r2.getRegistry()).contains("none")) //$NON-NLS-1$
			return null;
		ArrayList<Class<? extends Player>> a = new ArrayList<Class<? extends Player>>();
		PMS r3 = PMS.get();
		for(String engine:PMS.getConfiguration().getEnginesAsList(r3.getRegistry())) {
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
		return new String [] { "mpg", "mpeg", "mpe", "ts", "tp", "m2t", "m2ts", "m2p", "mts", "mp4", "m4v", "avi", "wmv", "wm", "vob", "divx" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$ //$NON-NLS-13$ //$NON-NLS-14$
	}

	@Override
	public boolean ps3compatible() {
		return true;
	}
	

}
