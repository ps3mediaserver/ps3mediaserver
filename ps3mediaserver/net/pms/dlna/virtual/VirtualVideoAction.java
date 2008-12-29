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
package net.pms.dlna.virtual;

import java.io.IOException;
import java.io.InputStream;

import net.pms.PMS;
import net.pms.dlna.DLNAResource;
import net.pms.network.HTTPResource;

public abstract class VirtualVideoAction extends DLNAResource {
	
	private boolean enabled;

	protected String name;
	private String thumbnailIconOK;
	private String thumbnailIconKO;
	private String thumbnailContentType;
	private String videoOk;
	private String videoKo;
	private long timer1;
	
	
	public VirtualVideoAction(String name, boolean enabled) {
		this.name = name;
		thumbnailContentType = HTTPResource.PNG_TYPEMIME;
		thumbnailIconOK = "images/apply-256.png";
		thumbnailIconKO = "images/button_cancel-256.png";
		this.videoOk = "videos/action_success-512.mpg";
		this.videoKo = "videos/button_cancel-512.mpg";
		timer1 = -1;
		notranscodefolder = true;
		this.enabled = enabled; 
	}

	@Override
	public InputStream getInputStream() throws IOException {
		if (timer1 == -1)
			timer1 = System.currentTimeMillis();
		else if (System.currentTimeMillis() - timer1 < 2000){
			timer1 = -1;
		}
		if (timer1 != -1)
			enabled = enable();
		return getResourceInputStream(enabled?videoOk:videoKo);
	}
	
	public abstract boolean enable();

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isFolder() {
		return false;
	}

	@Override
	public long length() {
		return -1; //DLNAMediaInfo.TRANS_SIZE;
	}

	public long lastModified() {
		return 0;
	}

	@Override
	public String getSystemName() {
		return getName();
	}
	
	@Override
	public InputStream getThumbnailInputStream() {
		return getResourceInputStream(enabled?thumbnailIconOK:thumbnailIconKO);
	}
	
	@Override
	public String getThumbnailContentType() {
		return thumbnailContentType;
	}

	@Override
	public boolean isValid() {
		ext = PMS.get().getAssociatedExtension("toto.mpg");
		return true;
	}
	
}
