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
import net.pms.encoders.FFMpegAudio;
import net.pms.encoders.MPlayerAudio;
import net.pms.encoders.Player;

public class WAV extends MP3 {
	@Override
	public boolean transcodable() {
		return true;
	}

	@Override
	public ArrayList<Class<? extends Player>> getProfiles() {
		ArrayList<Class<? extends Player>> a = new ArrayList<Class<? extends Player>>();
		PMS r = PMS.get();
		for (String engine : PMS.getConfiguration().getEnginesAsList(r.getRegistry())) {
			if (engine.equals(MPlayerAudio.ID)) {
				a.add(MPlayerAudio.class);
			} else if (engine.equals(FFMpegAudio.ID)) {
				a.add(FFMpegAudio.class);
			}
		}
		return a;
	}

	@Override
	public String[] getId() {
		return new String[]{"wav"};
	}

	@Override
	public boolean ps3compatible() {
		return true;
	}
}
