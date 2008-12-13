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
import net.pms.encoders.Player;
import net.pms.network.HTTPResource;

public abstract class Format implements Cloneable {

	public int getType() {
		return type;
	}
	public static final int UNKNOWN = -1;
	public static final int VIDEO = 0;
	public static final int AUDIO = 1;
	public static final int IMAGE = 2;
	
	protected int type = UNKNOWN;
	public void setType(int type) {
		if (isUnknown())
			this.type = type;
	}
	public abstract String [] getId();
	public abstract boolean ps3compatible();
	public abstract boolean transcodable();
	public abstract ArrayList<Class<? extends Player>> getProfiles();
	
	public String mimeType() {
		return new HTTPResource().getDefaultMimeType(type);
	}
	
	public boolean match(String filename) {
		boolean match = false;
		for(String singleid:getId()) {
			String id = singleid.toLowerCase();
			filename = filename.toLowerCase();
			match = filename.endsWith("." + id) || filename.startsWith(id + "://");
			if (match)
				return true;
		}
		return match;
	}
	public boolean isVideo() {
		return type == VIDEO;
	}
	public boolean isAudio() {
		return type == AUDIO;
	}
	public boolean isImage() {
		return type == IMAGE;
	}
	public boolean isUnknown() {
		return type == UNKNOWN;
	}
	@Override
	protected Object clone() {
		Object o = null;
		try {
			o = super.clone();
		} catch (CloneNotSupportedException e) {
			PMS.error(null, e);
		}
		return o;
	}
	public Format duplicate() {
		return (Format) this.clone();
	}
}
