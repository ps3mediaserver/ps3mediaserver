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
import java.util.StringTokenizer;

import net.pms.PMS;
import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.InputFile;
import net.pms.encoders.Player;
import net.pms.network.HTTPResource;

public abstract class Format implements Cloneable {

	public int getType() {
		return type;
	}
	public static final int ISO = 32;
	public static final int PLAYLIST = 16;
	public static final int UNKNOWN = 8;
	public static final int VIDEO = 4;
	public static final int AUDIO = 1;
	public static final int IMAGE = 2;
	
	protected String matchedId;
	public String getMatchedId() {
		return matchedId;
	}
	protected int type = UNKNOWN;
	protected Format secondaryFormat;
	public Format getSecondaryFormat() {
		return secondaryFormat;
	}
	public void setSecondaryFormat(Format secondaryFormat) {
		this.secondaryFormat = secondaryFormat;
	}
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
		if (filename == null)
			return match;
		filename = filename.toLowerCase();
		for(String singleid:getId()) {
			String id = singleid.toLowerCase();
			match = filename.endsWith("." + id) || filename.startsWith(id + "://"); //$NON-NLS-1$ //$NON-NLS-2$
			if (match) {
				matchedId = singleid;
				return true;
			}
		}
		return match;
	}
	
	public boolean isVideo() {
		return (type&VIDEO) == VIDEO;
	}
	public boolean isAudio() {
		return (type&AUDIO) == AUDIO;
	}
	public boolean isImage() {
		return (type&IMAGE) == IMAGE;
	}
	public boolean isUnknown() {
		return (type&UNKNOWN) == UNKNOWN;
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
	// method which allow us the fine tuning of parsing with different formats in the future
	public void parse(DLNAMediaInfo media, InputFile file, int type) {
		parse(media, file, type, null);
	}
	
	// 10/02/03 now this is useful :p
	public void parse(DLNAMediaInfo media, InputFile file, int type, RendererConfiguration renderer) {
		if (renderer != null && renderer.isMediaParserV2()) {
			renderer.getFormatConfiguration().parse(media, file, this, type);
		} else {
			media.parse(file, this, type, false);	
		}
		PMS.debug("Parsing results: " + file + " / " + media);
	}
	
	public boolean skip(String extensions, String another_set_of_extensions) {
		if (extensions != null && extensions.length() > 0) {
			StringTokenizer st = new StringTokenizer(extensions, ",");
			while (st.hasMoreTokens()) {
				String id = st.nextToken().toLowerCase();
				if (matchedId != null && matchedId.toLowerCase().equals(id)) {
					return true;
				}
			}
		}
		if (another_set_of_extensions != null && another_set_of_extensions.length() > 0) {
			StringTokenizer st = new StringTokenizer(another_set_of_extensions, ",");
			while (st.hasMoreTokens()) {
				String id = st.nextToken().toLowerCase();
				if (matchedId != null && matchedId.toLowerCase().equals(id)) {
					return true;
				}
			}
		}
		
		return false;
	}
}
